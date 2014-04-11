package com.revolsys.jts.testold.perf.algorithm;

import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.jts.algorithm.MinimumBoundingCircle;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.util.Assert;

public class MinimumBoundingCircleStressTest {
  public static void main(final String args[]) {
    try {
      new MinimumBoundingCircleStressTest().run();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }

  }

  GeometryFactory geomFact = GeometryFactory.getFactory();

  public MinimumBoundingCircleStressTest() {

  }

  void checkWithinCircle(final Coordinates[] pts, final Coordinates centre,
    final double radius, final double tolerance) {
    for (final Coordinates p : pts) {
      final double ptRadius = centre.distance(p);
      final double error = ptRadius - radius;
      if (error > tolerance) {
        Assert.shouldNeverReachHere();
      }
    }
  }

  Coordinates[] createRandomPoints(final int n) {
    final Coordinates[] pts = new Coordinates[n];
    for (int i = 0; i < n; i++) {
      final double x = 100 * Math.random();
      final double y = 100 * Math.random();
      pts[i] = new Coordinate((double)x, y, Coordinates.NULL_ORDINATE);
    }
    return pts;
  }

  void run() {
    while (true) {
      final int n = (int)(10000 * Math.random());
      run(n);
    }
  }

  void run(final int nPts) {
    final Coordinates[] randPts = createRandomPoints(nPts);
    final Geometry mp = this.geomFact.createMultiPoint(randPts);
    final MinimumBoundingCircle mbc = new MinimumBoundingCircle(mp);
    final Coordinates centre = mbc.getCentre();
    final double radius = mbc.getRadius();
    System.out.println("Testing " + nPts + " random points.  Radius = "
      + radius);

    checkWithinCircle(randPts, centre, radius, 0.0001);
  }
}
