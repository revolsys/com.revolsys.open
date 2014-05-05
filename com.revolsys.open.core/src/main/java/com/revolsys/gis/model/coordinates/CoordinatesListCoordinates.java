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

  @Override
  public int getAxisCount() {
    return coordinates.getAxisCount();
  }

  public int getIndex() {
    return index;
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

  public int size() {
    return coordinates.size();
  }

}
