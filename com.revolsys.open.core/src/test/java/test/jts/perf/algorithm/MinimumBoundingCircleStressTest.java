package test.jts.perf.algorithm;

import com.revolsys.jts.algorithm.MinimumBoundingCircle;
import com.revolsys.jts.geom.Coordinate;
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

  GeometryFactory geomFact = new GeometryFactory();

  public MinimumBoundingCircleStressTest() {

  }

  void checkWithinCircle(final Coordinate[] pts, final Coordinate centre,
    final double radius, final double tolerance) {
    for (final Coordinate p : pts) {
      final double ptRadius = centre.distance(p);
      final double error = ptRadius - radius;
      if (error > tolerance) {
        Assert.shouldNeverReachHere();
      }
    }
  }

  Coordinate[] createRandomPoints(final int n) {
    final Coordinate[] pts = new Coordinate[n];
    for (int i = 0; i < n; i++) {
      final double x = 100 * Math.random();
      final double y = 100 * Math.random();
      pts[i] = new Coordinate(x, y);
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
    final Coordinate[] randPts = createRandomPoints(nPts);
    final Geometry mp = this.geomFact.createMultiPoint(randPts);
    final MinimumBoundingCircle mbc = new MinimumBoundingCircle(mp);
    final Coordinate centre = mbc.getCentre();
    final double radius = mbc.getRadius();
    System.out.println("Testing " + nPts + " random points.  Radius = "
      + radius);

    checkWithinCircle(randPts, centre, radius, 0.0001);
  }
}
