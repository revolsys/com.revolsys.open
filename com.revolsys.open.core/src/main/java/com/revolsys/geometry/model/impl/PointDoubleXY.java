package com.revolsys.geometry.model.impl;

import java.io.Serializable;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class PointDoubleXY extends AbstractPoint implements Serializable {
  private static final long serialVersionUID = 1L;

  protected double x;

  protected double y;

  public PointDoubleXY() {
    this.x = java.lang.Double.NaN;
    this.y = java.lang.Double.NaN;
  }

  public PointDoubleXY(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  public PointDoubleXY(final GeometryFactory geometryFactory, final double x, final double y) {
    this.x = geometryFactory.makeXyPrecise(x);
    this.y = geometryFactory.makeXyPrecise(y);
  }

  public PointDoubleXY(final Point point) {
    this(point.getX(), point.getY());
  }

  /**
   * Creates and returns a full copy of this {@link Point} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public PointDoubleXY clone() {
    return (PointDoubleXY)super.clone();
  }

  @Override
  public void copyCoordinates(final double[] coordinates) {
    coordinates[X] = this.x;
    coordinates[Y] = this.y;
    for (int i = 2; i < coordinates.length; i++) {
      coordinates[i] = java.lang.Double.NaN;
    }
  }

  @Override
  public double distancePoint(Point point) {
    if (isEmpty()) {
      return java.lang.Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(point)) {
      return java.lang.Double.POSITIVE_INFINITY;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertPoint2d(geometryFactory);
      final double x = point.getX();
      final double y = point.getY();
      final double x1 = this.x;
      final double y1 = this.y;
      return MathUtil.distance(x1, y1, x, y);
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equals(point);
    } else {
      return false;
    }
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return java.lang.Double.NaN;
    } else {
      if (axisIndex == X) {
        return this.x;
      } else if (axisIndex == Y) {
        return this.y;
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    return new double[] {
      this.x, this.y
    };
  }

  @Override
  public double getX() {
    return this.x;
  }

  @Override
  public double getY() {
    return this.y;
  }

  @Override
  public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(this.x);
    bits ^= java.lang.Double.doubleToLongBits(this.y) * 31;
    return (int)bits ^ (int)(bits >> 32);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public Point move(final double... deltas) {
    if (deltas == null) {
      return this;
    } else {
      double x = this.x;
      if (deltas.length > 0) {
        x += deltas[0];
      }
      double y = this.y;
      if (deltas.length > 1) {
        y += deltas[1];
      }
      return new PointDoubleXY(x, y);
    }
  }

  @Override
  public void setLocation(final double x, final double y) {
    throw new UnsupportedOperationException();
  }

  protected void setX(final double x) {
    this.x = x;
  }

  protected void setY(final double y) {
    this.y = y;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
