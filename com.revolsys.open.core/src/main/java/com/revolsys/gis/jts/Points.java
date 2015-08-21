package com.revolsys.gis.jts;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble2D;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.util.MathUtil;

public class Points {

  public static Point pointOffset(final Point point1, final Point point2, final double xOffset,
    double yOffset) {
    final double distance = point1.distance(point2);

    final double projectionFactor = xOffset / distance;
    if (Double.isNaN(projectionFactor) || Double.isInfinite(projectionFactor)) {
      return new PointDouble2D(point1.getX() + xOffset, point1.getY() + yOffset);
    } else {
      final Point point = LineSegmentUtil.pointAlong(point1, point2, projectionFactor);
      if (yOffset == 0) {
        return new PointDouble2D(point);
      } else {
        double angle = point1.angle2d(point2);
        if (yOffset > 0) {
          angle += MathUtil.PI_OVER_2;
        } else {
          angle -= MathUtil.PI_OVER_2;
          yOffset = -yOffset;
        }
        final double x = point.getX() + Math.cos(angle) * yOffset;
        final double y = point.getY() + Math.sin(angle) * yOffset;
        return new PointDouble2D(x, y);
      }
    }
  }

}
