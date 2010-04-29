package com.revolsys.gis.model.coordinates;

public abstract class AbstractCoordinates implements Coordinates {

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
    double x) {
    setValue(0, x);
  }

  public void setY(
    double y) {
    setValue(1, y);
  }

  public void setZ(
    double z) {
    setValue(2, z);
  }
}
