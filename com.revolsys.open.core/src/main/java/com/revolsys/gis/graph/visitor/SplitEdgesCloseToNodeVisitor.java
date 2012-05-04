package com.revolsys.gis.graph.visitor;

import java.util.Collection;
import java.util.List;

import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;

public class SplitEdgesCloseToNodeVisitor<T> extends
  AbstractNodeListenerVisitor<T> {
  public static final String EDGE_CLOSE_TO_NODE = "Edge close to node";

  private final Graph<T> graph;

  private final double maxDistance;

  private Collection<Edge<T>> newEdges = null;

  private String ruleName = EDGE_CLOSE_TO_NODE;

  private Collection<T> splitObjects = null;

  public SplitEdgesCloseToNodeVisitor(final Graph<T> graph,
    final double maxDistance) {
    this.graph = graph;
    this.maxDistance = maxDistance;
  }

  public SplitEdgesCloseToNodeVisitor(final Graph<T> graph,
    final String ruleName, final double maxDistance) {
    this.graph = graph;
    this.ruleName = ruleName;
    this.maxDistance = maxDistance;
  }

  public double getMaxDistance() {
    return maxDistance;
  }

  public Collection<Edge<T>> getNewEdges() {
    return newEdges;
  }

  public Collection<T> getSplitObjects() {
    return splitObjects;
  }

  public void setNewEdges(final Collection<Edge<T>> newEdges) {
    this.newEdges = newEdges;
  }

  public void setSplitObjects(final Collection<T> splitObjects) {
    this.splitObjects = splitObjects;
  }

  public boolean visit(final Node<T> node) {
    final List<Edge<T>> closeEdges = EdgeLessThanDistanceToNodeVisitor.edgesWithinDistance(
      graph, node, maxDistance);
    for (final Edge<T> edge : closeEdges) {
      final T object = edge.getObject();
      final String typePath = graph.getTypeName(edge);
      final List<Edge<T>> splitEdges = graph.splitEdge(edge, node);
      if (splitEdges.size() > 1) {
        nodeEvent(node, typePath, ruleName, "Fixed", null);
        if (splitObjects != null) {
          splitObjects.add(object);
        }
        if (newEdges != null) {
          newEdges.remove(edge);
          newEdges.addAll(splitEdges);
        }
      }
    }
    return true;
  }

}
