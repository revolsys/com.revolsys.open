package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public interface CoordinatesProjection {
  void inverse(final Coordinates from, final Coordinates to);

  void inverse(final double x, double y, final double[] targetCoordinates,
    int targetOffset, int targetNumAxis);

  void project(final Coordinates from, final Coordinates to);

  void project(final double x, double y, final double[] targetCoordinates,
    int targetOffset, int targetNumAxis);
}
