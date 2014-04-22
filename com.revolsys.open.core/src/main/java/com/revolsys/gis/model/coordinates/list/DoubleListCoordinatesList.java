package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.util.MathUtil;

public class DoubleListCoordinatesList extends AbstractCoordinatesList {

  private static final long serialVersionUID = 4917034011117842840L;

  private List<Double> coordinates = new ArrayList<Double>();

  private final byte axisCount;

  public DoubleListCoordinatesList(final CoordinatesList coordinatesList) {
    this(coordinatesList.getAxisCount(), coordinatesList.getCoordinates());
  }

  public DoubleListCoordinatesList(final CoordinatesList coordinatesList,
    final int axisCount) {
    this(coordinatesList.size(), axisCount);
    coordinatesList.copy(0, this, 0, axisCount, coordinatesList.size());
  }

  public DoubleListCoordinatesList(final int axisCount) {
    this.axisCount = (byte)axisCount;
  }

  public DoubleListCoordinatesList(final int axisCount,
    final double... coordinates) {
    this.axisCount = (byte)axisCount;
    for (final double coordinate : coordinates) {
      this.coordinates.add(coordinate);
    }
  }

  public DoubleListCoordinatesList(final int axisCount,
    final List<Number> coordinates) {
    this(axisCount, MathUtil.toDoubleArray(coordinates));
  }

  public void add(final Coordinates point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double value;
      if (axisIndex < point.getAxisCount()) {
        value = point.getValue(axisIndex);
      } else {
        value = Double.NaN;
      }
      coordinates.add(value);
    }
  }

  public void add(final CoordinatesList points, final int index) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double value;
      if (axisIndex < points.getAxisCount()) {
        value = points.getValue(index, axisIndex);
      } else {
        value = Double.NaN;
      }
      coordinates.add(value);
    }
  }

  public void addAll(final CoordinatesList points) {
    for (final Coordinates point : new InPlaceIterator(points)) {
      add(point);
    }
  }

  public void clear() {
    coordinates.clear();
  }

  @Override
  public DoubleListCoordinatesList clone() {
    final DoubleListCoordinatesList clone = (DoubleListCoordinatesList)super.clone();
    clone.coordinates = new ArrayList<Double>(coordinates);
    return clone;
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.size()];
    for (int i = 0; i < coordinates.length; i++) {
      final double coordinate = this.coordinates.get(i);
      coordinates[i] = coordinate;

    }
    return coordinates;
  }

  @Override
  public int getAxisCount() {
    return axisCount;
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount && index < size()) {
      return coordinates.get(index * axisCount + axisIndex);
    } else {
      return Double.NaN;
    }
  }

  public void remove(final int index) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      coordinates.remove(index * axisCount);
    }
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final int axisCount = getAxisCount();
    if (axisIndex < axisCount) {
      if (index <= size()) {
        for (int i = coordinates.size(); i < (index + 1) * axisCount; i++) {
          coordinates.add(0.0);
        }
      }
      coordinates.set(index * axisCount + axisIndex, value);
    }
  }

  @Override
  public int size() {
    return coordinates.size() / axisCount;
  }
}
