package com.revolsys.jts.testold.perf.algorithm;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;

public class AreaPrecisionPerfTest {
  public static double accurateSignedArea(final Coordinates[] ring) {
    if (ring.length < 3) {
      return 0.0;
    }
    double sum = 0.0;
    // http://en.wikipedia.org/wiki/Shoelace_formula
    final double x0 = ring[0].getX();
    for (int i = 1; i < ring.length - 1; i++) {
      final double x = ring[i].getX() - x0;
      final double y1 = ring[i + 1].getY();
      final double y2 = ring[i == 0 ? ring.length - 1 : i - 1].getY();
      sum += x * (y2 - y1);
    }
    return sum / 2.0;
  }

  public static void main(final String[] args) throws Exception {

    final double originX = 1000000;
    final double originY = 5000000;
    final long start = System.currentTimeMillis();

    for (int nrVertices = 4; nrVertices <= 1000000; nrVertices *= 2) {
      final Coordinates[] coordinates = new Coordinates[nrVertices + 1];

      Coordinates vertex;
      for (int i = 0; i <= nrVertices; i++) {
        vertex = new Coordinate((double)originX
          + (1 + Math.sin((float)i / (float)nrVertices * 2 * Math.PI)), originY
          + (1 + Math.cos((float)i / (float)nrVertices * 2 * Math.PI)), Coordinates.NULL_ORDINATE);
        coordinates[i] = vertex;
      }
      // close ring
      coordinates[nrVertices] = coordinates[0];

      final Geometry g1 = GeometryFactory.getFactory().createLinearRing(coordinates);
      final LinearRing[] holes = new LinearRing[] {};
      final Polygon polygon = GeometryFactory.getFactory().createPolygon(
        (LinearRing)g1, holes);
      System.out.println(polygon);

      final double area = originalSignedArea(coordinates);
      final double area2 = accurateSignedArea(coordinates);
      final double exactArea = 0.5 * nrVertices
        * Math.sin(2 * Math.PI / nrVertices);

      final double eps = exactArea - area;
      final double eps2 = exactArea - area2;

      System.out.println(nrVertices + "   orig err: " + eps + "    acc err: "
        + eps2);
    }
    System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000.0);
  }

  public static double originalSignedArea(final Coordinates[] ring) {
    if (ring.length < 3) {
      return 0.0;
    }
    double sum = 0.0;
    for (int i = 0; i < ring.length - 1; i++) {
      final double bx = ring[i].getX();
      final double by = ring[i].getY();
      final double cx = ring[i + 1].getX();
      final double cy = ring[i + 1].getY();
      sum += (bx + cx) * (cy - by);
    }
    return -sum / 2.0;
  }

}
