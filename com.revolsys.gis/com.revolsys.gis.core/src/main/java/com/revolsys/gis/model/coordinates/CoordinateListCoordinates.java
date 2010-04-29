package com.revolsys.gis.model.coordinates;

import com.vividsolutions.jts.geom.CoordinateSequence;

public class CoordinateListCoordinates extends AbstractCoordinates {
  private final CoordinateSequence coordinates;

  private int index = 0;

  public CoordinateListCoordinates(
    final CoordinateSequence coordinates) {
    this.coordinates = coordinates;
  }

  public int getIndex() {
    return index;
  }

  public byte getNumAxis() {
    return (byte)coordinates.getDimension();
  }

  public double getValue(
    final int index) {
    if (index >= 0 && index < getNumAxis()) {
      return coordinates.getOrdinate(this.index, index);
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
    if (index >= 0 && index < getNumAxis()) {
      coordinates.setOrdinate(this.index, index, value);
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
