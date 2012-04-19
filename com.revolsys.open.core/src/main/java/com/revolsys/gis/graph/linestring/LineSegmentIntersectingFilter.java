package com.revolsys.gis.graph.linestring;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;

public class LineSegmentIntersectingFilter implements Filter<LineSegment> {

  private final LineSegment line;

  public LineSegmentIntersectingFilter(final LineSegment line) {
    this.line = line;
  }

  public boolean accept(LineSegment line) {
    if (line == this.line) {
      return false;
    } else {
      final CoordinatesList intersection = this.line.getIntersection(line);
      return intersection != null && intersection.size() > 0;
    }
  }
}
