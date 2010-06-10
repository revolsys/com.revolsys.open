package com.revolsys.gis.model.coordinates;

public interface CoordinatesPrecisionModel {
  Coordinates getPreciseCoordinates(
    Coordinates coordinates);

  void makePrecise(
    Coordinates coordinates);
}
