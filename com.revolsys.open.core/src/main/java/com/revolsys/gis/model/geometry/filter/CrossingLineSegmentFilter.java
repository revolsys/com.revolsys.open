package com.revolsys.gis.model.geometry.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;

public class CrossingLineSegmentFilter implements Filter<LineSegment> {
  private LineSegment line;

  public CrossingLineSegmentFilter(LineSegment line) {
    this.line = line;
  }

  public boolean accept(LineSegment line) {
    if (this.line == line) {
      return false;
    } else {
      CoordinatesList intersections = this.line.getIntersection(line);
      if (intersections.size() == 1) {
        Coordinates intersection = intersections.get(0);
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
