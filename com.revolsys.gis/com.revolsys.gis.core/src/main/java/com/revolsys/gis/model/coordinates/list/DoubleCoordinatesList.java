package com.revolsys.gis.model.coordinates.list;

public class DoubleCoordinatesList extends AbstractCoordinatesList {

  double[] coordinates;

  private final byte numAxis;

  public DoubleCoordinatesList(
    final CoordinatesList coordinatesList,
    int numAxis) {
    this(coordinatesList.size(), numAxis);
    coordinatesList.copy(0, this, 0, numAxis, coordinatesList.size());
  }

  public DoubleCoordinatesList(
    final CoordinatesList coordinatesList) {
    this(coordinatesList.getCoordinates(), coordinatesList.getNumAxis());
  }

  public DoubleCoordinatesList(
    final double[] coordinates,
    final int numAxis) {
    this.numAxis = (byte)numAxis;
    this.coordinates = coordinates;
  }

  public DoubleCoordinatesList(
    final int size,
    final int numAxis) {
    this.coordinates = new double[size * numAxis];
    this.numAxis = (byte)numAxis;
  }

  @Override
  public DoubleCoordinatesList clone() {
    return new DoubleCoordinatesList(this);
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0, coordinates.length);
    return coordinates;
  }

  public byte getNumAxis() {
    return numAxis;
  }

  public double getValue(
    final int index,
    final int axisIndex) {
    final byte numAxis = getNumAxis();
    return coordinates[index * numAxis + axisIndex];
  }

  public void setValue(
    final int index,
    final int axisIndex,
    final double value) {
    final byte numAxis = getNumAxis();
    coordinates[index * numAxis + axisIndex] = value;
  }

  public int size() {
    return coordinates.length / numAxis;
  }
}
