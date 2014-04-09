package com.revolsys.gis.model.coordinates;

import java.io.Serializable;

public class SimpleCoordinatesPrecisionModel implements
  CoordinatesPrecisionModel, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -1119410280750540081L;

  public static double makePrecise(final double value, final double scale) {
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

  public SimpleCoordinatesPrecisionModel(final double scale) {
    this.scaleXY = scale;
    this.scaleZ = scale;
  }

  public SimpleCoordinatesPrecisionModel(final double scaleXY,
    final double scaleZ) {
    this.scaleXY = scaleXY;
    this.scaleZ = scaleZ;
  }

  @Override
  public Coordinates getPreciseCoordinates(final Coordinates coordinates) {
    final Coordinates newCoordinates = new DoubleCoordinates(coordinates);
    makePrecise(newCoordinates);
    return newCoordinates;
  }

  @Override
  public double getResolutionXy() {
    if (scaleXY <= 0) {
      return 0;
    } else {
      return 1 / scaleXY;
    }
  }

  @Override
  public double getResolutionZ() {
    if (scaleZ <= 0) {
      return 0;
    } else {
      return 1 / scaleZ;
    }
  }

  @Override
  public double getScaleXY() {
    return scaleXY;
  }

  @Override
  public double getScaleZ() {
    return scaleZ;
  }

  @Override
  public boolean isFloating() {
    return scaleXY <= 0 && scaleZ <= 0;
  }

  @Override
  public void makePrecise(final Coordinates coordinates) {
    if (scaleXY > 0) {
      final double x = coordinates.getX();
      final double newX = makeXyPrecise(x);
      coordinates.setX(newX);

      final double y = coordinates.getY();
      final double newY = makeXyPrecise(y);
      coordinates.setY(newY);
    }
    if (scaleZ > 0) {
      if (coordinates.getNumAxis() > 2) {
        final double z = coordinates.getZ();
        final double newZ = makeZPrecise(z);
        coordinates.setZ(newZ);
      }
    }
  }

  @Override
  public double makeXyPrecise(final double value) {
    return makePrecise(value, scaleXY);
  }

  @Override
  public double makeZPrecise(final double value) {
    return makePrecise(value, scaleZ);
  }

  public void setScaleXY(final double scaleXY) {
    this.scaleXY = scaleXY;
  }

  public void setScaleZ(final double scaleZ) {
    this.scaleZ = scaleZ;
  }

  @Override
  public String toString() {
    if (isFloating()) {
      return "scale(xyz=floating)";
    } else if (scaleZ <= 0) {
      return "scale(xy=" + scaleXY + ",z=floating)";
    } else if (scaleXY <= 0) {
      return "scale(xy=floating,z=" + scaleZ + ")";
    } else {
      return "scale(xy=" + scaleXY + ",z=" + scaleZ + "]";
    }
  }
}
