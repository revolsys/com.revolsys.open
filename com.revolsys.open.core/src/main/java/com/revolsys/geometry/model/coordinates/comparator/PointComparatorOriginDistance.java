package com.revolsys.geometry.model.coordinates.comparator;

import java.util.Comparator;

import com.revolsys.geometry.model.Point;
import com.revolsys.util.MathUtil;

/**
 * Compare two points based on their distance from the originX, originY. If the distances are equal
 * compare the y coordinates and then the x coordinates.
 *
 */
public class PointComparatorOriginDistance implements Comparator<Point> {

  private final double originX;

  private final double originY;

  public PointComparatorOriginDistance() {
    this(0, 0);
  }

  public PointComparatorOriginDistance(final double x, final double y) {
    this.originX = x;
    this.originY = y;
  }

  public int compare(final double x1, final double y1, final double x2, final double y2) {
    final double distance1 = MathUtil.distance(this.originX, this.originY, x1, y1);
    final double distance2 = MathUtil.distance(this.originX, this.originY, x2, y2);
    int compare = Double.compare(distance1, distance2);
    if (compare == 0) {
      compare = Double.compare(y1, y2);
      if (compare == 0) {
        compare = Double.compare(x1, x2);
      }
    }
    return compare;
  }

  @Override
  public int compare(final Point c1, final Point c2) {
    final double x1 = c1.getX();
    final double y1 = c1.getY();
    final double x2 = c2.getX();
    final double y2 = c2.getY();
    return compare(x1, y1, x2, y2);
  }
}
