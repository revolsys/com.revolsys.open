package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;

public class EdgeObjectFilter<T> implements Filter<Edge<T>> {
  private final Filter<T> filter;

  public EdgeObjectFilter(
    final Filter<T> filter) {
    this.filter = filter;
  }

  public boolean accept(
    final Edge<T> edge) {
    final T object = edge.getObject();
    return filter.accept(object);
  }
}
