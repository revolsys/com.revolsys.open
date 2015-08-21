package com.revolsys.gis.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;

public class AngleFromPointComparator implements Comparator<Point> {

  private final Point point;

  public AngleFromPointComparator(final Point point) {
    this.point = point;
  }

  @Override
  public int compare(final Point c1, final Point c2) {
    final double angleC1 = this.point.angle2d(c1);
    final double angleC2 = this.point.angle2d(c2);
    if (angleC1 < angleC2) {
      return 1;
    } else if (angleC1 > angleC2) {
      return -1;
    } else {
      return 0;
    }
  }
}
