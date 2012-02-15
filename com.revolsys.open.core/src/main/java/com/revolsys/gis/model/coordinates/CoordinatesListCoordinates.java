package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;

public class CoordinatesListCoordinates extends AbstractCoordinates {
  private final CoordinatesList coordinates;

  private int index = 0;

  public CoordinatesListCoordinates(final CoordinatesList coordinates) {
    this.coordinates = coordinates;
  }

  public CoordinatesListCoordinates(final CoordinatesList coordinates,
    final int index) {
    this.coordinates = coordinates;
    this.index = index;
  }

  @Override
  public CoordinatesListCoordinates clone() {
    return new CoordinatesListCoordinates(coordinates, index);
  }

  public int getIndex() {
    return index;
  }

  @Override
  public byte getNumAxis() {
    return coordinates.getNumAxis();
  }

  public double getValue(final int index) {
    if (index >= 0 && index < coordinates.getNumAxis()) {
      return coordinates.getValue(this.index, index);
    } else {
      return 0;
    }
  }

  public void next() {
    index++;
  }

  public void setIndex(final int index) {
    this.index = index;
  }

  public void setValue(final int index, final double value) {
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
      final double x = coordinates.getX(index);
      final StringBuffer s = new StringBuffer(String.valueOf(x));
      final double y = coordinates.getY(index);
      s.append(',');
      s.append(y);

      for (int i = 2; i < numAxis; i++) {
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
