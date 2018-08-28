package com.revolsys.core.test.geometry.test.function;

import com.revolsys.geometry.model.Geometry;

public class PreparedGeometryFunctions {
  public static boolean intersects(final Geometry g1, final Geometry g2) {
    return g1.intersects(g2);
  }

  private static Geometry newPG(final Geometry g) {
    return g.prepare();
  }

  public static boolean preparedIntersects(final Geometry g1, final Geometry g2) {
    return newPG(g1).intersects(g2);
  }

}
