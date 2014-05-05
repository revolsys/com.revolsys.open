package com.revolsys.gis.model.coordinates;

import java.io.Serializable;

import com.revolsys.jts.geom.Coordinates;

public class SimpleCoordinatesPrecisionModel implements
  CoordinatesPrecisionModel, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -1119410280750540081L;

  public static double makePrecise(final double value, final double scale) {
    if (scale <= 0) {
      return value;
    } else if (Double.isInfinite(value)) {
      return value;
    } else if (Double.isNaN(value)) {
      return value;
    } else {
      // final BigDecimal scaleDecimal = new BigDecimal(scale);
      // final double preciseValue = new
      // BigDecimal(value).multiply(scaleDecimal)
      // .setScale(0, RoundingMode.HALF_UP)
      // .divide(scaleDecimal)
      // .doubleValue();

      final double multiple = value * scale;
      // if (multiple < 0) {
      // multiple -= 0.00001;
      // } else {
      // multiple += 0.00001;
      // }
      final long scaledValue = Math.round(multiple);
      final double preciseValue = scaledValue / scale;
      return preciseValue;
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
  public Coordinates getPreciseCoordinates(final Coordinates point) {
    final double[] coordinates = point.getCoordinates();
    if (scaleXY > 0) {
      final double x = coordinates[0];
      final double newX = makeXyPrecise(x);
      coordinates[0] = newX;

      final double y = coordinates[1];
      final double newY = makeXyPrecise(y);
      coordinates[1] = newY;
    }
    if (scaleZ > 0) {
      if (coordinates.length > 2) {
        final double z = coordinates[2];
        final double newZ = makeZPrecise(z);
        coordinates[2] = newZ;
      }
    }
    return new DoubleCoordinates(coordinates);
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
  public void makePrecise(final int axisCount, final double... coordinates) {
    final boolean hasXyScale = scaleXY > 0;
    final boolean hasZScale = scaleZ > 0;
    if (hasXyScale || hasZScale) {
      for (int i = 0; i < coordinates.length; i++) {
        final double value = coordinates[i];
        final int axisIndex = i % axisCount;
        if (axisIndex < 2) {
          if (hasXyScale) {
            coordinates[i] = makeXyPrecise(value);
          }
        } else if (axisIndex == 2) {
          if (hasZScale) {
            coordinates[i] = makeZPrecise(value);
          }
        }
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
