package com.revolsys.gis.model.coordinates;

public class SimpleCoordinatesPrecisionModel implements
  CoordinatesPrecisionModel {
  public static SimpleCoordinatesPrecisionModel FLOATING = new SimpleCoordinatesPrecisionModel();

  public static double makePrecise(
    final double value,
    final double scale) {
    if (Double.isNaN(scale)) {
      return value;
    } else if (Double.isNaN(value)) {
      return value;
    } else {
      return Math.round(value * scale) / scale;
    }
  }

  private double scaleXY = Double.NaN;

  private double scaleZ = Double.NaN;

  public SimpleCoordinatesPrecisionModel() {
  }

  public SimpleCoordinatesPrecisionModel(
    double scale) {
    this.scaleXY = scale;
    this.scaleZ = scale;
  }

  public SimpleCoordinatesPrecisionModel(
    double scaleXY,
    double scaleZ) {
    this.scaleXY = scaleXY;
    this.scaleZ = scaleZ;
  }

  public Coordinates getPreciseCoordinates(
    final Coordinates coordinates) {
    Coordinates newCoordinates = new DoubleCoordinates(coordinates);
    makePrecise(newCoordinates);
    return newCoordinates;
  }

  public void makePrecise(
    final Coordinates coordinates) {
    if (!Double.isNaN(scaleXY)) {
      final double x = coordinates.getX();
      final double newX = makePrecise(x, scaleXY);
      coordinates.setX(newX);

      final double y = coordinates.getY();
      final double newY = makePrecise(y, scaleXY);
      coordinates.setY(newY);
    }
    if (!Double.isNaN(scaleZ)) {
      if (coordinates.getNumAxis() > 2) {
        final double z = coordinates.getZ();
        final double newZ = makePrecise(z, scaleZ);
        coordinates.setZ(newZ);
      }
    }
  }

  public double getScaleXY() {
    return scaleXY;
  }

  public void setScaleXY(
    double scaleXY) {
    this.scaleXY = scaleXY;
  }

  public double getScaleZ() {
    return scaleZ;
  }

  public void setScaleZ(
    double scaleZ) {
    this.scaleZ = scaleZ;
  }

  @Override
  public String toString() {
    if (Double.isNaN(scaleXY)) {
      return "floating";
    } else if (Double.isNaN(scaleZ)) {
      return "fixed(" + scaleXY + ")";
    } else {
      return "fixed(" + scaleXY + "," + scaleZ + ")";
    }
  }
}
