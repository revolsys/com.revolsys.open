package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.Coordinate;

public class CoordinateCoordinates extends AbstractCoordinates {
  private Coordinate coordinate;

  public CoordinateCoordinates(final Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  @Override
  public CoordinateCoordinates cloneCoordinates() {
    final Coordinate newCoordinate = new Coordinate(coordinate);
    return new CoordinateCoordinates(newCoordinate);
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  @Override
  public byte getNumAxis() {
    if (Double.isNaN(coordinate.z)) {
      return 2;
    } else {
      return 3;
    }
  }

  @Override
  public double getValue(final int index) {
    switch (index) {
      case 0:
        return coordinate.x;
      case 1:
        return coordinate.y;
      case 2:
        return coordinate.z;
      default:
        return Double.NaN;
    }
  }

  public void setCoordinate(final Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  @Override
  public void setValue(final int index, final double value) {
    switch (index) {
      case 0:
        coordinate.x = value;
      break;
      case 1:
        coordinate.y = value;
      break;
      case 2:
        coordinate.z = value;
      break;
      default:
      break;
    }
  }

  @Override
  public String toString() {
    return coordinate.toString();
  }
}
