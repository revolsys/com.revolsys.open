package com.revolsys.gis.graph.visitor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.visitor.AbstractVisitor;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.SimpleCoordinatesPrecisionModel;
import com.revolsys.gis.model.data.equals.DataObjectEquals;
import com.revolsys.gis.util.NoOp;
import com.revolsys.util.ObjectProcessor;
import com.vividsolutions.jts.geom.LineString;

public class ItersectsNodeEdgeCleanupVisitor extends
  AbstractVisitor<Edge<DataObject>> implements ObjectProcessor<DataObjectGraph> {

  private static final Logger LOG = LoggerFactory.getLogger(ItersectsNodeEdgeCleanupVisitor.class);

  private Statistics splitStatistics;

  private final Set<String> equalExcludeAttributes = new HashSet<String>(
    Arrays.asList(DataObjectEquals.EXCLUDE_ID,
      DataObjectEquals.EXCLUDE_GEOMETRY));

  @PreDestroy
  public void destroy() {
    if (splitStatistics != null) {
      splitStatistics.disconnect();
    }
    splitStatistics = null;
  }

  public Set<String> getEqualExcludeAttributes() {
    return equalExcludeAttributes;
  }

  @PostConstruct
  public void init() {
    splitStatistics = new Statistics("Split edges");
    splitStatistics.connect();
  }

  public void process(final DataObjectGraph graph) {
    graph.visitEdges(this);
  }

  public boolean visit(final Edge<DataObject> edge) {
    final QName typeName = edge.getTypeName();
    final Node<DataObject> fromNode = edge.getFromNode();
    final Node<DataObject> toNode = edge.getToNode();

    final Graph<DataObject> graph = edge.getGraph();
    final List<Node<DataObject>> nodes = graph.findNodes(edge, 2);
    for (final Iterator<Node<DataObject>> nodeIter = nodes.iterator(); nodeIter.hasNext();) {
      final Node<DataObject> node = nodeIter.next();
      final List<Edge<DataObject>> edges = NodeAttributes.getEdgesByType(node,
        typeName);
      if (edges.isEmpty()) {
        nodeIter.remove();
      }
    }
    if (!nodes.isEmpty()) {
      if (nodes.size() > 1) {
        for (int i = 0; i < nodes.size(); i++) {
          Node<DataObject> node1 = nodes.get(i);
          for (int j = i + 1; j < nodes.size(); j++) {
            Node<DataObject> node2 = nodes.get(j);
            if (node1.distance(node2) < 2) {
              if (edge.distance(node1) <= edge.distance(node2)) {
                nodes.remove(j);
              } else {
                nodes.remove(i);
                node1 = node2;
              }
            }
          }
        }
      }
      if (nodes.size() == 1) {
        final Node<DataObject> node = nodes.get(0);
        if (node.distance(fromNode) <= 10) {
          moveEndUndershoots(typeName, fromNode, node);
        } else if (node.distance(toNode) <= 10) {
          moveEndUndershoots(typeName, toNode, node);
        } else {
          graph.splitEdge(edge, nodes);
          splitStatistics.add(typeName);
        }
      } else {
        graph.splitEdge(edge, nodes);
      }

    }
    return true;
  }

  private boolean moveEndUndershoots(final QName typeName,
    final Node<DataObject> node1, final Node<DataObject> node2) {
    boolean matched = false;
    if (!node2.hasEdgeTo(node1)) {
      final Set<Double> angles1 = NodeAttributes.getEdgeAnglesByType(node2,
        typeName);
      final Set<Double> angles2 = NodeAttributes.getEdgeAnglesByType(node1,
        typeName);
      if (angles1.size() == 1 && angles2.size() == 1) {

        matched = node1.getGraph().moveNodesToMidpoint(typeName, node2, node1);
      }
    }
    return matched;
  }
}
