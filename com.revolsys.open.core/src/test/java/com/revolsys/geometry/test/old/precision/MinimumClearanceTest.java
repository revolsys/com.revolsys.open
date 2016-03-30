package com.revolsys.geometry.test.old.precision;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.precision.MinimumClearance;
import com.revolsys.geometry.wkb.ParseException;
import com.revolsys.geometry.wkb.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class MinimumClearanceTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(MinimumClearanceTest.class);
  }

  private final GeometryFactory geomFact = GeometryFactory.DEFAULT;

  private final WKTReader reader = new WKTReader();

  public MinimumClearanceTest(final String name) {
    super(name);
  }

  private void runTest(final String wkt, final double expectedValue) throws ParseException {
    final Geometry g = this.reader.read(wkt);
    final double rp = MinimumClearance.getDistance(g);
    assertEquals(expectedValue, rp);
  }

  public void test2IdenticalPoints() throws ParseException {
    runTest("MULTIPOINT ((100 100), (100 100))", 1.7976931348623157E308);
  }

  public void test3Points() throws ParseException {
    runTest("MULTIPOINT ((100 100), (10 100), (30 100))", 20);
  }

  public void testTriangle() throws ParseException {
    runTest("POLYGON ((100 100, 300 100, 200 200, 100 100))", 100);
  }
}
