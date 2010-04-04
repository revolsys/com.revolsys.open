package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;

public class CoordinatesListCoordinates implements Coordinates {
  private final CoordinatesList coordinates;

  private int index = 0;

  public CoordinatesListCoordinates(
    final CoordinatesList coordinates) {
    this.coordinates = coordinates;
  }

  public CoordinatesListCoordinates(
    final CoordinatesList coordinates,
    final int index) {
    this.coordinates = coordinates;
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public byte getNumAxis() {
    return coordinates.getNumAxis();
  }

  public double getValue(
    final int index) {
    if (index >= 0 && index < coordinates.getNumAxis()) {
      return coordinates.getValue(this.index, index);
    } else {
      return 0;
    }
  }

  public void setIndex(
    final int index) {
    this.index = index;
  }

  public void setValue(
    final int index,
    final double value) {
    if (index >= 0 && index < coordinates.getNumAxis()) {
      coordinates.setValue(this.index, index, value);
    }
  }

  public int size() {
    return coordinates.size();
  }

  @Override
  public String toString() {
    final byte numAxis = getNumAxis();
    if (numAxis > 0) {
      final StringBuffer s = new StringBuffer(
        String.valueOf(coordinates.getValue(index, 0)));
      for (int i = 1; i < numAxis; i++) {
        final Double ordinate = coordinates.getValue(index, i);
        s.append(',');
        s.append(ordinate);
      }
      return s.toString();
    } else {
      return "";
    }
  }
}
