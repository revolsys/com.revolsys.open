package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;
import com.revolsys.math.Angle;

public class AngleFromPointComparator implements Comparator<Point> {

  private final double x;

  private final double y;

  public AngleFromPointComparator(final double x, final double y) {
    super();
    this.x = x;
    this.y = y;
  }

  @Override
  public int compare(final Point c1, final Point c2) {
    final double angleC1 = Angle.angle2d(this.x, this.y, c1.getX(), c1.getY());
    final double angleC2 = Angle.angle2d(this.x, this.y, c2.getX(), c2.getY());
    if (angleC1 < angleC2) {
      return 1;
    } else if (angleC1 > angleC2) {
      return -1;
    } else {
      return 0;
    }
  }
}
