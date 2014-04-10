package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.util.MathUtil;

public abstract class AbstractCoordinates implements Coordinates {

  /**
   * Calculate the angle in radians on a 2D plan between this point and another
   * point.
   * 
   * @param point The other point.
   * @return The angle in radians.
   */
  @Override
  public double angle2d(final Coordinates point) {
    return CoordinatesUtil.angle2d(this, point);
  }

  @Override
  public Coordinates clone() {
    try {
      return (Coordinates)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Coordinates cloneCoordinates() {
    return clone();
  }

  @Override
  public int compareTo(final Object other) {
    if (other instanceof Coordinates) {
      final Coordinates coordinates = (Coordinates)other;
      final double x = getX();
      final double y = getY();
      final double distance = MathUtil.distance(0, 0, x, y);

      final double otherX = coordinates.getX();
      final double otherY = coordinates.getY();
      final double otherDistance = MathUtil.distance(0, 0, otherX, otherY);
      final int distanceCompare = Double.compare(distance, otherDistance);
      if (distanceCompare == 0) {
        final int yCompare = Double.compare(y, otherY);
        return yCompare;
      } else {
        return distanceCompare;
      }
    } else {
      return -1;
    }
  }

  @Override
  public double distance(final Coordinates coordinates) {
    return CoordinatesUtil.distance(this, coordinates);
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
  public boolean equals2d(final Coordinates point) {
    return CoordinatesUtil.equals2d(this, point);
  }

  @Override
  public boolean equals3d(final Coordinates point) {
    return CoordinatesUtil.equals3d(this, point);
  }

  public BoundingBox getBoundingBox() {
    return new BoundingBox(this);
  }

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = new double[getNumAxis()];
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = getValue(i);
    }
    return coordinates;
  }

  @Override
  public double getM() {
    return getValue(3);
  }

  @Override
  public byte getNumAxis() {
    return 2;
  }

  public int getSrid() {
    return 0;
  }

  @Override
  public long getTime() {
    return (long)getM();
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
    return CoordinatesUtil.hashCode(this);
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
    final StringBuffer s = new StringBuffer();
    final int srid = getSrid();
    if (srid != 0) {
      s.append("SRID=");
      s.append(srid);
      s.append(';');
    }
    final byte numAxis = getNumAxis();
    final double[] coordinates = getCoordinates();
    if (numAxis > 0) {
      s.append("POINT(");
      for (int i = 0; i < numAxis; i++) {
        final Double ordinate = coordinates[i];
        if (i > 0) {
          s.append(' ');
        }
        s.append(MathUtil.toString(ordinate));
      }
      s.append(')');
    } else {
      s.append("POINT EMPTY");
    }
    return s.toString();
  }

}
