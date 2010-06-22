package com.revolsys.gis.model.coordinates.list;

import java.util.Iterator;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

public abstract class AbstractCoordinatesList implements CoordinatesList {

  private void append(
    final StringBuffer s,
    final int i,
    final byte numAxis) {
    s.append(getX(i));
    s.append(' ');
    s.append(getY(i));
    for (int j = 2; j < numAxis; j++) {
      final Double coordinate = getValue(i, j);
      s.append(' ');
      s.append(coordinate);
    }
  }

  public Iterator<Coordinates> iterator() {
    return new CoordinatesListCoordinatesIterator(this);
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
      final double x = getX(i);
      final double y = getY(i);
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
    coord.x = getX(index);
    coord.y = getY(index);
    if (getNumAxis() > 2) {
      coord.z = getZ(index);
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

  public Coordinates get(
    int i) {
    return new CoordinatesListCoordinates(this, i);
  }

  public double getX(
    final int index) {
    return getValue(index, 0);
  }

  public double getY(
    final int index) {
    return getValue(index, 1);
  }

  public double getZ(
    int index) {
    return getValue(index, 2);
  }

  public void setX(
    int index,
    double x) {
    setValue(index, 0, x);
  }

  public void setY(
    int index,
    double y) {
    setValue(index, 1, y);
  }

  public void setZ(
    int index,
    double z) {
    setValue(index, 2, z);
  }

  public void makePrecise(
    final CoordinatesPrecisionModel precisionModel) {
    InPlaceIterator iterator = new InPlaceIterator(this);
    for (Coordinates point : iterator) {
      precisionModel.makePrecise(point);
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

  public void setPoint(
    final int i,
    final Coordinates point) {
    setX(i, point.getX());
    setY(i, point.getY());
    if (getNumAxis() > 2) {
      setZ(i, point.getZ());
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
