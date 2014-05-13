package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.geom.Point;

@SuppressWarnings("serial")
public class CoordinatesWithOrientation extends DoubleCoordinates {

  private final double orientation;

  public CoordinatesWithOrientation(final Point coordinates,
    final double orientation) {
    super(coordinates);
    this.orientation = orientation;
  }

  public double getOrientation() {
    return orientation;
  }
}
