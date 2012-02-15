package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.jts.LineStringUtil;
import com.vividsolutions.jts.geom.LineString;

public class LineIntersectsFilter implements Filter<LineString> {
  private final LineString line;

  public LineIntersectsFilter(final LineString line) {
    this.line = line;
  }

  public boolean accept(final LineString line) {
    return LineStringUtil.intersects(this.line, line);
  }
}
