package com.revolsys.gis.graph.filter;

import java.util.function.Predicate;
import com.revolsys.gis.graph.Node;
import com.revolsys.jts.geom.Point;

public class NodeCoordinatesFilter<T> implements Predicate<Node<T>> {
  private Predicate<Point> filter;

  public NodeCoordinatesFilter() {
  }

  public NodeCoordinatesFilter(final Predicate<Point> filter) {
    this.filter = filter;
  }

  @Override
  public boolean test(final Node<T> node) {
    return this.filter.test(node);
  }

  public Predicate<Point> getFilter() {
    return this.filter;
  }

  public void setFilter(final Predicate<Point> filter) {
    this.filter = filter;
  }
}
