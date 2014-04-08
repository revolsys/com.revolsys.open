package com.revolsys.jts.dissolve;

import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import test.jts.junit.GeometryUtils;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.io.ParseException;

public class LineDissolverTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(LineDissolverTest.class);
  }

  public LineDissolverTest(final String name) {
    super(name);
  }

  private void checkDissolve(final List geoms, final Geometry expected) {
    final LineDissolver d = new LineDissolver();
    d.add(geoms);
    final Geometry result = d.getResult();
    final boolean equal = result.norm().equalsExact(expected.norm());
    if (!equal) {
      System.out.println("Expected = " + expected + " actual = "
        + result.norm());
    }
    assertTrue(equal);
  }

  private void checkDissolve(final String wkt, final String expectedWKT)
    throws ParseException {
    checkDissolve(new String[] {
      wkt
    }, expectedWKT);
  }

  private void checkDissolve(final String[] wkt, final String expectedWKT)
    throws ParseException {
    final List geoms = GeometryUtils.readWKT(wkt);
    final Geometry expected = GeometryUtils.readWKT(expectedWKT);
    checkDissolve(geoms, expected);
  }

  public void testDebug() throws ParseException {
    // testSingleLine();
    testIsolatedRing();
  }

  public void testDisjointLines() throws ParseException {
    checkDissolve("MULTILINESTRING ((0 0, 1 0, 2 1), (10 0, 11 0, 12 0))",
      "MULTILINESTRING ((0 0, 1 0, 2 1), (10 0, 11 0, 12 0))");
  }

  public void testDivergingLines() throws ParseException {
    checkDissolve(
      "MULTILINESTRING ((0 0, 1 0, 2 1), (0 0, 1 0, 2 0), (1 0, 2 1, 2 0, 3 0))",
      "MULTILINESTRING ((0 0, 1 0), (1 0, 2 0), (1 0, 2 1, 2 0), (2 0, 3 0))");
  }

  public void testIsolatedRing() throws ParseException {
    checkDissolve("LINESTRING (0 0, 1 1, 1 0, 0 0)",
      "LINESTRING (0 0, 1 1, 1 0, 0 0)");
  }

  public void testIsolateRingFromMultipleLineStrings() throws ParseException {
    checkDissolve("MULTILINESTRING ((0 0, 1 0, 1 1), (0 0, 0 1, 1 1))",
      "LINESTRING (0 0, 0 1, 1 1, 1 0, 0 0)");
  }

  public void testLollipop() throws ParseException {
    checkDissolve("LINESTRING (0 0, 1 0, 2 0, 2 1, 1 0, 0 0)",
      "MULTILINESTRING ((0 0, 1 0), (1 0, 2 0, 2 1, 1 0))");
  }

  public void testOneSegmentY() throws ParseException {
    checkDissolve("MULTILINESTRING ((0 0, 1 1, 2 2), (1 1, 1 2))",
      "MULTILINESTRING ((0 0, 1 1), (1 1, 2 2), (1 1, 1 2))");
  }

  public void testOverlappingLines3() throws ParseException {
    checkDissolve(new String[] {
      "LINESTRING (0 0, 1 1, 2 2)", "LINESTRING (1 1, 2 2, 3 3)",
      "LINESTRING (1 1, 2 2, 2 0)"
    }, "MULTILINESTRING ((0 0, 1 1, 2 2), (2 0, 2 2), (2 2, 3 3))");
  }

  public void testOverlappingTwoSegmentLines() throws ParseException {
    checkDissolve(new String[] {
      "LINESTRING (0 0, 1 1, 2 2)", "LINESTRING (1 1, 2 2, 3 3)"
    }, "LINESTRING (0 0, 1 1, 2 2, 3 3)");
  }

  /**
   * Shows that rings with incident lines are created with the correct node point.
   * 
   * @throws ParseException
   */
  public void testRingWithTail() throws ParseException {
    checkDissolve(
      "MULTILINESTRING ((0 0, 1 0, 1 1), (0 0, 0 1, 1 1), (1 0, 2 0))",
      "MULTILINESTRING ((1 0, 0 0, 0 1, 1 1, 1 0), (1 0, 2 0))");
  }

  public void testSingleLine() throws ParseException {
    checkDissolve("MULTILINESTRING ((0 0, 1 0, 2 1))",
      "LINESTRING (0 0, 1 0, 2 1)");
  }

  public void testSingleSegmentLine() throws ParseException {
    checkDissolve("LINESTRING (0 0, 1 1)", "LINESTRING (0 0, 1 1)");
  }

  public void testTwoSegmentLine() throws ParseException {
    checkDissolve("LINESTRING (0 0, 1 1, 2 2)", "LINESTRING (0 0, 1 1, 2 2)");
  }

  public void testTwoSegmentY() throws ParseException {
    checkDissolve(
      "MULTILINESTRING ((0 0, 9 9, 10 10, 11 11, 20 20), (10 10, 10 20))",
      "MULTILINESTRING ((10 20, 10 10), (10 10, 9 9, 0 0), (10 10, 11 11, 20 20))");
  }

}
