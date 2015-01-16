package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Point;

public class NodeCoordinatesFilter<T> implements Filter<Node<T>> {
  private Filter<Point> filter;

  public NodeCoordinatesFilter() {
  }

  public NodeCoordinatesFilter(final Filter<Point> filter) {
    this.filter = filter;
  }

  @Override
  public boolean accept(final Node<T> node) {
    return this.filter.accept(node);
  }

  public Filter<Point> getFilter() {
    return this.filter;
  }

  public void setFilter(final Filter<Point> filter) {
    this.filter = filter;
  }
}
