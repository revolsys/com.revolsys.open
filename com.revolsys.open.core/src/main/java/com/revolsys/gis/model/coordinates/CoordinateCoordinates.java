package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.util.NumberUtil;

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

  /**
   * Tests if another coordinate has the same values for the X and Y ordinates.
   * The Z ordinate is ignored.
   *
   *@param other a <code>Coordinate</code> with which to do the 2D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X and Y.
   */
  @Override
  public boolean equals2d(final Coordinates c, final double tolerance) {
    if (!NumberUtil.equalsWithTolerance(this.getX(), c.getX(), tolerance)) {
      return false;
    }
    if (!NumberUtil.equalsWithTolerance(this.getY(), c.getY(), tolerance)) {
      return false;
    }
    return true;
  }

  public Coordinates getCoordinate() {
    return coordinate;
  }

  @Override
  public byte getNumAxis() {
    if (Double.isNaN(coordinate.getZ())) {
      return 2;
    } else {
      return 3;
    }
  }

  @Override
  public double getValue(final int index) {
    switch (index) {
      case 0:
        return coordinate.getX();
      case 1:
        return coordinate.getY();
      case 2:
        return coordinate.getZ();
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
        coordinate.setX(value);
      break;
      case 1:
        coordinate.setY(value);
      break;
      case 2:
        coordinate.setZ(value);
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
