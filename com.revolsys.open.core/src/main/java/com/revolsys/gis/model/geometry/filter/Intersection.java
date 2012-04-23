package com.revolsys.gis.model.geometry.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.linestring.LineStringGraph;
import com.vividsolutions.jts.geom.LineString;

public class Intersection implements Filter<LineString> {

  private final LineString line;

  private final LineStringGraph graph;

  public Intersection(final LineString line) {
    this.line = line;
    this.graph = new LineStringGraph(line);
  }

  public boolean accept(final LineString line) {
    return graph.intersects(line);
  }

  public LineString getLine() {
    return line;
  }
}
