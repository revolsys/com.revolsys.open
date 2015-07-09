package com.revolsys.jts.geom.impl;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.util.MathUtil;

public class PointDouble2D extends AbstractPoint {
  private static final long serialVersionUID = 1L;

  public static PointDouble2D create(final Point point, final double scaleFactor) {
    final double x = MathUtil.makePrecise(scaleFactor, point.getX());
    final double y = MathUtil.makePrecise(scaleFactor, point.getY());
    return new PointDouble2D(x, y);
  }

  private final double x;

  private final double y;

  public PointDouble2D(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public PointDouble2D(final Point point) {
    this(point.getX(), point.getY());
  }

  @Override
  public double getCoordinate(final int index) {
    switch (index) {
      case 0:
        return this.x;
      case 1:
        return this.y;
      default:
        return Double.NaN;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating(0, 2);
  }

  @Override
  public double getX() {
    return this.x;
  }

  @Override
  public double getY() {
    return this.y;
  }

}
