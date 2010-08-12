package com.revolsys.gis.model.coordinates;

public interface CoordinatesPrecisionModel {
  Coordinates getPreciseCoordinates(
    Coordinates coordinates);

  double getScaleXY();

  double getScaleZ();

  void makePrecise(
    Coordinates coordinates);
}
