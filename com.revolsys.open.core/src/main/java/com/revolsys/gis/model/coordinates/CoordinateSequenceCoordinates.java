package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.CoordinatesList;

public class CoordinateSequenceCoordinates extends AbstractCoordinates {
  private final CoordinatesList coordinates;

  private int index = 0;

  public CoordinateSequenceCoordinates(final CoordinatesList coordinates) {
    this.coordinates = coordinates;
  }

  public CoordinateSequenceCoordinates(final CoordinatesList coordinates,
    final int index) {
    this.coordinates = coordinates;
    this.index = index;
  }

  @Override
  public CoordinateSequenceCoordinates cloneCoordinates() {
    return new CoordinateSequenceCoordinates(coordinates, index);
  }

  public int getIndex() {
    return index;
  }

  @Override
  public int getAxisCount() {
    return (byte)coordinates.getAxisCount();
  }

  @Override
  public double getValue(final int index) {
    if (index >= 0 && index < getAxisCount()) {
      return coordinates.getValue(this.index, index);
    } else {
      return 0;
    }
  }

  public void setIndex(final int index) {
    this.index = index;
  }

  @Override
  public void setValue(final int index, final double value) {
    if (index >= 0 && index < getAxisCount()) {
      coordinates.setValue(this.index, index, value);
    }
  }

  public int size() {
    return coordinates.size();
  }

  @Override
  public String toString() {
    return coordinates.getCoordinate(index).toString();
  }

}
