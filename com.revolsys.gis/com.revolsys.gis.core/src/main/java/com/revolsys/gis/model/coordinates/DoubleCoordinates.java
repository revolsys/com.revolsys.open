package com.revolsys.gis.model.coordinates;

public class DoubleCoordinates implements Coordinates {
  private final double[] coordinates;

  public DoubleCoordinates(
    final double... ordinates) {
    this.coordinates = ordinates;
  }

  public DoubleCoordinates(
    final int dimension) {
    this.coordinates = new double[dimension];
  }

  public byte getNumAxis() {
    return (byte)coordinates.length;
  }

  public double getValue(
    final int index) {
    if (index >= 0 && index < getNumAxis()) {
      return coordinates[index];
    } else {
      return Double.NaN;
    }
  }

  public void setValue(
    final int index,
    final double value) {
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
