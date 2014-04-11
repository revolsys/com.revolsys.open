package com.revolsys.jts.testold.operation.valid;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
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
  private static WKTReader rdr = new WKTReader();

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(ValidClosedRingTest.class);
  }

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

  public void testBadGeometryCollection() {
    final GeometryCollection gc = (GeometryCollection)fromWKT("GEOMETRYCOLLECTION ( POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1) )), POINT(0 0) )");
    final Polygon poly = (Polygon)gc.getGeometry(0);
    updateNonClosedRing((LinearRing)poly.getInteriorRingN(0));
    checkIsValid(poly, false);
  }

  public void testBadLinearRing() {
    final LinearRing ring = (LinearRing)fromWKT("LINEARRING (0 0, 0 10, 10 10, 10 0, 0 0)");
    updateNonClosedRing(ring);
    checkIsValid(ring, false);
  }

  public void testBadPolygonHole() {
    final Polygon poly = (Polygon)fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0), (1 1, 2 1, 2 2, 1 2, 1 1) ))");
    updateNonClosedRing((LinearRing)poly.getInteriorRingN(0));
    checkIsValid(poly, false);
  }

  public void testBadPolygonShell() {
    final Polygon poly = (Polygon)fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
    updateNonClosedRing((LinearRing)poly.getExteriorRing());
    checkIsValid(poly, false);
  }

  public void testGoodLinearRing() {
    final LinearRing ring = (LinearRing)fromWKT("LINEARRING (0 0, 0 10, 10 10, 10 0, 0 0)");
    checkIsValid(ring, true);
  }

  public void testGoodPolygon() {
    final Polygon poly = (Polygon)fromWKT("POLYGON ((0 0, 0 10, 10 10, 10 0, 0 0))");
    checkIsValid(poly, true);
  }

  private void updateNonClosedRing(final LinearRing ring) {
    final Coordinates[] pts = ring.getCoordinateArray();
    pts[0].setX(pts[0].getX() + 0.0001);
  }
}
