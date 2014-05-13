package com.revolsys.gis.model.coordinates;

import java.io.Serializable;
import java.util.List;

import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.AbstractPoint;
import com.revolsys.util.MathUtil;

public class DoubleCoordinates extends AbstractPoint implements Serializable {
  private static final long serialVersionUID = 1L;

  private final double[] coordinates;

  public DoubleCoordinates(final double... coordinates) {
    this(coordinates.length, coordinates);
  }

  public DoubleCoordinates(final int axisCount) {
    this.coordinates = new double[axisCount];
  }

  public DoubleCoordinates(final int axisCount, final double... coordinates) {
    this.coordinates = new double[axisCount];
    System.arraycopy(coordinates, 0, this.coordinates, 0,
      Math.min(axisCount, coordinates.length));
  }

  public DoubleCoordinates(final List<Number> coordinates) {
    this(MathUtil.toDoubleArray(coordinates));
  }

  public DoubleCoordinates(final Point coordinates) {
    final int axisCount = coordinates.getAxisCount();
    this.coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      final double value = coordinates.getCoordinate(i);
      this.coordinates[i] = value;
    }
  }

  public DoubleCoordinates(final Point point, final int axisCount) {
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
  public int getAxisCount() {
    return (byte)coordinates.length;
  }

  @Override
  public double getCoordinate(final int index) {
    if (index >= 0 && index < getAxisCount()) {
      return coordinates[index];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0,
      this.coordinates.length);
    return coordinates;
  }

}
