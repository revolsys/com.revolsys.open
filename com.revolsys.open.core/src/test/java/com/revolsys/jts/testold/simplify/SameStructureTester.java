package com.revolsys.jts.testold.simplify;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.util.Assert;

/**
 * Test if two geometries have the same structure
 * (but not necessarily the same coordinate sequences or adjacencies).
 */
public class SameStructureTester {

  public static boolean isSameStructure(final Geometry g1, final Geometry g2) {
    if (g1.getClass() != g2.getClass()) {
      return false;
    }
    if (g1 instanceof GeometryCollection) {
      return isSameStructureCollection((GeometryCollection)g1,
        (GeometryCollection)g2);
    } else if (g1 instanceof Polygon) {
      return isSameStructurePolygon((Polygon)g1, (Polygon)g2);
    } else if (g1 instanceof LineString) {
      return isSameStructureLineString((LineString)g1, (LineString)g2);
    } else if (g1 instanceof Point) {
      return isSameStructurePoint((Point)g1, (Point)g2);
    }

    Assert.shouldNeverReachHere("Unsupported Geometry class: "
        + g1.getClass().getName());
    return false;
  }

  private static boolean isSameStructureCollection(final GeometryCollection g1,
    final GeometryCollection g2) {
    if (g1.getGeometryCount() != g2.getGeometryCount()) {
      return false;
    }
    for (int i = 0; i < g1.getGeometryCount(); i++) {
      if (!isSameStructure(g1.getGeometry(i), g2.getGeometry(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isSameStructureLineString(final LineString g1,
    final LineString g2) {
    // could check for both empty or nonempty here
    return true;
  }

  private static boolean isSameStructurePoint(final Point g1, final Point g2) {
    // could check for both empty or nonempty here
    return true;
  }

  private static boolean isSameStructurePolygon(final Polygon g1,
    final Polygon g2) {
    if (g1.getHoleCount() != g2.getHoleCount()) {
      return false;
    }
    // could check for both empty or nonempty here
    return true;
  }

  private SameStructureTester() {
  }
}
