package com.revolsys.gis.model.geometry.util;

import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class PointUtil {

  public static Point createRandomPoint(
    final com.revolsys.jts.geom.GeometryFactory factory, final Envelope envelope) {
    final double x = envelope.getMinX() + envelope.getWidth() * Math.random();
    final double y = envelope.getMinY() + envelope.getHeight() * Math.random();
    final CoordinatesList coordinatesList = new DoubleCoordinatesList(2, x, y);
    return factory.createPoint(coordinatesList);
  }

  public static Point getPointWithin(final Geometry geometry) {
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometry(i);
      if (!part.isEmpty()) {
        if (part instanceof Point) {
          return (Point)part;
        } else if (part instanceof LineString) {
          return LineStringUtil.midPoint((LineString)geometry);
        } else if (part instanceof Polygon) {
          final Polygon polygon = (Polygon)part;
          return getPointWithin(polygon);
        } else {
          return part.getCentroid();
        }
      }
    }
    return null;
  }

  public static Point getPointWithin(final Polygon polygon) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(polygon);
    final Point centroid = polygon.getCentroid();
    if (centroid.within(polygon)) {
      final Coordinates coordinates = CoordinatesUtil.getInstance(centroid);
      final CoordinatesList coordinatesList = new DoubleCoordinatesList(2,
        coordinates.getX(), coordinates.getY());
      return factory.createPoint(coordinatesList);
    } else {
      final Envelope envelope = polygon.getEnvelopeInternal();
      Point point = createRandomPoint(factory, envelope);
      int i = 1;
      while (!point.within(polygon)) {
        point = createRandomPoint(factory, envelope);
        if (i++ > 1000) {
          throw new RuntimeException(
            "Too many iterations to create a random point in polygon");
        }
      }
      return point;
    }
  }

}
