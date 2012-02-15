package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;

/**
 * A filter for {@link Node} objects which contains an {@link Edge} matching the
 * edge filter.
 * 
 * @author Paul Austin
 * @param <T>
 */
public class NodeMatchingEdgeFilter<T> implements Filter<Node<T>> {

  private final Filter<Edge<T>> edgeFilter;

  public NodeMatchingEdgeFilter(final Filter<Edge<T>> edgeFilter) {
    this.edgeFilter = edgeFilter;
  }

  public boolean accept(final Node<T> node) {
    for (final Edge<T> edge : node.getEdges()) {
      if (edgeFilter.accept(edge)) {
        return true;
      }
    }
    return false;
  }

}
