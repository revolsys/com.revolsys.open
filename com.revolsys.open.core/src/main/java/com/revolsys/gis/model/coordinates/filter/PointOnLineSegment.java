package com.revolsys.gis.model.coordinates.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.geometry.LineSegment;

public class PointOnLineSegment implements Filter<Coordinates> {

  private LineSegment lineSegment;

  private double maxDistance;

  public PointOnLineSegment(LineSegment lineSegment, double maxDistance) {
    this.lineSegment = lineSegment;
    this.maxDistance = maxDistance;
  }

  public boolean accept(final Coordinates point) {
    final Coordinates start = lineSegment.get(0);
    final Coordinates end = lineSegment.get(1);
    final boolean onLine = LineSegmentUtil.isPointOnLine(start, end, point,
      maxDistance);
    return onLine;
  }
}
