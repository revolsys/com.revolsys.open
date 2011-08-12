package com.revolsys.gis.graph.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterProxy;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgePair;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.graph.attribute.PseudoNodeAttribute;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.data.equals.DataObjectEquals;
import com.revolsys.util.ObjectProcessor;
import com.vividsolutions.jts.geom.LineString;

/**
 * Find and remove nodes that have exactly two edges for each feature type with
 * the same attribution and have the same geometry across all feature types.
 * 
 * @author Paul Austin
 */
public class PseudoNodeRemovalVisitor extends
  AbstractNodeListenerVisitor<DataObject> implements
  FilterProxy<Node<DataObject>>, ObjectProcessor<DataObjectGraph> {
  /** The relationship between an Edge and it's next edge. */
  private enum EdgeType {
    /** The edge is backwards in relation to the next edge. */
    BACKWARDS,
    /** The edge has degree 1 at the TO node. */
    END_DEGREE_1,
    /** The edge has degree > 2 at the TO node. */
    END_DEGREE_N,
    /** The edge is forwards in relation to the next edge. */
    FORWARDS
  }

  private static final List<String> EQUAL_EXCLUDE = Arrays.asList(
    DataObjectEquals.EXCLUDE_ID, DataObjectEquals.EXCLUDE_GEOMETRY);

  private static final String REVERSE = PseudoNodeRemovalVisitor.class.getName()
    + "Reverse";

  private double autoFixEdgesLessThanLength = 3;

  private Collection<String> equalExcludeAttributes;

  private Filter<Node<DataObject>> filter;

  private Statistics mergedStatistics;
  

  @PreDestroy
  public void destroy() {
    if (mergedStatistics != null) {
      mergedStatistics.disconnect();
    }
    mergedStatistics = null;
  }

  @PostConstruct
  public void init() {
    mergedStatistics = new Statistics("Merged at psuedo node");
    mergedStatistics.connect();
  }

  public PseudoNodeRemovalVisitor() {
  }

  /**
   * Check the direction of the edge by checking following the edge to the TO
   * node until the degree of the node != 2 of the next edge is reversed.
   * 
   * @param node
   * @param edge The edge.
   * @return The type of edge.
   */
  private EdgeType checkDirection(final Node<DataObject> node,
    final QName typeName, final Edge<DataObject> edge) {
    final Node<DataObject> toNode = edge.getOppositeNode(node);
    final Map<QName, List<Edge<DataObject>>> edgesByType = NodeAttributes.getEdgesByType(toNode);
    final List<Edge<DataObject>> typeEdges = edgesByType.get(typeName);
    final int degree = typeEdges.size();
    if (degree == 1) {
      return EdgeType.END_DEGREE_1;
    } else if (degree == 2) {
      final Edge<DataObject> nextEdge = Node.getNextEdge(typeEdges, edge);
      if (edge.isForwards(toNode) == nextEdge.isForwards(toNode)) {
        return EdgeType.BACKWARDS;
      } else {
        final EdgeType nextDirection = checkDirection(toNode, typeName,
          nextEdge);
        if (nextDirection == EdgeType.BACKWARDS) {
          return EdgeType.BACKWARDS;
        } else {
          return EdgeType.FORWARDS;
        }
      }
    } else {
      return EdgeType.END_DEGREE_N;
    }

  }

  public double getAutoFixEdgesLessThanLength() {
    return autoFixEdgesLessThanLength;
  }

  public Filter<Node<DataObject>> getFilter() {
    return filter;
  }

  /**
   * Mark all the edges to be reversed. The method returns true if one edge in
   * each edge pair can be reversed, false otherwise.
   * 
   * @param node The node.
   * @param typeName The type name.
   * @param edgePairs The edge pairs.
   * @return True if all edge pairs could be reversed.
   */
  private boolean markReverseEdges(final Node<DataObject> node,
    final QName typeName, final List<EdgePair<DataObject>> edgePairs) {
    if (edgePairs == null) {
      return true;
    } else {
      boolean allEdgesReversed = true;
      for (final EdgePair<DataObject> edgePair : edgePairs) {
        final Edge<DataObject> edge1 = edgePair.getEdge1();
        final Edge<DataObject> edge2 = edgePair.getEdge2();
        final EdgeType edge1Direction = checkDirection(node, typeName, edge1);
        final EdgeType edge2Direction = checkDirection(node, typeName, edge2);
        if (edge1Direction == edge2Direction) {
          if (edge1.getLength() < autoFixEdgesLessThanLength) {
            edgePair.setProperty1(REVERSE, Boolean.TRUE);
          } else if (edge2.getLength() < autoFixEdgesLessThanLength) {
            edgePair.setProperty2(REVERSE, Boolean.TRUE);
          } else {
            allEdgesReversed = false;
          }
        } else if (edge1Direction == EdgeType.BACKWARDS) {
          edgePair.setProperty1(REVERSE, Boolean.TRUE);
        } else if (edge2Direction == EdgeType.BACKWARDS) {
          edgePair.setProperty2(REVERSE, Boolean.TRUE);
        } else {
          allEdgesReversed = false;
        }
      }
      return allEdgesReversed;
    }
  }

  private void mergeEdgePairs(final Node<DataObject> node,
    final QName typeName, final List<EdgePair<DataObject>> edgePairs) {
    if (edgePairs != null) {
      for (final EdgePair<DataObject> edgePair : edgePairs) {
        final Edge<DataObject> edge1 = edgePair.getEdge1();
        final DataObject object1 = edge1.getObject();
        final LineString line1 = edge1.getLine();
        String notes1 = "Features merged";

        final Edge<DataObject> edge2 = edgePair.getEdge2();
        final DataObject object2 = edge2.getObject();
        final LineString line2 = edge2.getLine();
        String notes2 = "Features merged";

        LineString newLine = null;

        if (edgePair.getProperty1(REVERSE) == Boolean.TRUE) {
          newLine = LineStringUtil.merge(line2, line1);
          notes1 = "Features merged and reversed";
        } else if (edgePair.getProperty2(REVERSE) == Boolean.TRUE) {
          newLine = LineStringUtil.merge(line1, line2);
          notes2 = "Features merged and reversed";
        } else {
          newLine = LineStringUtil.merge(line1, line2);
        }

        DataObject objectToCopy;
        if (edgePair.getProperty1(PseudoNodeAttribute.AUTO_FIX_LENGTH) == Boolean.TRUE) {
          objectToCopy = object2;
          notes1 += " short length with different attributes";
        } else if (edgePair.getProperty2(PseudoNodeAttribute.AUTO_FIX_LENGTH) == Boolean.TRUE) {
          objectToCopy = object1;
          notes2 += " short length with different attributes";
        } else {
          objectToCopy = object1;
        }

        final DataObject newObject = DataObjectUtil.copy(objectToCopy, newLine);
        newObject.setIdValue(null);

        final Graph<DataObject> graph = edge1.getGraph();
        graph.add(newObject, newLine);
        graph.remove(edge1);
        graph.remove(edge2);
        mergedStatistics.add(object1);
      }
    }
  }

  public void process(final DataObjectGraph graph) {
    graph.visitNodes(this);
  }

  private void processPseudoNodes(final Node<DataObject> node,
    final PseudoNodeAttribute pseudoNodeAttribute) {
    for (final QName typeName : NodeAttributes.getEdgeTypeNames(node)) {
      processPseudoNodesForType(node, typeName, pseudoNodeAttribute);
    }
  }

  private void processPseudoNodesForType(final Node<DataObject> node,
    final QName typeName, final PseudoNodeAttribute pseudoNodeAttribute) {
    final Map<QName, List<EdgePair<DataObject>>> typeEdgePairs = pseudoNodeAttribute.getTypeEdgePairs();
    final Map<QName, List<EdgePair<DataObject>>> typeReversedEdgePairs = pseudoNodeAttribute.getTypeReversedEdgePairs();

    final List<EdgePair<DataObject>> reversedEdgePairs = typeReversedEdgePairs.get(typeName);
    if (!markReverseEdges(node, typeName, reversedEdgePairs)) {
      // TODO unable to reverse edges
    } else {
      mergeEdgePairs(node, typeName, reversedEdgePairs);

      final List<EdgePair<DataObject>> edgePairs = typeEdgePairs.get(typeName);
      mergeEdgePairs(node, typeName, edgePairs);
    }
  }

  public void setAutoFixEdgesLessThanLength(
    final double autoFixEdgesLessThanLength) {
    this.autoFixEdgesLessThanLength = autoFixEdgesLessThanLength;
    PseudoNodeAttribute.setAutoFixEdgesLessThanLength(autoFixEdgesLessThanLength);
  }

  public void setFilter(final Filter<Node<DataObject>> filter) {
    this.filter = filter;
  }

  public boolean visit(final Node<DataObject> node) {
    if (node.getEdges().size() > 1) {
      if (equalExcludeAttributes == null) {
        equalExcludeAttributes = new HashSet<String>(EQUAL_EXCLUDE);
      }
      final PseudoNodeAttribute pseudoNodeAttribute = PseudoNodeAttribute.getAttribute(
        node, equalExcludeAttributes);

      processPseudoNodes(node, pseudoNodeAttribute);
    }
    return true;
  }
}
