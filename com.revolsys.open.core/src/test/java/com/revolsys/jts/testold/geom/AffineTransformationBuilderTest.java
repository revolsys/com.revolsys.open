package com.revolsys.jts.testold.geom;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.util.AffineTransformation;
import com.revolsys.jts.geom.util.AffineTransformationBuilder;
import com.revolsys.jts.geom.util.AffineTransformationFactory;

/**
 * Tests {@link AffineTransformationBuilder}.
 * 
 * @author Martin Davis
 */
public class AffineTransformationBuilderTest extends TestCase {
  private final Coordinates ctl0 = new Coordinate((double)-10, -10,
    Coordinates.NULL_ORDINATE);

  private final Coordinates ctl1 = new Coordinate((double)10, 20,
    Coordinates.NULL_ORDINATE);

  private final Coordinates ctl2 = new Coordinate((double)10, -20,
    Coordinates.NULL_ORDINATE);

  public AffineTransformationBuilderTest(final String name) {
    super(name);
  }

  private void assertEqualPoint(final Coordinates p, final Coordinates q) {
    assertEquals(p.getX(), q.getX(), 0.00005);
    assertEquals(p.getY(), q.getY(), 0.00005);
  }

  void run(final double p0x, final double p0y, final double pp0x,
    final double pp0y) {
    final Coordinates p0 = new Coordinate(p0x, p0y, Coordinates.NULL_ORDINATE);

    final Coordinates pp0 = new Coordinate(pp0x, pp0y,
      Coordinates.NULL_ORDINATE);

    final AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(
      p0, pp0);

    assertEqualPoint(pp0, trans.transform(p0));
  }

  void run(final double p0x, final double p0y, final double p1x,
    final double p1y, final double pp0x, final double pp0y, final double pp1x,
    final double pp1y) {
    final Coordinates p0 = new Coordinate(p0x, p0y, Coordinates.NULL_ORDINATE);
    final Coordinates p1 = new Coordinate(p1x, p1y, Coordinates.NULL_ORDINATE);

    final Coordinates pp0 = new Coordinate(pp0x, pp0y,
      Coordinates.NULL_ORDINATE);
    final Coordinates pp1 = new Coordinate(pp1x, pp1y,
      Coordinates.NULL_ORDINATE);

    final AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(
      p0, p1, pp0, pp1);

    assertEqualPoint(pp0, trans.transform(p0));
    assertEqualPoint(pp1, trans.transform(p1));
  }

  void run(final double p0x, final double p0y, final double p1x,
    final double p1y, final double p2x, final double p2y, final double pp0x,
    final double pp0y, final double pp1x, final double pp1y, final double pp2x,
    final double pp2y) {
    final Coordinates p0 = new Coordinate(p0x, p0y, Coordinates.NULL_ORDINATE);
    final Coordinates p1 = new Coordinate(p1x, p1y, Coordinates.NULL_ORDINATE);
    final Coordinates p2 = new Coordinate(p2x, p2y, Coordinates.NULL_ORDINATE);

    final Coordinates pp0 = new Coordinate(pp0x, pp0y,
      Coordinates.NULL_ORDINATE);
    final Coordinates pp1 = new Coordinate(pp1x, pp1y,
      Coordinates.NULL_ORDINATE);
    final Coordinates pp2 = new Coordinate(pp2x, pp2y,
      Coordinates.NULL_ORDINATE);

    final AffineTransformationBuilder atb = new AffineTransformationBuilder(p0,
      p1, p2, pp0, pp1, pp2);
    final AffineTransformation trans = atb.getTransformation();

    assertEqualPoint(pp0, trans.transform(p0));
    assertEqualPoint(pp1, trans.transform(p1));
    assertEqualPoint(pp2, trans.transform(p2));
  }

  void runSingular(final double p0x, final double p0y, final double p1x,
    final double p1y, final double p2x, final double p2y, final double pp0x,
    final double pp0y, final double pp1x, final double pp1y, final double pp2x,
    final double pp2y) {
    final Coordinates p0 = new Coordinate(p0x, p0y, Coordinates.NULL_ORDINATE);
    final Coordinates p1 = new Coordinate(p1x, p1y, Coordinates.NULL_ORDINATE);
    final Coordinates p2 = new Coordinate(p2x, p2y, Coordinates.NULL_ORDINATE);

    final Coordinates pp0 = new Coordinate(pp0x, pp0y,
      Coordinates.NULL_ORDINATE);
    final Coordinates pp1 = new Coordinate(pp1x, pp1y,
      Coordinates.NULL_ORDINATE);
    final Coordinates pp2 = new Coordinate(pp2x, pp2y,
      Coordinates.NULL_ORDINATE);

    final AffineTransformationBuilder atb = new AffineTransformationBuilder(p0,
      p1, p2, pp0, pp1, pp2);
    final AffineTransformation trans = atb.getTransformation();
    assertEquals(trans, null);
  }

  private void runTransform(final AffineTransformation trans,
    final Coordinates p0, final Coordinates p1, final Coordinates p2) {
    final Coordinates pp0 = trans.transform(p0);
    final Coordinates pp1 = trans.transform(p1);
    final Coordinates pp2 = trans.transform(p2);

    final AffineTransformationBuilder atb = new AffineTransformationBuilder(p0,
      p1, p2, pp0, pp1, pp2);
    final AffineTransformation atbTrans = atb.getTransformation();

    assertEqualPoint(pp0, atbTrans.transform(p0));
    assertEqualPoint(pp1, atbTrans.transform(p1));
    assertEqualPoint(pp2, atbTrans.transform(p2));
  }

  public void testDualControl_General() {
    run(0, 0, 1, 1, 5, 5, 6, 9);
  }

  public void testDualControl_Translation() {
    run(0, 0, 1, 1, 5, 5, 6, 6);
  }

  public void testLinear1() {
    run(0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 5, 7);
  }

  public void testRotate1() {
    run(0, 0, 1, 0, 0, 1, 0, 0, 0, 1, -1, 0);
  }

  public void testRotate2() {
    run(0, 0, 1, 0, 0, 1, 0, 0, 1, 1, -1, 1);
  }

  public void testScale1() {
    run(0, 0, 1, 0, 0, 1, 0, 0, 2, 0, 0, 2);
  }

  public void testSingleControl1() {
    run(0, 0, 5, 6);
  }

  public void testSingular1() {
    // points on a line mapping to non-collinear points - no solution
    runSingular(0, 0, 1, 1, 2, 2, 0, 0, 1, 2, 1, 3);
  }

  public void testSingular2() {
    // points on a line mapping to collinear points - not uniquely specified
    runSingular(0, 0, 1, 1, 2, 2, 0, 0, 10, 10, 30, 30);
  }

  public void testSingular3() {
    // points on a line mapping to collinear points - not uniquely specified
    runSingular(0, 0, 1, 1, 2, 2, 0, 0, 10, 10, 20, 20);
  }

  public void testTransform1() {
    final AffineTransformation trans = new AffineTransformation();
    trans.rotate(1);
    trans.translate(10, 10);
    trans.scale(2, 2);
    runTransform(trans, this.ctl0, this.ctl1, this.ctl2);
  }

  public void testTransform2() {
    final AffineTransformation trans = new AffineTransformation();
    trans.rotate(3);
    trans.translate(10, 10);
    trans.scale(2, 10);
    trans.shear(5, 2);
    trans.reflect(5, 8, 10, 2);
    runTransform(trans, this.ctl0, this.ctl1, this.ctl2);
  }

  public void testTranslate1() {
    run(0, 0, 1, 0, 0, 1, 5, 6, 6, 6, 5, 7);
  }

}
