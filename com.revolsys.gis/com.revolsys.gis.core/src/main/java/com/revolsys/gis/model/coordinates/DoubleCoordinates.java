package com.revolsys.gis.model.coordinates;

public class DoubleCoordinates extends AbstractCoordinates {
  private final double[] coordinates;

  public DoubleCoordinates(
    final double... ordinates) {
    this.coordinates = ordinates;
  }

  public DoubleCoordinates(
    final int dimension) {
    this.coordinates = new double[dimension];
  }

  public DoubleCoordinates(
    Coordinates coordinates) {
    this.coordinates = coordinates.getCoordinates();
  }

  @Override
  public DoubleCoordinates clone() {
    return new DoubleCoordinates(coordinates);
  }

  @Override
  public double[] getCoordinates() {
    double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0,
      this.coordinates.length);
    return coordinates;
  }

  @Override
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
