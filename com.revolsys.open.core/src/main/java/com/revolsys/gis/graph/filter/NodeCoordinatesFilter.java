package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Coordinates;

public class NodeCoordinatesFilter<T> implements Filter<Node<T>> {
  private Filter<Coordinates> filter;

  public NodeCoordinatesFilter() {
  }

  public NodeCoordinatesFilter(final Filter<Coordinates> filter) {
    this.filter = filter;
  }

  @Override
  public boolean accept(final Node<T> node) {
    return filter.accept(node);
  }

  public Filter<Coordinates> getFilter() {
    return filter;
  }

  public void setFilter(final Filter<Coordinates> filter) {
    this.filter = filter;
  }
}
