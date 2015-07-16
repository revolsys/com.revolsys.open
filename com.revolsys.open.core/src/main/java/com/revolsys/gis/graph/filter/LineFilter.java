package com.revolsys.gis.graph.filter;

import java.util.function.Predicate;
import com.revolsys.gis.graph.Edge;
import com.revolsys.jts.geom.LineString;

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
