package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Point;

public interface CoordinatesProjection {
  void inverse(final double x, double y, final double[] targetCoordinates,
    int targetOffset, int targetAxisCount);

  void project(final double x, double y, final double[] targetCoordinates,
    int targetOffset, int targetAxisCount);
}
