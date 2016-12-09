package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.number.Doubles;

public class PointDoubleXY extends AbstractPoint {
  private static final long serialVersionUID = 1L;

  public static PointDoubleXY newPoint(final Point point, final double scaleFactor) {
    final double x = Doubles.makePrecise(scaleFactor, point.getX());
    final double y = Doubles.makePrecise(scaleFactor, point.getY());
    return new PointDoubleXY(x, y);
  }

  protected double x;

  protected double y;

  protected PointDoubleXY() {
  }

  public PointDoubleXY(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public PointDoubleXY(final Point point) {
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

  protected void setX(final double x) {
    this.x = x;
  }

  protected void setY(final double y) {
    this.y = y;
  }

}
