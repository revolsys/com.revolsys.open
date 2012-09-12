package com.revolsys.gis.model.geometry.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;

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
