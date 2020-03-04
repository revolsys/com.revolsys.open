package com.revolsys.core.test.geometry.test.old.perf.operation.distance;

import java.util.List;

import com.revolsys.core.test.geometry.test.old.algorithm.InteriorPointTest;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.util.Stopwatch;

public class TestPerfFastDistanceFile {
  static final int MAX_ITER = 10;

  public static void main(final String[] args) {
    final TestPerfFastDistanceFile test = new TestPerfFastDistanceFile();
    try {
      test.test();
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
  }

  boolean testFailed = false;

  public TestPerfFastDistanceFile() {
  }

  void computeAllDistances(final List geoms, final int maxToScan) {
    int numGeoms1 = geoms.size();
    if (numGeoms1 > maxToScan) {
      numGeoms1 = maxToScan;
    }

    final int numGeoms2 = geoms.size();

    for (int i = 0; i < numGeoms1; i++) {
      // Geometry pg = PreparedGeometryFactory.prepare((Geometry)
      // geoms.get(i));
      for (int j = 0; j < numGeoms2; j++) {
        // don't compute distance to itself!
        // if (i == j) continue;

        final Geometry g1 = (Geometry)geoms.get(i);
        final Geometry g2 = (Geometry)geoms.get(j);

        // if (g1.getEnvelopeInternal().intersects(g2.getEnvelopeInternal()))
        // continue;

        // double dist = g1.distance(g2);
        // double dist = BranchAndBoundFacetDistance.distance(g1, g2);
        final double dist = CachedBABDistance.getDistance(g1, g2);
        // double distFast = SortedBoundsFacetDistance.distance(g1, g2);

        // pg.intersects(g2);
      }
    }
  }

  void computePairDistance(final List geoms, final int i, final int j) {
    for (int n = 0; n < MAX_ITER; n++) {
      final Geometry g1 = (Geometry)geoms.get(i);
      final Geometry g2 = (Geometry)geoms.get(j);

      final double dist = g1.distanceGeometry(g2);
      // double dist = SortedBoundsFacetDistance.distance(g1, g2);
      // double dist = BranchAndBoundFacetDistance.distance(g1, g2);
    }
  }

  public void test() throws Exception {
    final List geoms = InteriorPointTest.getTestGeometries("world.wkt");

    testAllDistances(geoms, 1);
    testAllDistances(geoms, 2);
    testAllDistances(geoms, 5);
    testAllDistances(geoms, 10);
    testAllDistances(geoms, 20);
    testAllDistances(geoms, 30);
    testAllDistances(geoms, 40);
    testAllDistances(geoms, 50);
  }

  void testAllDistances(final List geoms, final int maxToScan) {
    final Stopwatch sw = new Stopwatch();

    computeAllDistances(geoms, maxToScan);
    // computePairDistance(geoms, 1, 3);
    // computePairDistance(geoms, 55, 77);

    // System.out.println("Count = " + maxToScan + " Finished in "
    // + sw.getTimeString());
  }

}
