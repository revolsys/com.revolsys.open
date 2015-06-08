package com.revolsys.jts.testold.linearref;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * Base class for linear referencing class unit tests.
 */
public abstract class AbstractIndexedLineTest extends TestCase {

  static final double TOLERANCE_DIST = 0.001;

  private final WKTReader reader = new WKTReader();

  public AbstractIndexedLineTest(final String name) {
    super(name);
  }

  protected void checkExpected(final Geometry result, final String expected) {
    final Geometry subLine = read(expected);
    final boolean isEqual = result.equalsExact(subLine, 1.0e-5);
    if (!isEqual) {
      // System.out.println("Computed result is: " + result);
    }
    assertTrue(isEqual);
  }

  protected abstract Point extractOffsetAt(Geometry input, Point testPt, double offsetDistance);

  /**
   * Checks that the point computed by <tt>indexOfAfter</tt>
   * is the same as the input point.
   * (This should be the case for all except pathological cases,
   * such as the input test point being beyond the end of the line).
   *
   * @param input
   * @param testPt
   * @return true if the result of indexOfAfter is the same as the input point
   */
  protected abstract boolean indexOfAfterCheck(Geometry input, Point testPt);

  protected abstract boolean indexOfAfterCheck(Geometry input, Point testPt, Point afterPt);

  protected abstract Geometry indicesOfThenExtract(Geometry input, Geometry subLine);

  protected Geometry read(final String wkt) {
    try {
      return this.reader.read(wkt);
    } catch (final ParseException ex) {
      throw new RuntimeException(ex);
    }
  }

  protected void runIndexOfAfterTest(final String inputStr, final String testPtWKT)
  // throws Exception
  {
    final Geometry input = read(inputStr);
    final Geometry testPoint = read(testPtWKT);
    final Point testPt = testPoint.getPoint();
    final boolean resultOK = indexOfAfterCheck(input, testPt);
    assertTrue(resultOK);
  }

  protected void runIndexOfAfterTest(final String inputStr, final String testPtWKT,
    final String afterPtWKT)
  // throws Exception
  {
    final Geometry input = read(inputStr);
    final Geometry testPoint = read(testPtWKT);
    final Point testPt = testPoint.getPoint();
    final Geometry afterPoint = read(afterPtWKT);
    final Point afterPt = afterPoint.getPoint();
    final boolean resultOK = indexOfAfterCheck(input, testPt, afterPt);
    assertTrue(resultOK);
  }

  protected void runIndicesOfThenExtract(final String inputStr, final String subLineStr)
  // throws Exception
  {
    final Geometry input = read(inputStr);
    final Geometry subLine = read(subLineStr);
    final Geometry result = indicesOfThenExtract(input, subLine);
    checkExpected(result, subLineStr);
  }

  protected void runOffsetTest(final String inputWKT, final String testPtWKT,
    final double offsetDistance, final String expectedPtWKT)
  // throws Exception
  {
    final Geometry input = read(inputWKT);
    final Geometry testPoint = read(testPtWKT);
    final Geometry expectedPoint = read(expectedPtWKT);
    final Point testPt = testPoint.getPoint();
    final Point expectedPt = expectedPoint.getPoint();
    final Point offsetPt = extractOffsetAt(input, testPt, offsetDistance);

    final boolean isOk = offsetPt.distance(expectedPt) < TOLERANCE_DIST;
    if (!isOk) {
      // System.out.println("Expected = " + expectedPoint + "  Actual = "
      // + offsetPt);
    }
    assertTrue(isOk);
  }

  public void testFirst() {
    runOffsetTest("LINESTRING (0 0, 20 20)", "POINT(20 20)", 0.0, "POINT (20 20)");
  }

  public void testIndexOfAfterBeyondEndRibbon() {
    runIndexOfAfterTest("LINESTRING (0 0, 0 60, 50 60, 50 20, -20 20)", "POINT (-30 20)",
      "POINT (-20 20)");
  }

  public void testIndexOfAfterRibbon() {
    runIndexOfAfterTest("LINESTRING (0 0, 0 60, 50 60, 50 20, -20 20)", "POINT (0 20)");
    runIndexOfAfterTest("LINESTRING (0 0, 0 60, 50 60, 50 20, -20 20)", "POINT (0 20)",
      "POINT (30 60)");
  }

  public void testIndexOfAfterSquare() {
    runIndexOfAfterTest("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)", "POINT (0 0)");
  }

  public void testLoopWithEndingSubLine() {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
      "LINESTRING (10 10, 10 0, 0 0)");
  }

  // test a subline equal to the parent loop
  public void testLoopWithIdenticalSubLine() {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
      "LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)");
  }

  /**
   * Following tests check that correct portion of loop is identified.
   * This requires that the correct vertex for (0,0) is selected.
   */

  public void testLoopWithStartSubLine() {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)",
      "LINESTRING (0 0, 0 10, 10 10)");
  }

  public void testML() {
    runIndicesOfThenExtract("MULTILINESTRING ((0 0, 10 10), (20 20, 30 30))",
      "MULTILINESTRING ((1 1, 10 10), (20 20, 25 25))");
  }

  public void testOffsetEndPoint() {
    runOffsetTest("LINESTRING (0 0, 20 20)", "POINT(20 20)", 0.0, "POINT (20 20)");
    runOffsetTest("LINESTRING (0 0, 13 13, 20 20)", "POINT(20 20)", 0.0, "POINT (20 20)");
    runOffsetTest("LINESTRING (0 0, 10 0, 20 0)", "POINT(20 0)", 1.0, "POINT (20 1)");
    runOffsetTest("LINESTRING (0 0, 20 0)", "POINT(10 0)", 1.0, "POINT (10 1)"); // point
    // on
    // last
    // segment
    runOffsetTest("MULTILINESTRING ((0 0, 10 0), (10 0, 20 0))", "POINT(10 0)", -1.0,
      "POINT (10 -1)");
    runOffsetTest("MULTILINESTRING ((0 0, 10 0), (10 0, 20 0))", "POINT(20 0)", 1.0, "POINT (20 1)");
  }

  /*
   * // example of indicesOfThenLocate method private Geometry
   * indicesOfThenLocate(LineString input, LineString subLine) {
   * LocationIndexedLine indexedLine = new LocationIndexedLine(input);
   * LineStringLocation[] loc = indexedLine.indicesOf(subLine); Geometry result
   * = indexedLine.locate(loc[0], loc[1]); return result; }
   */

  public void testOffsetStartPoint() {
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(0 0)", 1.0,
      "POINT (-0.7071067811865475 0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(0 0)", -1.0,
      "POINT (0.7071067811865475 -0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(10 10)", 5.0,
      "POINT (6.464466094067262 13.535533905932738)");
    runOffsetTest("LINESTRING (0 0, 10 10, 20 20)", "POINT(10 10)", -5.0,
      "POINT (13.535533905932738 6.464466094067262)");
  }

  public void testOffsetStartPointRepeatedPoint() {
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", 1.0,
      "POINT (-0.7071067811865475 0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", -1.0,
      "POINT (0.7071067811865475 -0.7071067811865475)");
    // These tests work for LengthIndexedLine, but not LocationIndexedLine
    // runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)",
    // 5.0, "POINT (6.464466094067262 13.535533905932738)");
    // runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)",
    // -5.0, "POINT (13.535533905932738 6.464466094067262)");
  }

  /**
   * Tests that duplicate coordinates are handled correctly.
   */
  public void testPartOfSegmentContainingDuplicateCoords() {
    runIndicesOfThenExtract("LINESTRING (0 0, 10 10, 10 10, 20 20)",
      "LINESTRING (5 5, 10 10, 10 10, 15 15)");
  }

  public void testPartOfSegmentContainingVertex() {
    runIndicesOfThenExtract("LINESTRING (0 0, 10 10, 20 20)", "LINESTRING (5 5, 10 10, 15 15)");
  }

  public void testPartOfSegmentNoVertex() {
    runIndicesOfThenExtract("LINESTRING (0 0, 10 10, 20 20)", "LINESTRING (1 1, 9 9)");
  }

  // test a zero-length subline equal to a mid point
  public void testZeroLenSubLineAtMidVertex() {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)", "LINESTRING (10 10, 10 10)");
  }

  // test a zero-length subline equal to the start point
  public void testZeroLenSubLineAtStart() {
    runIndicesOfThenExtract("LINESTRING (0 0, 0 10, 10 10, 10 0, 0 0)", "LINESTRING (0 0, 0 0)");
  }

}
