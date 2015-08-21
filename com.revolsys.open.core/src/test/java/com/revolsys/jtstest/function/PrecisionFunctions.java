package com.revolsys.jtstest.function;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.precision.MinimumClearance;

public class PrecisionFunctions {

  public static double minClearance(final Geometry g) {
    return MinimumClearance.getDistance(g);
  }

  public static Geometry minClearanceLine(final Geometry g) {
    return MinimumClearance.getLine(g);
  }

}
