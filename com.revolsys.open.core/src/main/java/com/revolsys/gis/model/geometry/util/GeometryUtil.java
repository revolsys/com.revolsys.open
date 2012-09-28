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

  public static List<Point> getPoints(Geometry geometry) {
    List<Point> points = new ArrayList<Point>();
    for (Geometry part : geometry.getGeometries()) {
      if (part instanceof Point) {
        Point point = (Point)part;
        points.add(point);
      }
    }
    return points;
  }

  public static List<LineString> getLines(Geometry geometry) {
    List<LineString> lines = new ArrayList<LineString>();
    for (Geometry part : geometry.getGeometries()) {
      if (part instanceof LineString) {
        LineString line = (LineString)part;
        lines.add(line);
      } else if (part instanceof Polygon) {
        Polygon polygon = (Polygon)part;
        MultiLinearRing rings = polygon.getRings();
        for (LinearRing ring : rings) {
          lines.add(ring);
        }
      }
    }
    return lines;
  }

  public static List<Polygon> getPolygons(Geometry geometry) {
    List<Polygon> polygons = new ArrayList<Polygon>();
    for (Geometry part : geometry.getGeometries()) {
      if (part instanceof Polygon) {
        Polygon polygon = (Polygon)part;
        polygons.add(polygon);
      }
    }
    return polygons;
  }
}
