package com.revolsys.jts.testold.linearref;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.linearref.LinearLocation;
import com.revolsys.jts.linearref.LocationIndexedLine;

/**
 * Tests the {@link LocationIndexedLine} class
 */
public class LocationIndexedLineTest extends AbstractIndexedLineTest {

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(LocationIndexedLineTest.class);
  }

  public LocationIndexedLineTest(final String name) {
    super(name);
  }

  @Override
  protected Point extractOffsetAt(final Geometry linearGeom,
    final Point testPt, final double offsetDistance) {
    final LocationIndexedLine indexedLine = new LocationIndexedLine(linearGeom);
    final LinearLocation index = indexedLine.indexOf(testPt);
    return indexedLine.extractPoint(index, offsetDistance);
  }

  @Override
  protected boolean indexOfAfterCheck(final Geometry linearGeom,
    final Point testPt) {
    final LocationIndexedLine indexedLine = new LocationIndexedLine(linearGeom);

    // check locations are consecutive
    final LinearLocation loc1 = indexedLine.indexOf(testPt);
    final LinearLocation loc2 = indexedLine.indexOfAfter(testPt, loc1);
    if (loc2.compareTo(loc1) <= 0) {
      return false;
    }

    // check extracted points are the same as the input
    final Point pt1 = indexedLine.extractPoint(loc1);
    final Point pt2 = indexedLine.extractPoint(loc2);
    if (!pt1.equals(2,testPt)) {
      return false;
    }
    if (!pt2.equals(2,testPt)) {
      return false;
    }
    return true;
  }

  @Override
  protected boolean indexOfAfterCheck(final Geometry linearGeom,
    final Point testPt, final Point afterPt) {
    final LocationIndexedLine indexedLine = new LocationIndexedLine(linearGeom);

    // check that computed location is after check location
    final LinearLocation afterLoc = indexedLine.indexOf(afterPt);
    final LinearLocation testLoc = indexedLine.indexOfAfter(testPt, afterLoc);
    if (testLoc.compareTo(afterLoc) < 0) {
      return false;
    }

    return true;
  }

  @Override
  protected Geometry indicesOfThenExtract(final Geometry input,
    final Geometry subLine) {
    final LocationIndexedLine indexedLine = new LocationIndexedLine(input);
    final LinearLocation[] loc = indexedLine.indicesOf(subLine);
    final Geometry result = indexedLine.extractLine(loc[0], loc[1]);
    return result;
  }

  private void runExtractLine(final String wkt, final LinearLocation start,
    final LinearLocation end, final String expected) {
    final Geometry geom = read(wkt);
    final LocationIndexedLine lil = new LocationIndexedLine(geom);
    final Geometry result = lil.extractLine(start, end);
    // System.out.println(result);
    checkExpected(result, expected);
  }

  public void testMultiLineString2() throws Exception {
    runExtractLine("MULTILINESTRING ((0 0, 10 10), (20 20, 30 30))",
      new LinearLocation(0, 0, 1.0), new LinearLocation(1, 0, .5),
      "MULTILINESTRING ((10 10, 10 10), (20 20, 25 25))");
  }

  public void testMultiLineStringSimple() throws Exception {
    runExtractLine("MULTILINESTRING ((0 0, 10 10), (20 20, 30 30))",
      new LinearLocation(0, 0, .5), new LinearLocation(1, 0, .5),
      "MULTILINESTRING ((5 5, 10 10), (20 20, 25 25))");
  }

}
