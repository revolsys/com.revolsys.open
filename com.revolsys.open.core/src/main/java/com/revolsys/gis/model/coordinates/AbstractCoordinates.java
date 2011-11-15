package com.revolsys.gis.model.coordinates;

import com.revolsys.util.MathUtil;

public abstract class AbstractCoordinates implements Coordinates {

  public static int hashCode(final double d) {
    final long f = Double.doubleToLongBits(d);
    return (int)(f ^ (f >>> 32));
  }

  /**
   * Calculate the angle in radians on a 2D plan between this point and another
   * point.
   * 
   * @param other The other point.
   * @return The angle in radians.
   */
  public double angle2d(final Coordinates other) {
    final double dx = other.getX() - getX();
    final double dy = other.getY() - getY();
    return Math.atan2(dy, dx);
  }

  @Override
  public Coordinates clone() {
    try {
      return (Coordinates)super.clone();
    } catch (CloneNotSupportedException e) {
      throw new UnsupportedOperationException();
    }
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

  @Override
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
    final double[] coordinates = new double[getNumAxis()];
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = getValue(i);
    }
    return coordinates;
  }

  public double getM() {
    return getValue(3);
  }

  public byte getNumAxis() {
    return 2;
  }

  public long getTime() {
    return (long)getM();
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

  @Override
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

  public void setX(final double x) {
    setValue(0, x);
  }

  public void setY(final double y) {
    setValue(1, y);
  }

  public void setZ(final double z) {
    setValue(2, z);
  }

  @Override
  public String toString() {
    final byte numAxis = getNumAxis();
    final double[] coordinates = getCoordinates();
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
