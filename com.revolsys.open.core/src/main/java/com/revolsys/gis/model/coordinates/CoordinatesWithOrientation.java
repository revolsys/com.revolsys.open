package com.revolsys.gis.model.coordinates;


@SuppressWarnings("serial")
public class CoordinatesWithOrientation extends DoubleCoordinates {

  private double orientation;

  public CoordinatesWithOrientation(Coordinates coordinates, double orientation) {
    super(coordinates);
    this.orientation = orientation;
  }

  public double getOrientation() {
    return orientation;
  }
}
