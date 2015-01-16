package com.revolsys.gis.model.coordinates.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.linestring.LineStringGraph;
import com.revolsys.jts.geom.LineString;

public class Intersection implements Filter<LineString> {

  private final LineString line;

  private final LineStringGraph graph;

  public Intersection(final LineString line) {
    this.line = line;
    this.graph = new LineStringGraph(line);
  }

  @Override
  public boolean accept(final LineString line) {
    return this.graph.intersects(line);
  }

  public LineString getLine() {
    return this.line;
  }
}
