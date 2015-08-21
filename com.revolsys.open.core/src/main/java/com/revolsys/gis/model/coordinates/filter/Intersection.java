package com.revolsys.gis.model.coordinates.filter;

import java.util.function.Predicate;

import com.revolsys.geometry.model.LineString;
import com.revolsys.gis.graph.linestring.LineStringGraph;

public class Intersection implements Predicate<LineString> {

  private final LineString line;

  private final LineStringGraph graph;

  public Intersection(final LineString line) {
    this.line = line;
    this.graph = new LineStringGraph(line);
  }

  public LineString getLine() {
    return this.line;
  }

  @Override
  public boolean test(final LineString line) {
    return this.graph.intersects(line);
  }
}
