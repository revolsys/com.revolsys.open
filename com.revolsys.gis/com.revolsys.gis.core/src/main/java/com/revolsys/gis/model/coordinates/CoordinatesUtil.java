package com.revolsys.gis.model.coordinates;

public class CoordinatesUtil {

  public static double distance(
    final Coordinates coordinates1,
    final Coordinates coordinates2) {
    final double x1 = coordinates1.getX();
    final double y1 = coordinates1.getY();
    final double x2 = coordinates2.getX();
    final double y2 = coordinates2.getY();
    return distance(x1, y1, x2, y2);
  }

  public static double distance(
    final double x1,
    final double y1,
    final double x2,
    final double y2) {
    final double dx = x1 - x2;
    final double dy = y1 - y2;

    return Math.sqrt(dx * dx + dy * dy);
  }
}
