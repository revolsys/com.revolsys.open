package com.revolsys.gis.cs.projection;


public interface CoordinatesProjection {
  void inverse(final double x, double y, final double[] targetCoordinates,
    int targetOffset, int targetAxisCount);

  void project(final double x, double y, final double[] targetCoordinates,
    int targetOffset, int targetAxisCount);
}
