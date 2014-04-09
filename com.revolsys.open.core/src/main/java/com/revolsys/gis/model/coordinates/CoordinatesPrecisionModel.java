package com.revolsys.gis.model.coordinates;

public interface CoordinatesPrecisionModel {
  Coordinates getPreciseCoordinates(Coordinates coordinates);

  double getResolutionXy();

  double getResolutionZ();

  double getScaleXY();

  double getScaleZ();

  boolean isFloating();

  void makePrecise(Coordinates coordinates);

  double makeXyPrecise(final double value);

  double makeZPrecise(final double value);

}
