package com.revolsys.gis.model.geometry.base;

import com.revolsys.gis.model.geometry.Point;
import com.revolsys.util.MathUtil;

public abstract class BasePoint extends BaseGeometry implements Point {
  /**
   * Calculate the angle in radians on a 2D plan between this point and another
   * point.
   * 
   * @param point The other point.
   * @return The angle in radians.
   */
  public double angle2d(
    final Point point) {
    final double x1 = getX();
    final double x2 = point.getX();
    final double y1 = getY();
    final double y2 = point.getY();
    return MathUtil.angle2d(x1, x2, y1, y2);
  }

  public int compareTo(
    final Point point) {
    final double x1 = getX();
    final double y1 = getY();
    final double x2 = getX();
    final double y2 = getY();

    if (x1 < x2 || y1 < y2) {
      return -1;
    } else if (x1 > x2 || y1 > y2) {
      return 1;
    } else {
      return 0;
    }
  }

  public double distance(
    final Point point) {
    final double x1 = getX();
    final double y1 = getY();
    final double x2 = point.getX();
    final double y2 = point.getY();
    return MathUtil.distance(x1, y1, x2, y2);
  }

  @Override
  public boolean equals(
    final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equals2d(point);
    } else {
      return false;
    }
  }

  public boolean equals2d(
    final Point point) {
    final double x1 = getX();
    final double x2 = point.getX();
    if (x1 == x2) {
      final double y1 = getY();
      final double y2 = point.getY();
      if (y1 == y2) {
        return true;
      }
    }
    return false;
  }
}
