package com.revolsys.core.test.geometry.test.old.linearref;

import com.revolsys.geometry.linearref.LengthIndexedLine;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;

/**
 * Tests the {@link LengthIndexedLine} class
 */
public class LengthIndexedLineTest extends AbstractIndexedLineTest {

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(LengthIndexedLineTest.class);
  }

  public LengthIndexedLineTest(final String name) {
    super(name);
  }

  private void checkExtractLine(final String wkt, final double start, final double end,
    final String expected) {
    final Geometry linearGeom = read(wkt);
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final Geometry result = indexedLine.extractLine(start, end);
    checkExpected(result, expected);
  }

  @Override
  protected Point extractOffsetAt(final Geometry linearGeom, final Point testPt,
    final double offsetDistance) {
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final double index = indexedLine.indexOf(testPt);
    return indexedLine.extractPoint(index, offsetDistance);
  }

  @Override
  protected boolean indexOfAfterCheck(final Geometry linearGeom, final Point testPt) {
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);

    // check locations are consecutive
    final double loc1 = indexedLine.indexOf(testPt);
    final double loc2 = indexedLine.indexOfAfter(testPt, loc1);
    if (loc2 <= loc1) {
      return false;
    }

    // check extracted points are the same as the input
    final Point pt1 = indexedLine.extractPoint(loc1);
    final Point pt2 = indexedLine.extractPoint(loc2);
    if (!pt1.equals(2, testPt)) {
      return false;
    }
    if (!pt2.equals(2, testPt)) {
      return false;
    }

    return true;
  }

  @Override
  protected boolean indexOfAfterCheck(final Geometry linearGeom, final Point testPt,
    final Point checkPt) {
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);

    // check that computed location is after check location
    final double checkLoc = indexedLine.indexOf(checkPt);
    final double testLoc = indexedLine.indexOfAfter(testPt, checkLoc);
    if (testLoc < checkLoc) {
      return false;
    }

    return true;
  }

  @Override
  protected Geometry indicesOfThenExtract(final Geometry linearGeom, final Geometry subLine) {
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final double[] loc = indexedLine.indicesOf(subLine);
    final Geometry result = indexedLine.extractLine(loc[0], loc[1]);
    return result;
  }

  /**
   * Tests that z values are interpolated
   *
   */
  public void testComputeZ() {
    final Geometry linearGeom = read("LINESTRINGZ(0 0 0, 10 10 10)");
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final double projIndex = indexedLine.project(new PointDoubleXY(5, 5));
    final Point projPt = indexedLine.extractPoint(projIndex);
    // System.out.println(projPt);
    assertTrue(projPt.equals(3, new PointDoubleXYZ(5.0, 5, 5)));
  }

  /**
   * Tests that if the input does not have Z ordinates, neither does the output.
   *
   */
  public void testComputeZNaN() {
    final Geometry linearGeom = read("LINESTRING (0 0, 10 10 10)");
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final double projIndex = indexedLine.project(new PointDoubleXY(5, 5));
    final Point projPt = indexedLine.extractPoint(projIndex);
    assertTrue(Double.isNaN(projPt.getZ()));
  }

  public void testExtractLineBeyondRange() {
    checkExtractLine("LINESTRING (0 0, 10 10)", -100, 100, "LINESTRING (0 0, 10 10)");
  }

  public void testExtractLineBothIndicesAtEndpoint() {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))", 10, 10,
      "LINESTRING (10 0, 10 0)");
  }

  public void testExtractLineBothIndicesAtEndpointNegative() {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))", -10, 10,
      "LINESTRING (10 0, 10 0)");
  }

  public void testExtractLineBothIndicesAtEndpointXXX() {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))", -10, 10,
      "LINESTRING (10 0, 10 0)");
  }

  public void testExtractLineIndexAtEndpoint() {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))", 10, -1,
      "LINESTRING (20 0, 25 0, 29 0)");
  }

  /**
   * Tests that leading and trailing zero-length sublines are trimmed in the computed result,
   * and that zero-length extracts return the lowest extracted zero-length line
   */
  public void testExtractLineIndexAtEndpointWithZeroLenComponents() {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))", 10, -1,
      "LINESTRING (20 0, 25 0, 29 0)");
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))", 5, 10,
      "LINESTRING (5 0, 10 0)");
    checkExtractLine(
      "MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))", 10, 10,
      "LINESTRING (10 0, 10 0)");
    checkExtractLine(
      "MULTILINESTRING ((0 0, 10 0), (10 0, 10 0), (10 0, 10 0), (10 0, 10 0), (20 0, 25 0, 30 0))",
      10, -10, "LINESTRING (10 0, 10 0)");
  }

  public void testExtractLineNegative() {
    checkExtractLine("LINESTRING (0 0, 10 0)", -9, -1, "LINESTRING (1 0, 9 0)");
  }

  public void testExtractLineNegativeReverse() {
    checkExtractLine("LINESTRING (0 0, 10 0)", -1, -9, "LINESTRING (9 0, 1 0)");
  }

  public void testExtractLineReverse() {
    checkExtractLine("LINESTRING (0 0, 10 0)", 9, 1, "LINESTRING (9 0, 1 0)");
  }

  public void testExtractLineReverseMulti() {
    checkExtractLine("MULTILINESTRING ((0 0, 10 0), (20 0, 25 0, 30 0))", 19, 1,
      "MULTILINESTRING ((29 0, 25 0, 20 0), (10 0, 1 0))");
  }

  public void testExtractPointBeyondRange() {
    final Geometry linearGeom = read("LINESTRING (0 0, 10 10)");
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final Point pt = indexedLine.extractPoint(100);
    assertTrue(pt.equals(new PointDoubleXY(10, 10)));

    final Point pt2 = indexedLine.extractPoint(0);
    assertTrue(pt2.equals(new PointDoubleXY(0, 0)));
  }

  /**
   * These tests work for LengthIndexedLine, but not LocationIndexedLine
   *
   */
  @Override
  public void testOffsetStartPointRepeatedPoint() {
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", 1.0,
      "POINT (-0.7071067811865475 0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(0 0)", -1.0,
      "POINT (0.7071067811865475 -0.7071067811865475)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)", 5.0,
      "POINT (6.464466094067262 13.535533905932738)");
    runOffsetTest("LINESTRING (0 0, 10 10, 10 10, 20 20)", "POINT(10 10)", -5.0,
      "POINT (13.535533905932738 6.464466094067262)");
  }

  /**
   * From GEOS Ticket #323
   */
  public void testProjectExtractPoint() {
    final Geometry linearGeom = read("MULTILINESTRING ((0 2, 0 0), (-1 1, 1 1))");
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final double index = indexedLine.project(new PointDoubleXY(1, 0));
    final Point pt = indexedLine.extractPoint(index);
    assertTrue(pt.equals(new PointDoubleXY(0, 0)));
  }

  public void testProjectPointWithDuplicateCoords() {
    final Geometry linearGeom = read("LINESTRING (0 0, 10 0, 10 0, 20 0)");
    final LengthIndexedLine indexedLine = new LengthIndexedLine(linearGeom);
    final double projIndex = indexedLine.project(new PointDoubleXY(10, 1));
    assertTrue(projIndex == 10.0);
  }

}
