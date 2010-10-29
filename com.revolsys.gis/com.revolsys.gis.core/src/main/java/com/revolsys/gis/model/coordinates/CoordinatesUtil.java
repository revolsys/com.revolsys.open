package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
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
    return MathUtil.distance(x1, y1, x2, y2);
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

    return MathUtil.isAcute(x1, y1, x2, y2, x3, y3);
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
    Geometry geometry) {
    if (geometry.isEmpty()) {
      return null;
    } else {
      CoordinatesList points = CoordinatesListUtil.get(geometry);
      return points.get(0);
    }
  }
}
