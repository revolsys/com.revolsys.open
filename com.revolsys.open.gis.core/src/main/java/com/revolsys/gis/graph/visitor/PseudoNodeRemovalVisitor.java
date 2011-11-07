package com.revolsys.gis.graph.visitor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;

import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterProxy;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.graph.DataObjectGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgePair;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.graph.attribute.PseudoNodeAttribute;
import com.revolsys.gis.io.Statistics;
import com.revolsys.gis.model.data.equals.DataObjectEquals;
import com.revolsys.gis.util.NoOp;
import com.revolsys.util.ObjectProcessor;

/**
 * Find and remove nodes that have exactly two edges for each feature type with
 * the same attribution and have the same geometry across all feature types.
 * 
 * @author Paul Austin
 */
public class PseudoNodeRemovalVisitor extends
  AbstractNodeListenerVisitor<DataObject> implements
  FilterProxy<Node<DataObject>>, ObjectProcessor<DataObjectGraph> {

  private static final List<String> EQUAL_EXCLUDE = Arrays.asList(
    DataObjectEquals.EXCLUDE_ID, DataObjectEquals.EXCLUDE_GEOMETRY);

  private Collection<String> equalExcludeAttributes;

  private Filter<Node<DataObject>> filter;

  private Statistics mergedStatistics;

  public PseudoNodeRemovalVisitor() {
  }

  @PreDestroy
  public void destroy() {
    if (mergedStatistics != null) {
      mergedStatistics.disconnect();
    }
    mergedStatistics = null;
  }

  public Filter<Node<DataObject>> getFilter() {
    return filter;
  }

  @PostConstruct
  public void init() {
    mergedStatistics = new Statistics("Merged at psuedo node");
    mergedStatistics.connect();
  }

  private void mergeEdgePairs(final Node<DataObject> node,
    final List<EdgePair<DataObject>> edgePairs) {
    if (edgePairs != null) {
      for (final EdgePair<DataObject> edgePair : edgePairs) {
        final Edge<DataObject> edge1 = edgePair.getEdge1();
        final Edge<DataObject> edge2 = edgePair.getEdge2();
        final DataObject object = edge1.getObject();
        if (mergeEdges(node, edge1, edge2) != null) {
          mergedStatistics.add(object);
        }
      }
    }
  }

  protected Edge<DataObject> mergeEdges(final Node<DataObject> node,
    final Edge<DataObject> edge1, final Edge<DataObject> edge2) {
    final DataObject object1 = edge1.getObject();

    final DataObject object2 = edge2.getObject();

    final DataObject newObject = mergeObjects(node, object1, object2);
    newObject.setIdValue(null);

    final DataObjectGraph graph = (DataObjectGraph)edge1.getGraph();
    Edge<DataObject> newEdge = graph.add(newObject);
    graph.remove(edge1);
    graph.remove(edge2);
    return newEdge;
  }

  protected DataObject mergeObjects(final Node<DataObject> node,
    final DataObject object1, final DataObject object2) {
    return DirectionalAttributes.mergeLongest(node, object1, object2);
  }

  public void process(final DataObjectGraph graph) {
    graph.visitNodes(this);
  }

  private void processPseudoNodes(final Node<DataObject> node) {
    for (final QName typeName : NodeAttributes.getEdgeTypeNames(node)) {
      final PseudoNodeAttribute pseudoNodeAttribute = new PseudoNodeAttribute(
        node, typeName, equalExcludeAttributes);
      processPseudoNodesForType(node, pseudoNodeAttribute);
    }
  }

  private void processPseudoNodesForType(final Node<DataObject> node,
    final PseudoNodeAttribute pseudoNodeAttribute) {
    final List<EdgePair<DataObject>> reversedEdgePairs = pseudoNodeAttribute.getReversedEdgePairs();
    mergeEdgePairs(node, reversedEdgePairs);

    final List<EdgePair<DataObject>> edgePairs = pseudoNodeAttribute.getEdgePairs();
    mergeEdgePairs(node, edgePairs);
  }

  public void setFilter(final Filter<Node<DataObject>> filter) {
    this.filter = filter;
  }

  public boolean visit(final Node<DataObject> node) {
    if (node.equals(1216090.409, 473112.314)) {
      NoOp.noOp();
    }
    if (node.getEdges().size() > 1) {
      if (equalExcludeAttributes == null) {
        equalExcludeAttributes = new HashSet<String>(EQUAL_EXCLUDE);
      }

      processPseudoNodes(node);
    }
    return true;
  }
}
