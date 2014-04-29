package com.revolsys.jtstest.geomop;

import com.revolsys.jts.geom.Geometry;

public class NormalizedGeometryMatcher implements GeometryMatcher {
  private double tolerance;

  public NormalizedGeometryMatcher() {

  }

  @Override
  public boolean match(final Geometry a, final Geometry b) {
    final Geometry aClone = a.normalize();
    final Geometry bClone = b.normalize();
    return aClone.equalsExact(bClone, tolerance);
  }

  @Override
  public void setTolerance(final double tolerance) {
    this.tolerance = tolerance;
  }

}
