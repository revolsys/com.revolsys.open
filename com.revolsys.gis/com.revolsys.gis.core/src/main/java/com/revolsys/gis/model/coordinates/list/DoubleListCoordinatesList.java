package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.util.MathUtil;

public class DoubleListCoordinatesList extends AbstractCoordinatesList {

  private List<Double> coordinates = new ArrayList<Double>();

  private final byte numAxis;

  public DoubleListCoordinatesList(final CoordinatesList coordinatesList) {
    this(coordinatesList.getNumAxis(), coordinatesList.getCoordinates());
  }

  public DoubleListCoordinatesList(final CoordinatesList coordinatesList,
    final int numAxis) {
    this(coordinatesList.size(), numAxis);
    coordinatesList.copy(0, this, 0, numAxis, coordinatesList.size());
  }

  public DoubleListCoordinatesList(final int numAxis,
    final double... coordinates) {
    this.numAxis = (byte)numAxis;
    for (double coordinate : coordinates) {
      this.coordinates.add(coordinate);
    }
  }

  public DoubleListCoordinatesList(final int numAxis) {
    this.numAxis = (byte)numAxis;
  }

  public DoubleListCoordinatesList(final int numAxis,
    final List<Number> coordinates) {
    this(numAxis, MathUtil.toDoubleArray(coordinates));
  }

  @Override
  public DoubleListCoordinatesList clone() {
    return new DoubleListCoordinatesList(this);
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.size()];
    for (int i = 0; i < coordinates.length; i++) {
      double coordinate = this.coordinates.get(i);
      coordinates[i] = coordinate;

    }
    return coordinates;
  }

  public byte getNumAxis() {
    return numAxis;
  }

  public double getValue(final int index, final int axisIndex) {
    final byte numAxis = getNumAxis();
    if (axisIndex < numAxis && index < size()) {
      return coordinates.get(index * numAxis + axisIndex);
    } else {
      return Double.NaN;
    }
  }

  public void setValue(final int index, final int axisIndex, final double value) {
    final byte numAxis = getNumAxis();
    if (axisIndex < numAxis) {
      if (index <= size()) {
        for (int i = coordinates.size(); i < (index + 1) * numAxis; i++) {
          coordinates.add(0.0);
        }
      }
      coordinates.set(index * numAxis + axisIndex, value);
    }
  }

  public int size() {
    return coordinates.size() / numAxis;
  }
}
