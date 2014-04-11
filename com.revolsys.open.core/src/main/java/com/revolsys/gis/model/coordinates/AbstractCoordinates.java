package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.util.NumberUtil;
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
  public AbstractCoordinates clone() {
    try {
      return (AbstractCoordinates)super.clone();
    } catch (final CloneNotSupportedException e) {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public AbstractCoordinates cloneCoordinates() {
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

  /**
   * Computes the 2-dimensional Euclidean distance to another location.
   * The Z-ordinate is ignored.
   * 
   * @param c a point
   * @return the 2-dimensional Euclidean distance between the locations
   */
  @Override
  public double distance(final Coordinates point) {
    return CoordinatesUtil.distance(this, point);
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   * 
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  @Override
  public double distance3d(final Coordinates point) {
    return CoordinatesUtil.distance3d(this, point);
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

  /**
   *  Returns <code>true</code> if <code>other</code> has the same values for
   *  the x and y ordinates.
   *  Since Coordinates are 2.5D, this routine ignores the z value when making the comparison.
   *
   *@param  other  a <code>Coordinate</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for the x and y ordinates.
   */
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

  /**
   * Tests if another coordinate has the same values for the X and Y ordinates.
   * The Z ordinate is ignored.
   *
   *@param other a <code>Coordinate</code> with which to do the 2D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X and Y.
   */
  @Override
  public boolean equals2d(final Coordinates c, final double tolerance) {
    if (!NumberUtil.equalsWithTolerance(this.getX(), c.getX(), tolerance)) {
      return false;
    }
    if (!NumberUtil.equalsWithTolerance(this.getY(), c.getY(), tolerance)) {
      return false;
    }
    return true;
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
    return getValue(X);
  }

  @Override
  public double getY() {
    return getValue(Y);
  }

  @Override
  public double getZ() {
    return getValue(Z);
  }

  @Override
  public int hashCode() {
    return CoordinatesUtil.hashCode(this);
  }

  /**
   *  Sets this <code>Coordinate</code>s (x,y,z) values to that of <code>other</code>.
   *
   *@param  other  the <code>Coordinate</code> to copy
   */
  @Override
  public void setCoordinate(final Coordinates other) {
    for (int i = 0; i < getNumAxis(); i++) {
      final double value = other.getValue(i);
      setValue(i, value);
    }
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
    setValue(X, x);
  }

  @Override
  public void setY(final double y) {
    setValue(Y, y);
  }

  @Override
  public void setZ(final double z) {
    setValue(Z, z);
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
