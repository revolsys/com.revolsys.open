package com.revolsys.gis.model.coordinates.filter;

import java.util.function.Predicate;

import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineSegment;

public class PointOnLineSegment implements Predicate<Point> {

  private final LineSegment lineSegment;

  private final double maxDistance;

  public PointOnLineSegment(final LineSegment lineSegment, final double maxDistance) {
    this.lineSegment = lineSegment;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean test(final Point point) {
    final Point start = this.lineSegment.getPoint(0);
    final Point end = this.lineSegment.getPoint(1);
    final boolean onLine = LineSegmentUtil.isPointOnLine(start, end, point, this.maxDistance);
    return onLine;
  }
}
