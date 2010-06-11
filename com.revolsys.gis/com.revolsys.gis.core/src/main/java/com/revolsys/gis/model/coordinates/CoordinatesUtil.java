package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

public class CoordinatesUtil {

  public static double distance(
    final Coordinates point1,
    final Coordinates point2) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
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

  public static boolean isAcute(
    Coordinates point1,
    Coordinates point2,
    Coordinates point3) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    final double x3 = point3.getX();
    final double y3 = point3.getY();

    return isAcute(x1, y1, x2, y2, x3, y3);
  }

  private static boolean isAcute(
    final double x1,
    final double y1,
    final double x2,
    final double y2,
    final double x3,
    final double y3) {
    double dx0 = x1 - x2;
    double dy0 = y1 - y2;
    double dx1 = x3 - x2;
    double dy1 = y3 - y2;
    double dotprod = dx0 * dx1 + dy0 * dy1;
    return dotprod > 0;
  }

  public static Coordinates get(
    Coordinate coordinate) {
    if (Double.isNaN(coordinate.z)) {
      return new DoubleCoordinates(coordinate.z, coordinate.y);
    } else {
      return new DoubleCoordinates(coordinate.z, coordinate.y, coordinate.z);
    }
  }

  public static Coordinates get(
    Point point) {
    if (point.isEmpty()) {
      return null;
    } else {
      CoordinatesList points = CoordinatesListUtil.get(point);
      return points.getPoint(0);
    }
  }
  public static Coordinates get(
    LineString line) {
    if (line.isEmpty()) {
      return null;
    } else {
      CoordinatesList points = CoordinatesListUtil.get(line);
      return points.getPoint(0);
    }
  }
}
