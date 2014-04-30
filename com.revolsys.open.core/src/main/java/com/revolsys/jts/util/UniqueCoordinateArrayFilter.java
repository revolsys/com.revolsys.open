package com.revolsys.jts.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.io.Reader;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.vertex.Vertex;

public class UniqueCoordinateArrayFilter {
  public static List<Coordinates> getUniquePoints(final Geometry geometry) {
    final Reader<Vertex> vertices = geometry.vertices();
    return getUniquePoints(vertices);
  }

  public static List<Coordinates> getUniquePoints(
    final Iterable<? extends Coordinates> coordinates) {
    final Set<Coordinates> set = new TreeSet<>();
    final List<Coordinates> points = new ArrayList<>();
    for (final Coordinates point : coordinates) {
      if (!set.contains(point)) {
        final Coordinates clone = point.cloneCoordinates();
        points.add(clone);
        set.add(clone);
      }
    }
    return points;
  }

  public static Coordinates[] getUniquePointsArray(final Geometry geometry) {
    final List<Coordinates> points = getUniquePoints(geometry);
    return points.toArray(new Coordinates[points.size()]);
  }

  public static Coordinates[] getUniquePointsArray(
    final Iterable<? extends Coordinates> coordinates) {
    final List<Coordinates> points = getUniquePoints(coordinates);
    return points.toArray(new Coordinates[points.size()]);
  }

}
