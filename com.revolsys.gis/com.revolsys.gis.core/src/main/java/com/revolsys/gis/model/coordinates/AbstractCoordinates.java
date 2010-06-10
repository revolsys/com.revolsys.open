package com.revolsys.gis.model.coordinates;

public abstract class AbstractCoordinates implements Coordinates {

  public double distance(
    final Coordinates coordinates) {
    return CoordinatesUtil.distance(this, coordinates);
  }

  @Override
  public boolean equals(
    final Object other) {
    if (other instanceof Coordinates) {
      final Coordinates coordinates = (Coordinates)other;
      return equals2d(coordinates);
    } else {
      return false;
    }
  }

  public boolean equals2d(
    final Coordinates coordinates) {
    if (getX() == coordinates.getX()) {
      if (getY() == coordinates.getY()) {
        return true;
      }
    }
    return false;
  }

  public double[] getCoordinates() {
    double[] coordinates = new double[getNumAxis()];
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = getValue(i);
    }
    return coordinates;
  }

  public byte getNumAxis() {
    return 0;
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

  public void setX(
    final double x) {
    setValue(0, x);
  }

  public void setY(
    final double y) {
    setValue(1, y);
  }

  public void setZ(
    final double z) {
    setValue(2, z);
  }

  @Override
  public abstract Coordinates clone();
}
