package com.revolsys.gis.model.coordinates;

public interface Coordinates {
  byte getNumAxis();

  double getValue(
    int index);

  void setValue(
    final int index,
    final double value);
}
