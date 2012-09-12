package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;

public class EdgeObjectFilter<T> implements Filter<Edge<T>> {
  private Filter<T> filter;

  public EdgeObjectFilter() {
  }

  public EdgeObjectFilter(final Filter<T> filter) {
    this.filter = filter;
  }

  @Override
  public boolean accept(final Edge<T> edge) {
    final T object = edge.getObject();
    return filter.accept(object);
  }

  public Filter<T> getFilter() {
    return filter;
  }

  public void setFilter(final Filter<T> filter) {
    this.filter = filter;
  }
}
