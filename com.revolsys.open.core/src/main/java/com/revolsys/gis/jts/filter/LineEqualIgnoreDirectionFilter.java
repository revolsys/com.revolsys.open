package com.revolsys.gis.jts.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.jts.geom.LineString;

public class LineEqualIgnoreDirectionFilter implements Filter<LineString> {
  private final int dimension;

  private final LineString line;

  public LineEqualIgnoreDirectionFilter(final LineString line,
    final int dimension) {
    this.line = line;
    this.dimension = dimension;
  }

  @Override
  public boolean accept(final LineString line) {
    return LineStringUtil.equalsIgnoreDirection(line, this.line, dimension);
  }

}
