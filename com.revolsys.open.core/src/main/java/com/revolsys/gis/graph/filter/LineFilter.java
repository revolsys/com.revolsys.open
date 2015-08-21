package com.revolsys.gis.graph.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.LineString;
import com.revolsys.gis.graph.Edge;

public class LineFilter<T> implements Predicate<Edge<T>> {
  private final Predicate<LineString> filter;

  public LineFilter(final Predicate<LineString> filter) {
    this.filter = filter;
  }

  @Override
  public boolean test(final Edge<T> edge) {
    final LineString line = edge.getLine();
    return this.filter.test(line);
  }

}
