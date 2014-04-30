package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.precision.GeometryPrecisionReducer;
import com.revolsys.jts.precision.MinimumClearance;

public class PrecisionFunctions {

  public static double minClearance(final Geometry g) {
    return MinimumClearance.getDistance(g);
  }

  public static Geometry minClearanceLine(final Geometry g) {
    return MinimumClearance.getLine(g);
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
