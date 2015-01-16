package com.revolsys.jts.testold.perf.operation.distance;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.util.SineStarFactory;
import com.revolsys.jts.util.Stopwatch;

public class TestPerfDistanceGeomSet {
  public static void main(final String[] args) {
    final TestPerfDistanceGeomSet test = new TestPerfDistanceGeomSet();
    // test.test();
    test.test();
  }

  static final int MAX_ITER = 1;

  static final int NUM_GEOM = 100;

  static final double GEOM_SIZE = 1;

  static final double MAX_X = 100;

  boolean testFailed = false;

  boolean verbose = false;

  double size = 100;

  double separationDist = this.size * 2;

  public TestPerfDistanceGeomSet() {
  }

  Geometry createCircleRandomLocation(final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(randomLocation());
    gsf.setSize(GEOM_SIZE);
    gsf.setNumPoints(nPts);

    final Polygon g = gsf.createCircle();
    // Geometry g = gsf.createSineStar();

    return g;
  }

  Geometry[] createRandomCircles(final int nPts) {
    final Geometry[] geoms = new Geometry[NUM_GEOM];
    for (int i = 0; i < NUM_GEOM; i++) {
      geoms[i] = createCircleRandomLocation(nPts);
    }
    return geoms;
  }

  Geometry[] createRandomCircles(final int numGeom, final int nPtsMin,
    final int nPtsMax) {
    final int nPtsRange = nPtsMax - nPtsMin + 1;
    final Geometry[] geoms = new Geometry[numGeom];
    for (int i = 0; i < numGeom; i++) {
      final int nPts = (int)(nPtsRange * Math.random()) + nPtsMin;
      geoms[i] = createCircleRandomLocation(nPts);
    }
    return geoms;
  }

  Point randomLocation() {
    final double x = Math.random() * MAX_X;
    final double y = Math.random() * MAX_X;
    return new PointDouble(x, y, Point.NULL_ORDINATE);
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
      //  System.out.println(sw.getTimeString());
    }
    if (this.verbose) {
      //  System.out.println("Finished in " + sw.getTimeString());
      //  System.out.println("       (Distance = " + dist + ")");
    }
  }

  public void test(final int num) {

    // Geometry[] geom = createRandomCircles(nPts);
    final Geometry[] geom = createRandomCircles(100, 5, num);
    // Geometry[] geom = createSineStarsRandomLocation(nPts);

    if (this.verbose) {
      //  System.out.println("Running with " + num + " points");
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
        final double dist = element.distance(element2);
        // double dist = SortedBoundsFacetDistance.distance(g1, g2);
        // double dist = BranchAndBoundFacetDistance.distance(geom[i], geom[j]);
        // double dist = CachedBABDistance.getDistance(geom[i], geom[j]);

      }
    }
  }
}
