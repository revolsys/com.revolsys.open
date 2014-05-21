package com.revolsys.gis.graph.linestring;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.segment.LineSegment;

public class LineSegmentIntersectingFilter implements Filter<LineSegment> {

  private final LineSegment line;

  public LineSegmentIntersectingFilter(final LineSegment line) {
    this.line = line;
  }

  @Override
  public boolean accept(final LineSegment line) {
    if (line == this.line) {
      return false;
    } else {
      final Geometry intersection = this.line.getIntersection(line);
      return intersection != null && !intersection.isEmpty();
    }
  }
}
