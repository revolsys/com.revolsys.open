package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Geometry;

public class PreparedGeometryFunctions {
  private static Geometry createPG(final Geometry g) {
    return g.prepare();
  }

  public static boolean intersects(final Geometry g1, final Geometry g2) {
    return g1.intersects(g2);
  }

  public static boolean preparedIntersects(final Geometry g1, final Geometry g2) {
    return createPG(g1).intersects(g2);
  }

}
