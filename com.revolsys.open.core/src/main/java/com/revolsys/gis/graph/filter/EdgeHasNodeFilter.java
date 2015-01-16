package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Node;

public class EdgeHasNodeFilter<T> implements Filter<Edge<T>> {
  private final Node<T> node;

  public EdgeHasNodeFilter(final Node<T> node) {
    this.node = node;
  }

  @Override
  public boolean accept(final Edge<T> edge) {
    return edge.hasNode(this.node);
  }
}
