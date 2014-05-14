package com.revolsys.jts.geom.impl;

import java.io.Serializable;
import java.util.List;

import com.revolsys.jts.geom.Point;
import com.revolsys.util.MathUtil;

public class PointDouble extends AbstractPoint implements Serializable {
  private static final long serialVersionUID = 1L;

  private double[] coordinates;

  public PointDouble(final double... coordinates) {
    this(coordinates.length, coordinates);
  }

  public PointDouble(final int axisCount) {
    this.coordinates = new double[axisCount];
  }

  public PointDouble(final int axisCount, final double... coordinates) {
    this.coordinates = new double[axisCount];
    System.arraycopy(coordinates, 0, this.coordinates, 0,
      Math.min(axisCount, coordinates.length));
  }

  public PointDouble(final List<Number> coordinates) {
    this(MathUtil.toDoubleArray(coordinates));
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
    if (coordinates != null) {
      point.coordinates = coordinates.clone();
    }
    return point;
  }

  @Override
  public int getAxisCount() {
    return (byte)coordinates.length;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        return coordinates[axisIndex];
      } else {
        return Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    if (coordinates == null) {
      return coordinates;
    } else {
      return this.coordinates.clone();
    }
  }

  @Override
  public Point move(final double... deltas) {
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final double[] coordinates = this.coordinates.clone();
      final int axisCount = Math.min(deltas.length, getAxisCount());
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[axisIndex] += deltas[axisIndex];
      }
      return new PointDouble(coordinates);
    }
  }
}
