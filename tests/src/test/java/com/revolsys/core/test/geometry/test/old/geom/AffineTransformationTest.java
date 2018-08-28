package com.revolsys.core.test.geometry.test.old.geom;

import java.io.IOException;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.util.AffineTransformation;
import com.revolsys.geometry.model.util.NoninvertibleTransformationException;
import com.revolsys.geometry.wkb.ParseException;

import junit.framework.TestCase;

/**
 * @author Martin Davis
 *
 */
public class AffineTransformationTest extends TestCase {
  static GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  public AffineTransformationTest(final String name) {
    super(name);
  }

  void checkTransformation(final AffineTransformation trans0, final AffineTransformation trans1) {
    final double[] m0 = trans0.getMatrixEntries();
    final double[] m1 = trans1.getMatrixEntries();
    for (int i = 0; i < m0.length; i++) {
      assertEquals(m0[i], m1[i], 0.000005);
    }
  }

  /**
   * Checks that a transformation produces the expected result
   * @param x the input pt x
   * @param y the input pt y
   * @param trans the transformation
   * @param xp the expected output x
   * @param yp the expected output y
   */
  void checkTransformation(final double x, final double y, final AffineTransformation trans,
    final double xp, final double yp) {
    final Point p = new PointDoubleXY(x, y);
    final Point p2 = trans.transform(p);

    assertEquals(xp, p2.getX(), .00005);
    assertEquals(yp, p2.getY(), .00005);

    // if the transformation is invertible, test the inverse
    try {
      final AffineTransformation invTrans = trans.getInverse();
      final Point pInv = invTrans.transform(p2);

      assertEquals(x, pInv.getX(), .00005);
      assertEquals(y, pInv.getY(), .00005);

      final double det = trans.getDeterminant();
      final double detInv = invTrans.getDeterminant();
      assertEquals(det, 1.0 / detInv, .00005);

    } catch (final NoninvertibleTransformationException ex) {
    }
  }

  void checkTransformation(final String geomStr)
    throws IOException, ParseException, NoninvertibleTransformationException {
    final Geometry geom = geometryFactory.geometry(geomStr);
    final AffineTransformation trans = AffineTransformation.rotationInstance(Math.PI / 2);
    final AffineTransformation inv = trans.getInverse();
    final Geometry transGeom = trans.transform(geom);
    final Geometry invGeom = inv.transform(transGeom);
    // check if transformed geometry is equal to original
    final boolean isEqual = geom.equalsExact(invGeom, 0.0005);
    assertTrue(isEqual);
  }

  public void testCompose1() {
    final AffineTransformation t0 = AffineTransformation.translationInstance(10, 0);
    t0.rotate(Math.PI / 2);
    t0.translate(0, -10);

    final AffineTransformation t1 = AffineTransformation.translationInstance(0, 0);
    t1.rotate(Math.PI / 2);

    checkTransformation(t0, t1);
  }

  public void testCompose2() {
    final AffineTransformation t0 = AffineTransformation.reflectionInstance(0, 0, 1, 0);
    t0.reflect(0, 0, 0, -1);

    final AffineTransformation t1 = AffineTransformation.rotationInstance(Math.PI);

    checkTransformation(t0, t1);
  }

  public void testCompose3() {
    final AffineTransformation t0 = AffineTransformation.reflectionInstance(0, 10, 10, 0);
    t0.translate(-10, -10);

    final AffineTransformation t1 = AffineTransformation.reflectionInstance(0, 0, -1, 1);

    checkTransformation(t0, t1);
  }

  public void testComposeRotation1() {
    final AffineTransformation t0 = AffineTransformation.rotationInstance(1, 10, 10);

    final AffineTransformation t1 = AffineTransformation.translationInstance(-10, -10);
    t1.rotate(1);
    t1.translate(10, 10);

    checkTransformation(t0, t1);
  }

  public void testGeometryCollection()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation(
      "GEOMETRYCOLLECTION ( POINT ( 1 1), LINESTRING (0 0, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0)))");
  }

  public void testLineString()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation("LINESTRING (1 2, 10 20, 100 200)");
  }

  public void testMultiLineString()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation("MULTILINESTRING ((0 0, 1 10), (10 10, 20 30), (123 123, 456 789))");
  }

  public void testMultiPoint()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation("MULTIPOINT (0 0, 1 4, 100 200)");
  }

  public void testMultiPolygon()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation(
      "MULTIPOLYGON ( ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1)), ((200 200, 200 250, 250 250, 250 200, 200 200)))");
  }

  public void testNestedGeometryCollection()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation(
      "GEOMETRYCOLLECTION ( POINT (20 20), GEOMETRYCOLLECTION ( POINT ( 1 1), LINESTRING (0 0, 10 10), POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))) )");
  }

  public void testPolygon()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation("POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0))");
  }

  public void testPolygonWithHole()
    throws IOException, ParseException, NoninvertibleTransformationException {
    checkTransformation(
      "POLYGON ((0 0, 100 0, 100 100, 0 100, 0 0), (1 1, 1 10, 10 10, 10 1, 1 1))");
  }

  public void testReflectXY1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.reflectionInstance(1, 1);
    checkTransformation(10, 0, t, 0, 10);
    checkTransformation(0, 10, t, 10, 0);
    checkTransformation(-10, -10, t, -10, -10);
    checkTransformation(-3, -4, t, -4, -3);
  }

  public void testReflectXY2() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.reflectionInstance(1, -1);
    checkTransformation(10, 0, t, 0, -10);
    checkTransformation(0, 10, t, -10, 0);
    checkTransformation(-10, -10, t, 10, 10);
    checkTransformation(-3, -4, t, 4, 3);
  }

  public void testReflectXYXY1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.reflectionInstance(0, 5, 5, 0);
    checkTransformation(5, 0, t, 5, 0);
    checkTransformation(0, 0, t, 5, 5);
    checkTransformation(-10, -10, t, 15, 15);
  }

  public void testRotate1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.rotationInstance(Math.PI / 2);
    checkTransformation(10, 0, t, 0, 10);
    checkTransformation(0, 10, t, -10, 0);
    checkTransformation(-10, -10, t, 10, -10);
  }

  public void testRotateAroundPoint1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.rotationInstance(Math.PI / 2, 1, 1);
    checkTransformation(1, 1, t, 1, 1);
    checkTransformation(10, 0, t, 2, 10);
    checkTransformation(0, 10, t, -8, 0);
    checkTransformation(-10, -10, t, 12, -10);
  }

  public void testScale1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.scaleInstance(2, 3);
    checkTransformation(10, 0, t, 20, 0);
    checkTransformation(0, 10, t, 0, 30);
    checkTransformation(-10, -10, t, -20, -30);
  }

  public void testShear1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.shearInstance(2, 3);
    checkTransformation(10, 0, t, 10, 30);
  }

  public void testTranslate1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.translationInstance(2, 3);
    checkTransformation(1, 0, t, 3, 3);
    checkTransformation(0, 0, t, 2, 3);
    checkTransformation(-10, -5, t, -8, -2);
  }

  public void testTranslateRotate1() throws IOException, ParseException {
    final AffineTransformation t = AffineTransformation.translationInstance(3, 3)
      .rotate(Math.PI / 2);
    checkTransformation(10, 0, t, -3, 13);
    checkTransformation(-10, -10, t, 7, -7);
  }
}
