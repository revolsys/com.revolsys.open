package com.revolsys.gis.model.geometry.impl;

import java.util.Collections;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Dimension;

public class PointImpl extends GeometryImpl implements Point {

  public static int hashCode(final double d) {
    final long f = Double.doubleToLongBits(d);
    return (int)(f ^ (f >>> 32));
  }

  protected final double[] coordinates;

  protected PointImpl(GeometryFactoryImpl geometryFactory,
    final double[] coordinates) {
    super(geometryFactory);
    int numAxis = geometryFactory.getNumAxis();
    this.coordinates = new double[numAxis];
    for (int i = 0; i < numAxis; i++) {
      double value;
      if (i < coordinates.length) {
        value = coordinates[i];
      } else {
        value = Double.NaN;
      }
      this.coordinates[i] = value;
    }
  }

  @Override
  public int getDimension() {
    return 0;
  }

  protected PointImpl(GeometryFactoryImpl geometryFactory,
    final Coordinates coordinates) {
    super(geometryFactory);
    int numAxis = geometryFactory.getNumAxis();
    this.coordinates = new double[numAxis];
    for (int i = 0; i < numAxis; i++) {
      double value;
      if (i < coordinates.getNumAxis()) {
        value = coordinates.getValue(i);
      } else {
        value = Double.NaN;
      }
      this.coordinates[i] = value;
    }
  }

  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  public boolean isEmpty() {
    return coordinates.length == 0;
  }

  @Override
  public List<CoordinatesList> getCoordinatesLists() {
    CoordinatesList coordinatesList = new DoubleCoordinatesList(getNumAxis(),
      getCoordinates());
    return Collections.singletonList(coordinatesList);
  }

  public double angle2d(final Coordinates other) {
    final double dx = other.getX() - getX();
    final double dy = other.getY() - getY();
    return Math.atan2(dy, dx);
  }

  public PointImpl clone() {
    return (PointImpl)super.clone();
  }

  @Override
  public Coordinates cloneCoordinates() {
    return clone();
  }

  public int compareTo(final Coordinates other) {
    final double x = getX();
    final double y = getY();
    final double distance = MathUtil.distance(0, 0, x, y);

    final double otherX = other.getX();
    final double otherY = other.getY();
    final double otherDistance = MathUtil.distance(0, 0, otherX, otherY);
    final int distanceCompare = Double.compare(distance, otherDistance);
    if (distanceCompare == 0) {
      final int yCompare = Double.compare(y, otherY);
      return yCompare;
    } else {
      return distanceCompare;
    }
  }

  public double distance(final Coordinates coordinates) {
    return CoordinatesUtil.distance(this, coordinates);
  }

  public boolean equals(final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double coordinate = coordinates[i];
      if (coordinate != getValue(i)) {
        return false;
      }
    }
    return true;
  }

  public boolean equals(final Object other) {
    if (other instanceof Coordinates) {
      final Coordinates coordinates = (Coordinates)other;
      return equals2d(coordinates);
    } else {
      return false;
    }
  }

  public boolean equals2d(final Coordinates coordinates) {
    if (getX() == coordinates.getX()) {
      if (getY() == coordinates.getY()) {
        return true;
      }
    }
    return false;
  }

  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0,
      this.coordinates.length);
    return coordinates;
  }

  public double getM() {
    return getValue(3);
  }

  public long getTime() {
    return (long)getM();
  }

  public double getValue(final int index) {
    if (index >= 0 && index < getNumAxis()) {
      return coordinates[index];
    } else {
      return Double.NaN;
    }
  }

  public double getX() {
    return getValue(0);
  }

  public double getY() {
    return getValue(1);
  }

  public double getZ() {
    return getValue(2);
  }

  public int hashCode() {
    int result = 17;
    result = 37 * result + hashCode(getX());
    result = 37 * result + hashCode(getY());
    return result;
  }

  public void setM(final double m) {
    setValue(3, m);
  }

  public void setTime(final long time) {
    setM(time);
  }

  public void setValue(final int index, final double value) {
    if (index >= 0 && index < getNumAxis()) {
      coordinates[index] = value;
    }
  }

  public void setX(final double x) {
    setValue(0, x);
  }

  public void setY(final double y) {
    setValue(1, y);
  }

  public void setZ(final double z) {
    setValue(2, z);
  }

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
