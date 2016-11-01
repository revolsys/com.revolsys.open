package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;

public class CoordinatesDistanceComparator implements Comparator<Point> {
  private final double x;

  private final double y;

  public CoordinatesDistanceComparator(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public int compare(final Point point1, final Point point2) {
    int compare;
    final double distance1 = point1.distance(this.x, this.y);
    final double distance2 = point2.distance(this.x, this.y);
    if (distance1 == distance2) {
      compare = point1.compareTo(point2);
    } else if (distance1 < distance2) {
      compare = -1;
    } else {
      compare = 1;
    }

    return compare;
  }
}
