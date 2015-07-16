package com.revolsys.gis.jts.filter;

import java.util.function.Predicate;

import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.jts.geom.LineString;

public class LineIntersectsFilter implements Predicate<LineString> {
  private final LineString line;

  public LineIntersectsFilter(final LineString line) {
    this.line = line;
  }

  @Override
  public boolean test(final LineString line) {
    return LineStringUtil.intersects(this.line, line);
  }
}
