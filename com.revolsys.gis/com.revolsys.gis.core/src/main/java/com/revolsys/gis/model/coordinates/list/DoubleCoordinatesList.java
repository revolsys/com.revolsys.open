package com.revolsys.gis.model.coordinates.list;

import com.revolsys.gis.util.NoOp;

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
    this(coordinatesList.getNumAxis(), coordinatesList.getCoordinates());
  }

  public DoubleCoordinatesList(
    final int numAxis,
    final double... coordinates) {
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
    if (axisIndex < numAxis) {
      return coordinates[index * numAxis + axisIndex];
    } else {
      return Double.NaN;
    }
  }

  public void setValue(
    final int index,
    final int axisIndex,
    final double value) {
    if (axisIndex < 2) {
      NoOp.equals(value, 1.0);
    }
    final byte numAxis = getNumAxis();
    if (axisIndex < numAxis) {
      coordinates[index * numAxis + axisIndex] = value;
    }
  }

  public int size() {
    return coordinates.length / numAxis;
  }
}
