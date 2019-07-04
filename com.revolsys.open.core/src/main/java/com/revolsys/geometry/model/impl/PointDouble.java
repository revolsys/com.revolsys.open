package com.revolsys.geometry.model.impl;

import java.io.Serializable;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class PointDouble extends AbstractPoint implements Serializable {
  private static final long serialVersionUID = 1L;

  private double[] coordinates;

  protected PointDouble() {
  }

  public PointDouble(final double... coordinates) {
    this(coordinates.length, coordinates);
  }

  protected PointDouble(final GeometryFactory geometryFactory, final double... coordinates) {
    if (coordinates != null && coordinates.length > 0) {
      final int axisCount = geometryFactory.getAxisCount();
      this.coordinates = new double[axisCount];
      for (int i = 0; i < axisCount; i++) {
        double value;
        if (i < coordinates.length) {
          value = geometryFactory.makePrecise(i, coordinates[i]);
        } else {
          value = Double.NaN;
        }
        this.coordinates[i] = value;
      }
    }
  }

  public PointDouble(final int axisCount) {
    if (axisCount > 1) {
      this.coordinates = new double[axisCount];
    } else {
      this.coordinates = null;
    }
  }

  public PointDouble(final int axisCount, final double... coordinates) {
    this.coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      double value;
      if (i < coordinates.length) {
        value = coordinates[i];
      } else {
        value = Double.NaN;
      }
      this.coordinates[i] = value;
    }
  }

  public PointDouble(final Point coordinates) {
    final int axisCount = coordinates.getAxisCount();
    this.coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      final double value = coordinates.getCoordinate(i);
      this.coordinates[i] = value;
    }
  }

  public PointDouble(final Point point, final int axisCount) {
    this(axisCount);
    final int pointAxisCount = point.getAxisCount();
    for (int i = 0; i < axisCount; i++) {
      final double value;
      if (i < pointAxisCount) {
        value = point.getCoordinate(i);
      } else {
        value = Double.NaN;
      }
      this.coordinates[i] = value;
    }
  }

  @Override
  public PointDouble clone() {
    final PointDouble point = (PointDouble)super.clone();
    if (this.coordinates != null) {
      point.coordinates = this.coordinates.clone();
    }
    return point;
  }

  @Override
  public int getAxisCount() {
    if (this.coordinates == null) {
      return 0;
    } else {
      return (byte)this.coordinates.length;
    }
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        return this.coordinates[axisIndex];
      } else {
        return Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    if (this.coordinates == null) {
      return this.coordinates;
    } else {
      return this.coordinates.clone();
    }
  }

  @Override
  public double getX() {
    if (this.coordinates == null) {
      return Double.NaN;
    } else {
      return this.coordinates[0];
    }
  }

  @Override
  public double getY() {
    if (this.coordinates == null) {
      return Double.NaN;
    } else {
      return this.coordinates[1];
    }
  }

  @Override
  public boolean isEmpty() {
    return this.coordinates == null;
  }

}
