package com.revolsys.gis.graph.visitor;

import java.util.List;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;

public class SplitEdgesOnNodeVisitor<T> implements Visitor<Node<T>> {

  /**
   * Split edges which the node is on the line of the edge. The edge will only
   * be split if the original node has at least one edge which shares the the
   * first two coordinates as one of the split lines.
   *
   * @param node The node.
   * @return True if an edge was split, false otherwise.
   */
  private boolean splitEdgesCloseToNode(final Node<T> node) {
    final Graph<T> graph = node.getGraph();
    final List<Edge<T>> nodeEdges = node.getEdges();
    if (!nodeEdges.isEmpty()) {
      final List<Edge<T>> edges = NodeOnEdgeVisitor.getEdges(graph, node, 1);
      for (final Edge<T> edge : edges) {
        if (!edge.isRemoved() && !node.hasEdge(edge)) {
          graph.splitEdge(edge, node);
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean visit(final Node<T> node) {
    while (splitEdgesCloseToNode(node)) {
    }
    return true;
  }

}
