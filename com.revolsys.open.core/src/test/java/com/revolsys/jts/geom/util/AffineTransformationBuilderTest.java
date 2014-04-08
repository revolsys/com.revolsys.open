package com.revolsys.jts.geom.util;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Coordinate;

/**
 * Tests {@link AffineTransformationBuilder}.
 * 
 * @author Martin Davis
 */
public class AffineTransformationBuilderTest extends TestCase {
  private final Coordinate ctl0 = new Coordinate(-10, -10);

  private final Coordinate ctl1 = new Coordinate(10, 20);

  private final Coordinate ctl2 = new Coordinate(10, -20);

  public AffineTransformationBuilderTest(final String name) {
    super(name);
  }

  private void assertEqualPoint(final Coordinate p, final Coordinate q) {
    assertEquals(p.x, q.x, 0.00005);
    assertEquals(p.y, q.y, 0.00005);
  }

  void run(final double p0x, final double p0y, final double pp0x,
    final double pp0y) {
    final Coordinate p0 = new Coordinate(p0x, p0y);

    final Coordinate pp0 = new Coordinate(pp0x, pp0y);

    final AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(
      p0, pp0);

    final Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, trans.transform(p0, dest));
  }

  void run(final double p0x, final double p0y, final double p1x,
    final double p1y, final double pp0x, final double pp0y, final double pp1x,
    final double pp1y) {
    final Coordinate p0 = new Coordinate(p0x, p0y);
    final Coordinate p1 = new Coordinate(p1x, p1y);

    final Coordinate pp0 = new Coordinate(pp0x, pp0y);
    final Coordinate pp1 = new Coordinate(pp1x, pp1y);

    final AffineTransformation trans = AffineTransformationFactory.createFromControlVectors(
      p0, p1, pp0, pp1);

    final Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, trans.transform(p0, dest));
    assertEqualPoint(pp1, trans.transform(p1, dest));
  }

  void run(final double p0x, final double p0y, final double p1x,
    final double p1y, final double p2x, final double p2y, final double pp0x,
    final double pp0y, final double pp1x, final double pp1y, final double pp2x,
    final double pp2y) {
    final Coordinate p0 = new Coordinate(p0x, p0y);
    final Coordinate p1 = new Coordinate(p1x, p1y);
    final Coordinate p2 = new Coordinate(p2x, p2y);

    final Coordinate pp0 = new Coordinate(pp0x, pp0y);
    final Coordinate pp1 = new Coordinate(pp1x, pp1y);
    final Coordinate pp2 = new Coordinate(pp2x, pp2y);

    final AffineTransformationBuilder atb = new AffineTransformationBuilder(p0,
      p1, p2, pp0, pp1, pp2);
    final AffineTransformation trans = atb.getTransformation();

    final Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, trans.transform(p0, dest));
    assertEqualPoint(pp1, trans.transform(p1, dest));
    assertEqualPoint(pp2, trans.transform(p2, dest));
  }

  void runSingular(final double p0x, final double p0y, final double p1x,
    final double p1y, final double p2x, final double p2y, final double pp0x,
    final double pp0y, final double pp1x, final double pp1y, final double pp2x,
    final double pp2y) {
    final Coordinate p0 = new Coordinate(p0x, p0y);
    final Coordinate p1 = new Coordinate(p1x, p1y);
    final Coordinate p2 = new Coordinate(p2x, p2y);

    final Coordinate pp0 = new Coordinate(pp0x, pp0y);
    final Coordinate pp1 = new Coordinate(pp1x, pp1y);
    final Coordinate pp2 = new Coordinate(pp2x, pp2y);

    final AffineTransformationBuilder atb = new AffineTransformationBuilder(p0,
      p1, p2, pp0, pp1, pp2);
    final AffineTransformation trans = atb.getTransformation();
    assertEquals(trans, null);
  }

  private void runTransform(final AffineTransformation trans,
    final Coordinate p0, final Coordinate p1, final Coordinate p2) {
    final Coordinate pp0 = trans.transform(p0, new Coordinate());
    final Coordinate pp1 = trans.transform(p1, new Coordinate());
    final Coordinate pp2 = trans.transform(p2, new Coordinate());

    final AffineTransformationBuilder atb = new AffineTransformationBuilder(p0,
      p1, p2, pp0, pp1, pp2);
    final AffineTransformation atbTrans = atb.getTransformation();

    final Coordinate dest = new Coordinate();
    assertEqualPoint(pp0, atbTrans.transform(p0, dest));
    assertEqualPoint(pp1, atbTrans.transform(p1, dest));
    assertEqualPoint(pp2, atbTrans.transform(p2, dest));
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
