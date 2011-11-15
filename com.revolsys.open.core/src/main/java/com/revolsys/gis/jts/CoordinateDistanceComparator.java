package com.revolsys.gis.jts;

import java.util.Comparator;

import com.vividsolutions.jts.geom.Coordinate;

public class CoordinateDistanceComparator implements Comparator<Coordinate> {
  private final Coordinate coordinate;

  public CoordinateDistanceComparator(
    final Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  public int compare(
    final Coordinate c1,
    final Coordinate c2) {
    final double d1 = coordinate.distance(c1);
    final double d2 = coordinate.distance(c2);
    return Double.compare(d1, d2);
  };
}
