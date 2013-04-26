package com.revolsys.gis.model.geometry.util;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;

public class GeometryUtil {

  public static List<LineString> getLines(final Geometry geometry) {
    final List<LineString> lines = new ArrayList<LineString>();
    for (final Geometry part : geometry.getGeometries()) {
      if (part instanceof LineString) {
        final LineString line = (LineString)part;
        lines.add(line);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        final MultiLinearRing rings = polygon.getRings();
        for (final LinearRing ring : rings) {
          lines.add(ring);
        }
      }
    }
    return lines;
  }

  public static List<Point> getPoints(final Geometry geometry) {
    final List<Point> points = new ArrayList<Point>();
    for (final Geometry part : geometry.getGeometries()) {
      if (part instanceof Point) {
        final Point point = (Point)part;
        points.add(point);
      }
    }
    return points;
  }

  public static List<Polygon> getPolygons(final Geometry geometry) {
    final List<Polygon> polygons = new ArrayList<Polygon>();
    for (final Geometry part : geometry.getGeometries()) {
      if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        polygons.add(polygon);
      }
    }
    return polygons;
  }

}
