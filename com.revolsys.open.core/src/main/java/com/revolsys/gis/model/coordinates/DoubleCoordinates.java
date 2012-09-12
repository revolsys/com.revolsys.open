package com.revolsys.gis.model.coordinates;

import java.io.Serializable;
import java.util.List;

import com.revolsys.util.MathUtil;

public class DoubleCoordinates extends AbstractCoordinates implements
  Serializable {
  private static final long serialVersionUID = 1L;

  private final double[] coordinates;

  public DoubleCoordinates(final Coordinates coordinates) {
    this.coordinates = coordinates.getCoordinates();
  }

  public DoubleCoordinates(final Coordinates point, final int numAxis) {
    this(numAxis);
    final int count = Math.min(numAxis, point.getNumAxis());
    for (int i = 0; i < count; i++) {
      final double value = point.getValue(i);
      setValue(i, value);
    }
  }

  public DoubleCoordinates(final double... coordinates) {
    this.coordinates = coordinates;
  }

  public DoubleCoordinates(final int dimension) {
    this.coordinates = new double[dimension];
  }

  public DoubleCoordinates(final int numAxis, final double[] coordinates) {
    this.coordinates = new double[numAxis];
    System.arraycopy(coordinates, 0, this.coordinates, 0,
      Math.min(numAxis, coordinates.length));
  }

  public DoubleCoordinates(final List<Number> coordinates) {
    this(MathUtil.toDoubleArray(coordinates));
  }

  @Override
  public DoubleCoordinates clone() {
    return new DoubleCoordinates(coordinates);
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0,
      this.coordinates.length);
    return coordinates;
  }

  @Override
  public byte getNumAxis() {
    return (byte)coordinates.length;
  }

  @Override
  public double getValue(final int index) {
    if (index >= 0 && index < getNumAxis()) {
      return coordinates[index];
    } else {
      return Double.NaN;
    }
  }

  @Override
  public void setValue(final int index, final double value) {
    if (index >= 0 && index < getNumAxis()) {
      coordinates[index] = value;
    }
  }

  @Override
  public String toString() {
    final byte numAxis = getNumAxis();
    if (numAxis > 0) {
      final StringBuffer s = new StringBuffer(String.valueOf(coordinates[0]));
      for (int i = 1; i < numAxis; i++) {
        final Double ordinate = coordinates[i];
        s.append(',');
        s.append(ordinate);
      }
      return s.toString();
    } else {
      return "";
    }
  }

}
