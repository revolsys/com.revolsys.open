package com.revolsys.jts.testold.operation;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.valid.IsValidOp;

/**
 * Tests validating geometries with
 * non-closed rings.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ValidClosedRingTest extends TestCase {
  public static void main(final String[] args) {
    junit.textui.TestRunner.run(ValidClosedRingTest.class);
  }

  private static WKTReader rdr = new WKTReader();

  public ValidClosedRingTest(final String name) {
    super(name);
  }

  private void checkIsValid(final Geometry geom, final boolean expected) {
    final IsValidOp validator = new IsValidOp(geom);
    final boolean isValid = validator.isValid();
    assertTrue(isValid == expected);
  }

  Geometry fromWKT(final String wkt) {
    Geometry geom = null;
    try {
      geom = rdr.read(wkt);
    } catch (final Exception ex) {
      ex.printStackTrace();
    }
    return geom;
  }

  public void testGoodLinearRing() {
    final LinearRing ring = (LinearRing)fromWKT("LINEARRING (0 0, 0 10, 10 10, 10 0, 0 0)");
    checkIsValid(ring, true);
  }

  public void testGoodPolygon() {
    final Polygon poly = (Polygon)fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
    checkIsValid(poly, true);
  }

}
