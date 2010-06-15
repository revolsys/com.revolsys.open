package com.revolsys.gis.model.coordinates;

public interface Coordinates extends Comparable<Coordinates> {
  double angle2d(
    Coordinates other);

  Coordinates clone();

  double distance(
    Coordinates coordinates);

  boolean equals(
    double... coordinates);

  boolean equals2d(
    Coordinates coordinates);

  double[] getCoordinates();

  byte getNumAxis();

  double getValue(
    int index);

  double getX();

  double getY();

  double getZ();

  void setValue(
    final int index,
    final double value);

  void setX(
    double x);

  void setY(
    double y);

  void setZ(
    double z);
}
