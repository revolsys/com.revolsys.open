package com.revolsys.gis.model.coordinates.filter;

import com.revolsys.filter.Filter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.LineSegment;

public class CrossingLineSegmentFilter implements Filter<LineSegment> {
  private final LineSegment line;

  public CrossingLineSegmentFilter(final LineSegment line) {
    this.line = line;
  }

  @Override
  public boolean accept(final LineSegment line) {
    if (this.line == line) {
      return false;
    } else {
      final CoordinatesList intersections = this.line.getIntersection(line);
      if (intersections.size() == 1) {
        final Coordinates intersection = intersections.get(0);
        if (this.line.contains(intersection)) {
          return false;
        } else {
          return true;
        }
      } else {
        return false;
      }
    }
  }
}
