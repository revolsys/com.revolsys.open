package com.revolsys.gis.model.coordinates.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.jts.LineSegmentImpl;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineSegment;

public class LineSegmentCoordinateDistanceFilter implements Filter<LineSegment> {

  private final double maxDistance;

  private final Coordinates point;

  public LineSegmentCoordinateDistanceFilter(final Coordinates point,
    final double maxDistance) {
    this.point = point;
    this.maxDistance = maxDistance;
  }

  @Override
  public boolean accept(final LineSegment lineSegment) {
    final double distance = lineSegment.distance(this.point);
    if (distance < this.maxDistance) {
      return true;
    } else {
      return false;
    }
  }

}
