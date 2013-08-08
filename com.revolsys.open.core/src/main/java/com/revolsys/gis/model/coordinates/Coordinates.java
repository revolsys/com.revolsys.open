package com.revolsys.gis.model.coordinates;

public interface Coordinates extends Comparable<Coordinates>, Cloneable {
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

  double distance(Coordinates coordinates);

  boolean equals(double... coordinates);

  boolean equals2d(Coordinates coordinates);

  boolean equals3d(Coordinates coordinates);

  double[] getCoordinates();

  double getM();

  byte getNumAxis();

  long getTime();

  double getValue(int index);

  double getX();

  double getY();

  double getZ();

  void setM(double m);

  void setTime(long time);

  void setValue(final int index, final double value);

  void setX(double x);

  void setY(double y);

  void setZ(double z);
}
