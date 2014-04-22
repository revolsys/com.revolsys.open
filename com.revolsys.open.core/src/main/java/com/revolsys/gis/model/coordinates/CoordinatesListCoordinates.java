package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.CoordinatesList;

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
  public DoubleCoordinates cloneCoordinates() {
    return new DoubleCoordinates(this);
  }

  public int getIndex() {
    return index;
  }

  @Override
  public int getAxisCount() {
    return coordinates.getAxisCount();
  }

  @Override
  public double getValue(final int index) {
    if (index >= 0 && index < coordinates.getAxisCount()) {
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

  @Override
  public void setValue(final int index, final double value) {
    if (index >= 0 && index < coordinates.getAxisCount()) {
      coordinates.setValue(this.index, index, value);
    }
  }

  public int size() {
    return coordinates.size();
  }

  @Override
  public String toString() {
    final int axisCount = getAxisCount();
    if (axisCount > 0) {
      final double x = coordinates.getX(index);
      final StringBuffer s = new StringBuffer(String.valueOf(x));
      final double y = coordinates.getY(index);
      s.append(',');
      s.append(y);

      for (int i = 2; i < axisCount; i++) {
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
