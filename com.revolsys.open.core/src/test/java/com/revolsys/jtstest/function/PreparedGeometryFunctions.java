package com.revolsys.jtstest.function;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.prep.PreparedGeometry;
import com.revolsys.jts.geom.prep.PreparedGeometryFactory;

public class PreparedGeometryFunctions {
  private static PreparedGeometry createPG(final Geometry g) {
    return (new PreparedGeometryFactory()).create(g);
  }

  public static boolean intersects(final Geometry g1, final Geometry g2) {
    return g1.intersects(g2);
  }

  public static boolean preparedIntersects(final Geometry g1, final Geometry g2) {
    return createPG(g1).intersects(g2);
  }

}
