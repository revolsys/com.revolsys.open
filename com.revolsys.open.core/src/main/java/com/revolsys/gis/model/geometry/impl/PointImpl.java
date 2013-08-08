package com.revolsys.gis.model.geometry.impl;

import java.util.Collections;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Dimension;

public class PointImpl extends GeometryImpl implements Point {

  public static int hashCode(final double d) {
    final long f = Double.doubleToLongBits(d);
    return (int)(f ^ (f >>> 32));
  }

  protected final double[] coordinates;

  protected PointImpl(final GeometryFactoryImpl geometryFactory,
    final Coordinates coordinates) {
    super(geometryFactory);
    final int numAxis = geometryFactory.getNumAxis();
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

  protected PointImpl(final GeometryFactoryImpl geometryFactory,
    final double[] coordinates) {
    super(geometryFactory);
    final int numAxis = geometryFactory.getNumAxis();
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
  public double angle2d(final Coordinates other) {
    final double dx = other.getX() - getX();
    final double dy = other.getY() - getY();
    return Math.atan2(dy, dx);
  }

  @Override
  public PointImpl clone() {
    return (PointImpl)super.clone();
  }

  @Override
  public Coordinates cloneCoordinates() {
    return clone();
  }

  @Override
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

  @Override
  public double distance(final Coordinates coordinates) {
    return CoordinatesUtil.distance(this, coordinates);
  }

  @Override
  public double distance(final Point point) {
    return distance((Coordinates)point);
  }

  @Override
  public boolean equals(final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double coordinate = coordinates[i];
      if (coordinate != getValue(i)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Coordinates) {
      final Coordinates coordinates = (Coordinates)other;
      return equals2d(coordinates);
    } else {
      return false;
    }
  }

  @Override
  public boolean equals2d(final Coordinates coordinates) {
    if (getX() == coordinates.getX()) {
      if (getY() == coordinates.getY()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals3d(final Coordinates coordinates) {
    if (EqualsRegistry.equal(getX(), coordinates.getX())) {
      if (EqualsRegistry.equal(getY(), coordinates.getY())) {
        if (EqualsRegistry.equal(getZ(), coordinates.getZ())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[this.coordinates.length];
    System.arraycopy(this.coordinates, 0, coordinates, 0,
      this.coordinates.length);
    return coordinates;
  }

  @Override
  public List<CoordinatesList> getCoordinatesLists() {
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(
      getNumAxis(), getCoordinates());
    return Collections.singletonList(coordinatesList);
  }

  @Override
  public int getDimension() {
    return 0;
  }

  @Override
  public Point getFirstPoint() {
    return this;
  }

  @Override
  public double getM() {
    return getValue(3);
  }

  @Override
  public long getTime() {
    return (long)getM();
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
  public double getX() {
    return getValue(0);
  }

  @Override
  public double getY() {
    return getValue(1);
  }

  @Override
  public double getZ() {
    return getValue(2);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 37 * result + hashCode(getX());
    result = 37 * result + hashCode(getY());
    return result;
  }

  @Override
  public boolean isEmpty() {
    return coordinates.length == 0;
  }

  @Override
  public void setM(final double m) {
    setValue(3, m);
  }

  @Override
  public void setTime(final long time) {
    setM(time);
  }

  @Override
  public void setValue(final int index, final double value) {
    if (index >= 0 && index < getNumAxis()) {
      coordinates[index] = value;
    }
  }

  @Override
  public void setX(final double x) {
    setValue(0, x);
  }

  @Override
  public void setY(final double y) {
    setValue(1, y);
  }

  @Override
  public void setZ(final double z) {
    setValue(2, z);
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
