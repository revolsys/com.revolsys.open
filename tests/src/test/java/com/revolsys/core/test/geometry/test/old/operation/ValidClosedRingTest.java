package com.revolsys.core.test.geometry.test.old.operation;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.operation.valid.IsValidOp;

import junit.framework.TestCase;

/**
 * Tests validating geometries with
 * non-closed rings.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class ValidClosedRingTest extends TestCase {
  private static GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

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
      geom = geometryFactory.geometry(wkt);
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
