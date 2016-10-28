package com.revolsys.geometry.test.old.perf.operation.distance;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.util.SineStarFactory;
import com.revolsys.geometry.util.GeometricShapeFactory;
import com.revolsys.geometry.util.Stopwatch;

public class TestPerfDistanceGeomPair {

  static final int MAX_ITER = 100;

  public static void main(final String[] args) {
    final TestPerfDistanceGeomPair test = new TestPerfDistanceGeomPair();
    // test.test();
    test.test2();
  }

  double separationDist = this.size * 2;

  double size = 100;

  boolean testFailed = false;

  boolean verbose = true;

  public TestPerfDistanceGeomPair() {
  }

  void computeDistanceToAllPoints(final Geometry[] geom) {
    double dist = 0.0;
    final double dist2 = 0.0;
    for (final Point p : geom[1].vertices()) {
      // slow N^2 distance
      dist = geom[0].distance(geom[1].getGeometryFactory().point(p));

      // dist2 = fastDist.getDistance(geom[1].getFactory().createPoint(p));

      // if (dist != dist2) System.out.println("distance discrepancy found!");
    }
  }

  Geometry[] newCircles(final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setCentre(new PointDoubleXY((double)0, 0));
    gsf.setSize(100);
    gsf.setNumPoints(nPts);

    final Polygon gRect = gsf.newCircle();

    gsf.setCentre(new PointDoubleXY((double)0, this.separationDist));

    final Polygon gRect2 = gsf.newCircle();

    return new Geometry[] {
      gRect, gRect2
    };

  }

  Geometry[] newSineStars(final int nPts) {
    final SineStarFactory gsf = new SineStarFactory();
    gsf.setCentre(new PointDoubleXY((double)0, 0));
    gsf.setSize(100);
    gsf.setNumPoints(nPts);

    final Geometry g = gsf.newSineStar().getBoundary();

    gsf.setCentre(new PointDoubleXY((double)0, this.separationDist));

    final Geometry g2 = gsf.newSineStar().getBoundary();

    return new Geometry[] {
      g, g2
    };

  }

  public void test() {

    // test(5000);
    // test(8001);

    test(10);
    test(10);
    test(100);
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
    final double dist2 = 0.0;
    for (int i = 0; i < MAX_ITER; i++) {

      // dist = geom[0].distance(geom[1]);
      // dist = SortedBoundsFacetDistance.distance(g1, g2);
      // dist2 = BranchAndBoundFacetDistance.distance(geom[0], geom[1]);
      // if (dist != dist2) System.out.println("distance discrepancy found!");

      computeDistanceToAllPoints(geom);
    }
    if (!this.verbose) {
      // System.out.println(sw.getTimeString());
    }
    if (this.verbose) {
      // System.out.println("Finished in " + sw.getTimeString());
      // System.out.println(" (Distance = " + dist + ")");
    }
  }

  public void test(final int nPts) {

    // Geometry[] geom = newCircles(nPts);
    final Geometry[] geom = newSineStars(nPts);

    if (this.verbose) {
      // System.out.println("Running with " + nPts + " points");
    }
    if (!this.verbose) {
      System.out.print(nPts + ": ");
    }
    test(geom);
  }

  public void test2() {
    this.verbose = false;

    for (int i = 100; i <= 2000; i += 100) {
      test(i);
    }
  }
}
