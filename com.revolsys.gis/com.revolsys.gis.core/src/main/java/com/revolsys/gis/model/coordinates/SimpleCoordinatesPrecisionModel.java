package com.revolsys.gis.model.coordinates;

public class SimpleCoordinatesPrecisionModel implements
  CoordinatesPrecisionModel {
  public static SimpleCoordinatesPrecisionModel FLOATING = new SimpleCoordinatesPrecisionModel();

  public static double makePrecise(
    final double value,
    final double scale) {
    if (scale <= 0) {
      return value;
    } else if (Double.isNaN(value)) {
      return value;
    } else {
      return Math.round(value * scale) / scale;
    }
  }

  private double scaleXY = 0;

  private double scaleZ = 0;

  public SimpleCoordinatesPrecisionModel() {
  }

  public SimpleCoordinatesPrecisionModel(
    final double scale) {
    this.scaleXY = scale;
    this.scaleZ = scale;
  }

  public SimpleCoordinatesPrecisionModel(
    final double scaleXY,
    final double scaleZ) {
    this.scaleXY = scaleXY;
    this.scaleZ = scaleZ;
  }

  public Coordinates getPreciseCoordinates(
    final Coordinates coordinates) {
    final Coordinates newCoordinates = new DoubleCoordinates(coordinates);
    makePrecise(newCoordinates);
    return newCoordinates;
  }

  public double getScaleXY() {
    return scaleXY;
  }

  public double getScaleZ() {
    return scaleZ;
  }

  public void makePrecise(
    final Coordinates coordinates) {
    if (scaleXY >0) {
      final double x = coordinates.getX();
      final double newX = makePrecise(x, scaleXY);
      coordinates.setX(newX);

      final double y = coordinates.getY();
      final double newY = makePrecise(y, scaleXY);
      coordinates.setY(newY);
    }
    if (scaleZ > 0) {
      if (coordinates.getNumAxis() > 2) {
        final double z = coordinates.getZ();
        final double newZ = makePrecise(z, scaleZ);
        coordinates.setZ(newZ);
      }
    }
  }

  public void setScaleXY(
    final double scaleXY) {
    this.scaleXY = scaleXY;
  }

  public void setScaleZ(
    final double scaleZ) {
    this.scaleZ = scaleZ;
  }
  @Override
  public String toString() {
    if (isFloating()) {
      return "floating";
    } else if (scaleZ > 0) {
      return "fixed(" + scaleXY + ")";
    } else {
      return "fixed[" + scaleXY + "," + scaleZ + "]";
    }
  }

  public boolean isFloating() {
    return scaleXY <=0;
  }
}
