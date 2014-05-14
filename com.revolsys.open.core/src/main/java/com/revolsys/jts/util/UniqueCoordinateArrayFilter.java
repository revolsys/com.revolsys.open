package com.revolsys.jts.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.vertex.Vertex;

public class UniqueCoordinateArrayFilter {
  public static List<Point> getUniquePoints(final Geometry geometry) {
    final Reader<Vertex> vertices = geometry.vertices();
    return getUniquePoints(vertices);
  }

  public static List<Point> getUniquePoints(
    final Iterable<? extends Point> coordinates) {
    final Set<Point> set = new TreeSet<>();
    final List<Point> points = new ArrayList<>();
    for (final Point point : coordinates) {
      if (!set.contains(point)) {
        final Point clone = point.cloneCoordinates();
        points.add(clone);
        set.add(clone);
      }
    }
    return points;
  }

  public static Point[] getUniquePointsArray(final Geometry geometry) {
    final List<Point> points = getUniquePoints(geometry);
    return points.toArray(new Point[points.size()]);
  }

  public static Point[] getUniquePointsArray(
    final Iterable<? extends Point> coordinates) {
    final List<Point> points = getUniquePoints(coordinates);
    return points.toArray(new Point[points.size()]);
  }

}
