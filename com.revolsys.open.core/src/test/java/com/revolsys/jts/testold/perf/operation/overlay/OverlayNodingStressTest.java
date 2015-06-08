package com.revolsys.jts.testold.perf.operation.overlay;

import java.util.Random;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.operation.overlay.snap.SnapIfNeededOverlayOp;

/**
 * Tests Noding checking during overlay.
 * Intended to show that noding check failures due to robustness
 * problems do not occur very often (i.e. that the heuristic is
 * not triggering so often that a large performance penalty would be incurred.)
 *
 * The class generates test geometries for input to overlay which contain almost parallel lines
 * - this should cause noding failures relatively frequently.
 *
 * Can also be used to check that the cross-snapping heuristic fix for robustness
 * failures works well.  If snapping ever fails to fix a case,
 * an exception is thrown.  It is expected (and has been observed)
 * that cross-snapping works extremely well on this dataset.
 *
 * @version 1.7
 */
public class OverlayNodingStressTest extends TestCase {
  private static final int ITER_LIMIT = 10000;

  private static final int BATCH_SIZE = 20;

  private static final double MAX_DISPLACEMENT = 60;

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(OverlayNodingStressTest.class);
  }

  private final Random rand = new Random((long)(Math.PI * 10e8));

  private Geometry baseAccum = null;

  private int geomCount = 0;

  public OverlayNodingStressTest(final String name) {
    super(name);
  }

  public void checkIntersection(final Geometry base, final Geometry testGeom) {

    // this line can be used to test for the presence of noding failures for
    // non-tricky cases
    // Geometry star = rr2;
    // System.out.println("Star:");
    // System.out.println(base);
    // System.out.println("Rectangle:");
    // System.out.println(testGeom);

    // test to see whether the basic overlay code fails
    try {
      final Geometry intTrial = base.intersection(testGeom);
    } catch (final Exception ex) {
    }

    // this will throw an intersection if a robustness error occurs,
    // stopping the run
    final Geometry intersection = SnapIfNeededOverlayOp.intersection(base, testGeom);
    // System.out.println("Intersection:");
    // System.out.println(intersection);
  }

  public Geometry[] generateGeometryAccum(final double angle1, final double angle2) {
    final RotatedRectangleFactory rrFact = new RotatedRectangleFactory();
    final double basex = angle2 * MAX_DISPLACEMENT - MAX_DISPLACEMENT / 2;
    final Point base = new PointDouble(basex, basex, Point.NULL_ORDINATE);
    final Polygon rr1 = rrFact.createRectangle(100, 20, angle1, base);

    // limit size of accumulated star
    this.geomCount++;
    if (this.geomCount >= BATCH_SIZE) {
      this.geomCount = 0;
    }
    if (this.geomCount == 0) {
      this.baseAccum = null;
    }

    if (this.baseAccum == null) {
      this.baseAccum = rr1;
    } else {
      // this line can be used to test for the presence of noding failures for
      // non-tricky cases
      // Geometry star = rr2;
      this.baseAccum = rr1.union(this.baseAccum);
    }
    return new Geometry[] {
      this.baseAccum, rr1
    };
  }

  public Geometry[] generateGeometryStar(final double angle1, final double angle2) {
    final RotatedRectangleFactory rrFact = new RotatedRectangleFactory();
    final Polygon rr1 = rrFact.createRectangle(100, 20, angle1);
    final Polygon rr2 = rrFact.createRectangle(100, 20, angle2);

    // this line can be used to test for the presence of noding failures for
    // non-tricky cases
    // Geometry star = rr2;
    final Geometry star = rr1.union(rr2);
    return new Geometry[] {
      star, rr1
    };
  }

  private double getRand() {
    final double r = this.rand.nextDouble();
    return r;
  }

  public void testNoding() {
    final int iterLimit = ITER_LIMIT;
    for (int i = 0; i < iterLimit; i++) {
      // System.out.println("Iter: " + i + "  Noding failure count = "
      // + this.failureCount);
      final double ang1 = getRand() * Math.PI;
      final double ang2 = getRand() * Math.PI;
      // Geometry[] geom = generateGeometryStar(ang1, ang2);
      final Geometry[] geom = generateGeometryAccum(ang1, ang2);
      checkIntersection(geom[0], geom[1]);
    }
    // System.out.println("Test count = " + iterLimit
    // + "  Noding failure count = " + this.failureCount);
  }
}

class RotatedRectangleFactory {
  private static double PI_OVER_2 = Math.PI / 2;

  private final GeometryFactory fact = GeometryFactory.floating3();

  public RotatedRectangleFactory() {

  }

  public Polygon createRectangle(final double length, final double width, final double angle) {
    return createRectangle(length, width, angle, new PointDouble((double)0, 0, Point.NULL_ORDINATE));
  }

  public Polygon createRectangle(final double length, final double width, final double angle,
    final Point base) {
    final double posx = length / 2 * Math.cos(angle);
    final double posy = length / 2 * Math.sin(angle);
    final double negx = -posx;
    final double negy = -posy;
    final double widthOffsetx = width / 2 * Math.cos(angle + PI_OVER_2);
    final double widthOffsety = width / 2 * Math.sin(angle + PI_OVER_2);

    final Point[] pts = new Point[] {
      new PointDouble(base.getX() + posx + widthOffsetx, base.getY() + posy + widthOffsety,
        Point.NULL_ORDINATE),
      new PointDouble(base.getX() + posx - widthOffsetx, base.getY() + posy - widthOffsety,
        Point.NULL_ORDINATE),
      new PointDouble(base.getX() + negx - widthOffsetx, base.getY() + negy - widthOffsety,
        Point.NULL_ORDINATE),
      new PointDouble(base.getX() + negx + widthOffsetx, base.getY() + negy + widthOffsety,
        Point.NULL_ORDINATE), new PointDouble(0.0, 0, Point.NULL_ORDINATE),
    };
    // close polygon
    pts[4] = new PointDouble(pts[0]);
    final Polygon poly = this.fact.polygon(this.fact.linearRing(pts));
    return poly;
  }

}
