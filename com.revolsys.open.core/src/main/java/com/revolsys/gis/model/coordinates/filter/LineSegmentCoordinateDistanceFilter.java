package com.revolsys.gis.model.coordinates.filter;

import java.util.function.Predicate;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.Segment;

public class LineSegmentCoordinateDistanceFilter implements Predicate<Segment> {

  private final double maxDistance;

  private final Point point;

  public LineSegmentCoordinateDistanceFilter(final Point point, final double maxDistance) {
    this.point = point;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean test(final Segment lineSegment) {
    final double distance = lineSegment.distance(this.point);
    if (distance < this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }

}
