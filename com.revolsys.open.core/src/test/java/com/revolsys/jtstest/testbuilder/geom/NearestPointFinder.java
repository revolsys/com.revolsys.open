package com.revolsys.jtstest.testbuilder.geom;

import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;

public class NearestPointFinder {
  static class NearestPointFilter implements CoordinateSequenceFilter {
    private double tolerance = 0.0;

    private final Coordinates basePt;

    private Coordinates nearestPt = null;

    private final double dist = Double.MAX_VALUE;

    public NearestPointFilter(final Coordinates basePt, final double tolerance) {
      this.basePt = basePt;
      this.tolerance = tolerance;
    }

    @Override
    public void filter(final CoordinatesList seq, final int i) {
      final Coordinates p = seq.getCoordinate(i);
      double dist = p.distance(basePt);
      if (dist > tolerance) {
        return;
      }

      if (nearestPt == null || basePt.distance(p) < dist) {
        nearestPt = p;
        dist = basePt.distance(nearestPt);
        return;
      }
    }

    public Coordinates getNearestPoint() {
      return nearestPt;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public boolean isGeometryChanged() {
      return false;
    }
  }

  public static Coordinates findNearestPoint(final Geometry geom,
    final Coordinates pt, final double tolerance) {
    final NearestPointFinder finder = new NearestPointFinder(geom);
    return finder.getNearestPoint(pt, tolerance);
  }

  private final Geometry geom;

  public NearestPointFinder(final Geometry geom) {
    this.geom = geom;
  }

  public Coordinates getNearestPoint(final Coordinates pt,
    final double tolerance) {
    final NearestPointFilter filter = new NearestPointFilter(pt, tolerance);
    geom.apply(filter);
    return filter.getNearestPoint();
  }

}
