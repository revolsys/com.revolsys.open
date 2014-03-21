package com.revolsys.gis.model.geometry.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.LineSegment;

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
