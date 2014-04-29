package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.precision.GeometryPrecisionReducer;
import com.revolsys.jts.precision.MinimumClearance;
import com.revolsys.jts.precision.SimpleMinimumClearance;

public class PrecisionFunctions {

  /*
   * private static Geometry OLDreducePrecisionPointwise(Geometry geom, double
   * scaleFactor) { PrecisionModel pm = new PrecisionModel(scaleFactor);
   * Geometry reducedGeom = SimpleGeometryPrecisionReducer.reduce(geom, pm);
   * return reducedGeom; }
   */

  public static double minClearance(final Geometry g) {
    return MinimumClearance.getDistance(g);
  }

  public static Geometry minClearanceLine(final Geometry g) {
    return MinimumClearance.getLine(g);
  }

  public static double minClearanceSimple(final Geometry g) {
    return SimpleMinimumClearance.getDistance(g);
  }

  public static Geometry minClearanceSimpleLine(final Geometry g) {
    return SimpleMinimumClearance.getLine(g);
  }

  public static Geometry reducePrecision(final Geometry geom,
    final double scaleFactor) {
    final PrecisionModel pm = new PrecisionModel(scaleFactor);
    final Geometry reducedGeom = GeometryPrecisionReducer.reduce(geom, pm);
    return reducedGeom;
  }

  public static Geometry reducePrecisionPointwise(final Geometry geom,
    final double scaleFactor) {
    final PrecisionModel pm = new PrecisionModel(scaleFactor);
    final Geometry reducedGeom = GeometryPrecisionReducer.reducePointwise(geom,
      pm);
    return reducedGeom;
  }
}
