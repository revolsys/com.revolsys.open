package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.impl.AbstractPoint;

public class CoordinatesListCoordinates extends AbstractPoint {
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
  public int getAxisCount() {
    return coordinates.getAxisCount();
  }

  @Override
  public double getCoordinate(final int index) {
    if (index >= 0 && index < coordinates.getAxisCount()) {
      return coordinates.getValue(this.index, index);
    } else {
      return 0;
    }
  }

  public int getIndex() {
    return index;
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
