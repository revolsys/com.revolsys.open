package com.revolsys.jts.testold.perf.operation.distance;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.operation.distance.IndexedFacetDistance;

public class CachedBABDistance {

  static double getDistance(final Geometry g1, final Geometry g2) {
    if (cacheGeom != g1) {
      babDist = new IndexedFacetDistance(g1);
      cacheGeom = g1;
    }
    return babDist.getDistance(g2);
  }

  private static Geometry cacheGeom = null;

  private static IndexedFacetDistance babDist;

  public CachedBABDistance() {
    super();
  }
}
