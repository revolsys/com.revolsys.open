package com.revolsys.geometry.test.old.algorithm.distance;

import com.revolsys.geometry.algorithm.distance.DiscreteHausdorffDistance;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.test.old.junit.GeometryUtils;
import com.revolsys.geometry.wkb.ParseException;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class DiscreteHausdorffDistanceTest extends TestCase {
  private static final double TOLERANCE = 0.00001;

  public static void main(final String args[]) {
    TestRunner.run(DiscreteHausdorffDistanceTest.class);
  }

  public DiscreteHausdorffDistanceTest(final String name) {
    super(name);
  }

  private void runTest(final String wkt1, final String wkt2, final double expectedDistance)
    throws ParseException {
    final Geometry g1 = GeometryUtils.readWKT(wkt1);
    final Geometry g2 = GeometryUtils.readWKT(wkt2);

    final double distance = DiscreteHausdorffDistance.distance(g1, g2);
    assertEquals(distance, expectedDistance, TOLERANCE);
  }

  private void runTest(final String wkt1, final String wkt2, final double densifyFrac,
    final double expectedDistance) throws ParseException {
    final Geometry g1 = GeometryUtils.readWKT(wkt1);
    final Geometry g2 = GeometryUtils.readWKT(wkt2);

    final double distance = DiscreteHausdorffDistance.distance(g1, g2, densifyFrac);
    assertEquals(distance, expectedDistance, TOLERANCE);
  }

  public void testLinePoints() throws Exception {
    runTest("LINESTRING (0 0, 2 0)", "MULTIPOINT (0 1, 1 0, 2 1)", 1.0);
  }

  public void testLineSegments() throws Exception {
    runTest("LINESTRING (0 0, 2 1)", "LINESTRING (0 0, 2 0)", 1.0);
  }

  public void testLineSegments2() throws Exception {
    runTest("LINESTRING (0 0, 2 0)", "LINESTRING (0 1, 1 2, 2 1)", 2.0);
  }

  /**
   * Shows effects of limiting HD to vertices
   * Answer is not true Hausdorff distance.
   *
   * @throws Exception
   */
  public void testLinesShowingDiscretenessEffect() throws Exception {
    runTest("LINESTRING (130 0, 0 0, 0 150)", "LINESTRING (10 10, 10 150, 130 10)",
      14.142135623730951);
    // densifying provides accurate HD
    runTest("LINESTRING (130 0, 0 0, 0 150)", "LINESTRING (10 10, 10 150, 130 10)", 0.5, 70.0);
  }

}
