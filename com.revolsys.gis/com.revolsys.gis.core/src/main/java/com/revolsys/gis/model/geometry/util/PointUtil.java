package com.revolsys.gis.model.geometry.util;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class PointUtil {

  public static Point getPointWithin(
    Polygon polygon) {
    Point centroid = polygon.getCentroid();
    if (centroid.within(polygon)) {
      return centroid;
    } else {
      final GeometryFactory factory = GeometryFactory.getFactory(polygon);
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

  public static Point createRandomPoint(
    GeometryFactory factory,
    Envelope envelope) {
    double x = envelope.getMinX() + envelope.getWidth() * Math.random();
    double y = envelope.getMinY() + envelope.getHeight() * Math.random();
    CoordinatesList coordinatesList = new DoubleCoordinatesList(2, x, y);
    return factory.createPoint(coordinatesList);
  }

}
