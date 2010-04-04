package com.revolsys.gis.model.coordinates.list;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.PrecisionModel;

public abstract class AbstractCoordinatesList implements CoordinatesList {

  private void append(
    final StringBuffer s,
    final int i,
    final byte numAxis) {
    s.append(getValue(i, 0));
    for (int j = 1; j < numAxis; j++) {
      final Double coordinate = getValue(i, j);
      s.append(' ');
      s.append(coordinate);
    }
  }

  @Override
  public abstract CoordinatesList clone();

  public void copy(
    final int sourceIndex,
    final CoordinatesList target,
    final int targetIndex,
    final int numAxis,
    final int count) {
    for (int i = 0; i < count; i++) {
      for (int j = 0; j < numAxis; j++) {
        final double coordinate = getValue(sourceIndex + i, j);
        target.setValue(targetIndex + i, j, coordinate);
      }
    }
  }

  public CoordinatesList create(
    final int length,
    final int numAxis) {
    return new DoubleCoordinatesList(length, numAxis);
  }

  public Envelope expandEnvelope(
    final Envelope env) {
    for (int i = 0; i < size(); i++) {
      final double x = getValue(i, 0);
      final double y = getValue(i, 1);
      env.expandToInclude(x, y);
    }
    return env;
  }

  public Coordinate getCoordinate(
    final int i) {
    final Coordinate coordinate = new Coordinate();
    getCoordinate(i, coordinate);
    return coordinate;
  }

  public void getCoordinate(
    final int index,
    final Coordinate coord) {
    coord.x = getValue(index, 0);
    coord.y = getValue(index, 1);
    if (getNumAxis() > 2) {
      coord.z = getValue(index, 2);
    }
  }

  public Coordinate getCoordinateCopy(
    final int i) {
    final Coordinate coordinate = new Coordinate();
    getCoordinate(i, coordinate);
    return coordinate;
  }

  public double[] getCoordinates() {
    final int size = size();
    final byte numAxis = getNumAxis();
    final double[] coordinates = new double[size * numAxis];
    final int k = 0;
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < numAxis; j++) {
        final double coordinate = getValue(i, j);
        coordinates[k] = coordinate;
      }
    }
    return coordinates;
  }

  public int getDimension() {
    return getNumAxis();
  }

  public double getOrdinate(
    final int index,
    final int axisIndex) {
    return getValue(index, axisIndex);
  }

  public double getX(
    final int index) {
    return getValue(index, 0);
  }

  public double getY(
    final int index) {
    return getValue(index, 1);
  }

  public void makePrecise(
    final PrecisionModel precisionModel) {
    if (precisionModel.getType() == PrecisionModel.FIXED) {
      final byte numAxis = getNumAxis();
      for (int i = 0; i < size(); i++) {
        for (int j = 0; j < numAxis; j++) {
          final double ordinate = getValue(i, j);
          final double preciseOrdinate = precisionModel.makePrecise(ordinate);
          setValue(i, j, preciseOrdinate);
        }
      }
    }
  }

  public CoordinatesList reverse() {
    return new ReverseCoordinatesList(this);
  }

  public void setCoordinate(
    final int i,
    final Coordinate coordinate) {
    setValue(i, 0, coordinate.x);
    setValue(i, 1, coordinate.y);
    if (getNumAxis() > 2) {
      setValue(i, 2, coordinate.z);
    }
  }

  public void setOrdinate(
    final int index,
    final int axisIndex,
    final double value) {
    setValue(index, axisIndex, value);
  }

  public CoordinatesList subList(
    final int index,
    final int count) {
    return subList(count, index, count);
  }

  public CoordinatesList subList(
    final int length,
    final int index,
    final int count) {
    return subList(length, index, 0, count);
  }

  public CoordinatesList subList(
    final int length,
    final int sourceIndex,
    final int targetIndex,
    final int count) {
    final int numAxis = getNumAxis();
    final CoordinatesList target = create(length, numAxis);
    copy(sourceIndex, target, targetIndex, numAxis, count);
    return target;
  }

  public Coordinate[] toCoordinateArray() {
    final Coordinate[] coordinateArray = new Coordinate[size()];
    for (int i = 0; i < coordinateArray.length; i++) {
      coordinateArray[i] = getCoordinateCopy(i);
    }
    return coordinateArray;
  }

  @Override
  public String toString() {
    final byte numAxis = getNumAxis();
    if (numAxis > 0) {
      final StringBuffer s = new StringBuffer("LINESTRING(");
      append(s, 0, numAxis);
      for (int i = 1; i < size(); i++) {
        s.append(',');
        append(s, i, numAxis);
      }
      s.append(')');
      return s.toString();
    } else {
      return "LINESTRING EMPTY";
    }
  }
}
