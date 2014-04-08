package com.revolsys.jts.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.io.WKTReader;

/**
 * Test spatial predicate optimizations for rectangles.
 *
 * @version 1.7
 */

public class RectanglePredicateTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(RectanglePredicateTest.class);
  }

  private final WKTReader rdr = new WKTReader();

  private final GeometryFactory fact = GeometryFactory.getFactory();

  public RectanglePredicateTest(final String name) {
    super(name);
  }

  private void runRectanglePred(final Geometry rect, final Geometry testGeom) {
    final boolean intersectsValue = rect.intersects(testGeom);
    final boolean relateIntersectsValue = rect.relate(testGeom).isIntersects();
    final boolean intersectsOK = intersectsValue == relateIntersectsValue;

    final boolean containsValue = rect.contains(testGeom);
    final boolean relateContainsValue = rect.relate(testGeom).isContains();
    final boolean containsOK = containsValue == relateContainsValue;

    // System.out.println(testGeom);
    if (!intersectsOK || !containsOK) {
      System.out.println(testGeom);
    }
    assertTrue(intersectsOK);
    assertTrue(containsOK);
  }

  private void runRectanglePred(final String[] wkt) throws Exception {
    final Geometry rect = this.rdr.read(wkt[0]);
    final Geometry b = this.rdr.read(wkt[1]);
    runRectanglePred(rect, b);
  }

  public void testAngleOnBoundary() throws Exception {
    final String[] onBoundary = {
      "POLYGON ((10 10, 30 10, 30 30, 10 30, 10 10))",
      "LINESTRING (10 30, 10 10, 30 10)"
    };
    runRectanglePred(onBoundary);
  }

  public void testShortAngleOnBoundary() throws Exception {
    final String[] onBoundary = {
      "POLYGON ((10 10, 30 10, 30 30, 10 30, 10 10))",
      "LINESTRING (10 25, 10 10, 25 10)"
    };
    runRectanglePred(onBoundary);
  }

}
