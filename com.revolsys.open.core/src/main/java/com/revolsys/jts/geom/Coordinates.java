package com.revolsys.jts.geom;

import java.io.Serializable;

public interface Coordinates extends Comparable<Object>, Cloneable,
  Serializable {

  /**
   * The value used to indicate a null or missing ordinate value.
   * In particular, used for the value of ordinates for dimensions 
   * greater than the defined dimension of a coordinate.
   */
  double NULL_ORDINATE = Double.NaN;

  /**
   * Standard ordinate index values
   */
  int X = 0;

  int Y = 1;

  int Z = 2;

  /**
   * Calculate the counter clockwise angle in radians of the vector from this
   * point to another point. The angle is relative to the positive x-axis
   * relative to the positive X-axis. The angle will be in the range -PI -> PI
   * where negative values have a clockwise orientation.
   * 
   * @return The angle in radians.
   */
  double angle2d(Coordinates other);

  Object clone();

  Coordinates cloneCoordinates();

  /**
   *  Compares this {@link Coordinate} with the specified {@link Coordinate} for order.
   *  This method ignores the z value when making the comparison.
   *  Returns:
   *  <UL>
   *    <LI> -1 : this.x < other.x || ((this.x == other.x) && (this.y <
   *    other.y))
   *    <LI> 0 : this.x == other.x && this.y = other.y
   *    <LI> 1 : this.x > other.x || ((this.x == other.x) && (this.y > other.y))
   *
   *  </UL>
   *  Note: This method assumes that ordinate values
   * are valid numbers.  NaN values are not handled correctly.
   *
   *@param  o  the <code>Coordinate</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    -1, zero, or 1 as this <code>Coordinate</code>
   *      is less than, equal to, or greater than the specified <code>Coordinate</code>
   */
  @Override
  int compareTo(Object o);

  /**
   * Computes the 2-dimensional Euclidean distance to another location.
   * The Z-ordinate is ignored.
   * 
   * @param c a point
   * @return the 2-dimensional Euclidean distance between the locations
   */
  double distance(Coordinates point);

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   * 
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  double distance3d(Coordinates c);

  boolean equals(double... coordinates);

  /**
   *  Returns whether the planar projections of the two <code>Coordinate</code>s
   *  are equal.
   *
   *@param  other  a <code>Coordinate</code> with which to do the 2D comparison.
   *@return        <code>true</code> if the x- and y-coordinates are equal; the
   *      z-coordinates do not have to be equal.
   */
  boolean equals2d(Coordinates point);

  /**
   * Tests if another coordinate has the same values for the X and Y ordinates.
   * The Z ordinate is ignored.
   *
   *@param other a <code>Coordinate</code> with which to do the 2D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X and Y.
   */
  boolean equals2d(Coordinates point, double tolerance);

  /**
   * Tests if another coordinate has the same values for the X, Y and Z ordinates.
   *
   *@param point a <code>Coordinate</code> with which to do the 3D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X, Y and Z.
   */
  boolean equals3d(Coordinates point);

  double[] getCoordinates();

  double getM();

  byte getNumAxis();

  long getTime();

  /**
   * Gets the ordinate value for the given index.
   * The supported values for the index are 
   * {@link #X}, {@link #Y}, and {@link #Z}.
   * 
   * @param axisIndex the ordinate index
   * @return the value of the ordinate
   * @throws IllegalArgumentException if the index is not valid
   */
  double getValue(int axisIndex);

  double getX();

  double getY();

  double getZ();

  /**
   *  Sets this <code>Coordinate</code>s (x,y,z) values to that of <code>other</code>.
   *
   *@param  other  the <code>Coordinate</code> to copy
   */
  void setCoordinate(Coordinates other);

  void setM(double m);

  void setTime(long time);

  /**
   * Sets the ordinate for the given index
   * to a given value.
   * The supported values for the index are 
   * {@link #X}, {@link #Y}, and {@link #Z}.
   * 
   * @param axisIndex the ordinate index
   * @param value the value to set
   * @throws IllegalArgumentException if the index is not valid
   */
  void setValue(int axisIndex, double value);

  void setX(double x);

  void setY(double y);

  void setZ(double z);
}
