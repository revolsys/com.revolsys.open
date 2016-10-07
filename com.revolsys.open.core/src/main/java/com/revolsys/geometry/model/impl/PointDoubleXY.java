package com.revolsys.geometry.model.impl;

import java.io.Serializable;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class PointDoubleXY extends AbstractPoint implements Serializable {
  private static final long serialVersionUID = 1L;

  protected double x;

  protected double y;

  public PointDoubleXY() {
    this(Double.NaN, Double.NaN);
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

  @Override
  public PointDoubleXY clone() {
    return (PointDoubleXY)super.clone();
  }

  @Override
  public int getAxisCount() {
    return 2;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      if (axisIndex == X) {
        return this.x;
      } else if (axisIndex == Y) {
        return this.y;
      } else {
        return Double.NaN;
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

  protected void setX(final double x) {
    this.x = x;
  }

  protected void setY(final double y) {
    this.y = y;
  }
}
