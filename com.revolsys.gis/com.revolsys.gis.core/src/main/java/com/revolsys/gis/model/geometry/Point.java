package com.revolsys.gis.model.geometry;

public interface Point extends Geometry, Comparable<Point> {
  double angle2d(
    Point point);

  double distance2d(
    Point point);

  double getX();

  double getY();

  double getZ();

  double getM();
}
