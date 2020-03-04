package com.revolsys.core.test.geometry.test.old.perf.operation.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.util.SineStarFactory;
import com.revolsys.geometry.util.Stopwatch;

public class TestPerfDistanceGeomSet {
  static final double GEOM_SIZE = 1;

  static final int MAX_ITER = 1;

  static final double MAX_X = 100;

  static final int NUM_GEOM = 100;

  public static void main(final String[] args) {
    final TestPerfDistanceGeomSet test = new TestPerfDistanceGeomSet();
    // test.test();
    test.test();
  }

  double separationDist = this.size * 2;

  double size = 100;

  boolean testFailed = false;

  boolean verbose = false;

  public TestPerfDistanceGeomSet() {
  }

  Geometry newCircleRandomLocation(final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(randomLocation());
    gsf.setSize(GEOM_SIZE);
    gsf.setNumPoints(nPts);

    final Polygon g = gsf.newCircle();
    // Geometry g = gsf.createSineStar();

    return g;
  }

  Geometry[] newRandomCircles(final int nPts) {
    final Geometry[] geoms = new Geometry[NUM_GEOM];
    for (int i = 0; i < NUM_GEOM; i++) {
      geoms[i] = newCircleRandomLocation(nPts);
    }
    return geoms;
  }

  Geometry[] newRandomCircles(final int numGeom, final int nPtsMin, final int nPtsMax) {
    final int nPtsRange = nPtsMax - nPtsMin + 1;
    final Geometry[] geoms = new Geometry[numGeom];
    for (int i = 0; i < numGeom; i++) {
      final int nPts = (int)(nPtsRange * Math.random()) + nPtsMin;
      geoms[i] = newCircleRandomLocation(nPts);
    }
    return geoms;
  }

  Point randomLocation() {
    final double x = Math.random() * MAX_X;
    final double y = Math.random() * MAX_X;
    return new PointDoubleXY(x, y);
  }

  public void test() {

    // test(5000);
    // test(8001);

    test(10);
    test(3);
    test(4);
    test(5);
    test(10);
    test(20);
    test(30);
    test(40);
    test(50);
    test(60);
    test(100);
    test(200);
    test(500);
    test(1000);
    test(5000);
    test(10000);
    test(50000);
    test(100000);
  }

  public void test(final Geometry[] geom) {
    final Stopwatch sw = new Stopwatch();
    final double dist = 0.0;
    for (int i = 0; i < MAX_ITER; i++) {
      testAll(geom);
    }
    if (!this.verbose) {
      // System.out.println(sw.getTimeString());
    }
    if (this.verbose) {
      // System.out.println("Finished in " + sw.getTimeString());
      // System.out.println(" (Distance = " + dist + ")");
    }
  }

  public void test(final int num) {

    final Geometry[] geom = newRandomCircles(100, 5, num);

    if (this.verbose) {
      // System.out.println("Running with " + num + " points");
    }
    if (!this.verbose) {
      System.out.print(num + ", ");
    }
    test(geom);
  }

  public void test2() {
    this.verbose = false;

    for (int i = 800; i <= 2000; i += 100) {
      test(i);
    }
  }

  void testAll(final Geometry[] geom) {
    for (final Geometry element : geom) {
      for (final Geometry element2 : geom) {
        final double dist = element.distanceGeometry(element2);
        // double dist = SortedBoundsFacetDistance.distance(g1, g2);
        // double dist = BranchAndBoundFacetDistance.distance(geom[i], geom[j]);
        // double dist = CachedBABDistance.getDistance(geom[i], geom[j]);

      }
    }
  }
}
