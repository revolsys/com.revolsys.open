package com.revolsys.gis.graph.visitor;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.data.record.property.PseudoNodeProperty;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.Filter;
import com.revolsys.filter.FilterProxy;
import com.revolsys.gis.graph.RecordGraph;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.EdgePair;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.graph.attribute.PseudoNodeAttribute;
import com.revolsys.gis.io.Statistics;
import com.revolsys.util.ObjectProcessor;

/**
 * Find and remove nodes that have exactly two edges for each feature type with
 * the same attribution and have the same geometry across all feature types.
 * 
 * @author Paul Austin
 */
public class RecordPseudoNodeRemovalVisitor extends
  AbstractNodeListenerVisitor<Record> implements
  FilterProxy<Node<Record>>, ObjectProcessor<RecordGraph> {

  private Filter<Node<Record>> filter;

  private Statistics mergedStatistics;

  public RecordPseudoNodeRemovalVisitor() {
  }

  @PreDestroy
  public void destroy() {
    if (mergedStatistics != null) {
      mergedStatistics.disconnect();
    }
    mergedStatistics = null;
  }

  @Override
  public Filter<Node<Record>> getFilter() {
    return filter;
  }

  @PostConstruct
  public void init() {
    mergedStatistics = new Statistics("Merged at psuedo node");
    mergedStatistics.connect();
  }

  private void mergeEdgePairs(final Node<Record> node,
    final List<EdgePair<Record>> edgePairs) {
    if (edgePairs != null) {
      for (final EdgePair<Record> edgePair : edgePairs) {
        final Edge<Record> edge1 = edgePair.getEdge1();
        final Edge<Record> edge2 = edgePair.getEdge2();
        final Record object = edge1.getObject();
        if (mergeEdges(node, edge1, edge2) != null) {
          mergedStatistics.add(object);
        }
      }
    }
  }

  protected Edge<Record> mergeEdges(final Node<Record> node,
    final Edge<Record> edge1, final Edge<Record> edge2) {
    final Record object1 = edge1.getObject();

    final Record object2 = edge2.getObject();

    final Record newObject = mergeObjects(node, object1, object2);
    // newObject.setIdValue(null);

    final RecordGraph graph = (RecordGraph)edge1.getGraph();
    final Edge<Record> newEdge = graph.addEdge(newObject);
    graph.remove(edge1);
    graph.remove(edge2);
    return newEdge;
  }

  protected Record mergeObjects(final Node<Record> node,
    final Record object1, final Record object2) {
    return DirectionalAttributes.merge(node, object1, object2);
  }

  @Override
  public void process(final RecordGraph graph) {
    graph.visitNodes(this);
  }

  private void processPseudoNodes(final Node<Record> node) {
    for (final RecordDefinition recordDefinition : NodeAttributes.getEdgeMetaDatas(node)) {
      final PseudoNodeProperty property = PseudoNodeProperty.getProperty(recordDefinition);

      final PseudoNodeAttribute pseudoNodeAttribute = property.getAttribute(node);
      processPseudoNodesForType(node, pseudoNodeAttribute);
    }
  }

  private void processPseudoNodesForType(final Node<Record> node,
    final PseudoNodeAttribute pseudoNodeAttribute) {
    final List<EdgePair<Record>> reversedEdgePairs = pseudoNodeAttribute.getReversedEdgePairs();
    mergeEdgePairs(node, reversedEdgePairs);

    final List<EdgePair<Record>> edgePairs = pseudoNodeAttribute.getEdgePairs();
    mergeEdgePairs(node, edgePairs);
  }

  public void setFilter(final Filter<Node<Record>> filter) {
    this.filter = filter;
  }

  @Override
  public boolean visit(final Node<Record> node) {
    if (node.getEdges().size() > 1) {
      processPseudoNodes(node);
    }
    return true;
  }
}
