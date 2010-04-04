package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;
import com.vividsolutions.jts.geom.LineString;

public class LineFilter<T> implements Filter<Edge<T>> {
  private Filter<LineString> filter;

  public LineFilter(
    final Filter<LineString> filter) {
    this.filter = filter;
  }

  public boolean accept(
    final Edge<T> edge) {
    final LineString line = edge.getLine();
    return filter.accept(line);
  }

}
