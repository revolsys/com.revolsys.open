package com.revolsys.gis.model.coordinates;

@SuppressWarnings("serial")
public class CoordinatesWithOrientation extends DoubleCoordinates {

  private final double orientation;

  public CoordinatesWithOrientation(final Coordinates coordinates,
    final double orientation) {
    super(coordinates);
    this.orientation = orientation;
  }

  public double getOrientation() {
    return orientation;
  }
}
