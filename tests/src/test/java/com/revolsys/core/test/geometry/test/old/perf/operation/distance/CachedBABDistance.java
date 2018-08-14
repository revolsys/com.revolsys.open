package com.revolsys.core.test.geometry.test.old.perf.operation.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.operation.distance.IndexedFacetDistance;

public class CachedBABDistance {

  private static IndexedFacetDistance babDist;

  private static Geometry cacheGeom = null;

  static double getDistance(final Geometry g1, final Geometry g2) {
    if (cacheGeom != g1) {
      babDist = new IndexedFacetDistance(g1);
      cacheGeom = g1;
    }
    return babDist.getDistance(g2);
  }

  public CachedBABDistance() {
    super();
  }
}
