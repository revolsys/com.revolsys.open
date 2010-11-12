package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.jts.LineStringUtil;
import com.vividsolutions.jts.geom.LineString;

public class LineIntersectsFilter implements Filter<LineString> {
  private LineString line;

  public LineIntersectsFilter(
    LineString line) {
    this.line = line;
  }

  public boolean accept(
    LineString line) {
    return LineStringUtil.intersects(this.line, line);
  }
}
