package com.revolsys.jts.testold.geom;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Test spatial predicate optimizations for rectangles by
 * synthesizing an exhaustive set of test cases.
 *
 * @version 1.7
 */
public class RectanglePredicateSyntheticTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(RectanglePredicateSyntheticTest.class);
  }

  private final GeometryFactory fact = GeometryFactory.floating3();

  double baseX = 10;

  double baseY = 10;

  double rectSize = 20;

  double bufSize = 10;

  double testGeomSize = 10;

  double bufferWidth = 1.0;

  BoundingBox rectEnv = new BoundingBoxDoubleGf(2, this.baseX, this.baseY,
    this.baseX + this.rectSize, this.baseY + this.rectSize);

  Geometry rect = this.rectEnv.toGeometry();

  public RectanglePredicateSyntheticTest(final String name) {
    super(name);
  }

  public Geometry createAngle(final Point base, final double size, final int quadrant) {
    final int[][] factor = {
      {
        1, 0
        },
      {
        0, 1
        },
      {
        -1, 0
        },
      {
        0, -1
        }
    };

    final int xFac = factor[quadrant][0];
    final int yFac = factor[quadrant][1];

    final Point p0 = new PointDouble(base.getX() + xFac * size, base.getY() + yFac * size,
      Point.NULL_ORDINATE);
    final Point p2 = new PointDouble(base.getX() + yFac * size, base.getY() + -xFac * size,
      Point.NULL_ORDINATE);

    return this.fact.lineString(new Point[] {
      p0, base, p2
    });
  }

  public List<Geometry> createTestGeometries(final BoundingBox env, final double inc,
    final double size) {
    final List<Geometry> testGeoms = new ArrayList<Geometry>();

    for (double y = env.getMinY(); y <= env.getMaxY(); y += inc) {
      for (double x = env.getMinX(); x <= env.getMaxX(); x += inc) {
        final Point base = new PointDouble(x, y, Point.NULL_ORDINATE);
        testGeoms.add(createAngle(base, size, 0));
        testGeoms.add(createAngle(base, size, 1));
        testGeoms.add(createAngle(base, size, 2));
        testGeoms.add(createAngle(base, size, 3));
      }
    }
    return testGeoms;
  }

  private List<Geometry> getTestGeometries() {
    final BoundingBox testEnv = new BoundingBoxDoubleGf(2, this.rectEnv.getMinX() - this.bufSize,
      this.rectEnv.getMinY() - this.bufSize, this.rectEnv.getMaxX() + this.bufSize,
      this.rectEnv.getMaxY() + this.bufSize);
    final List<Geometry> testGeoms = createTestGeometries(testEnv, 5, this.testGeomSize);
    return testGeoms;
  }

  private void runRectanglePredicates(final Geometry rect, final Geometry testGeom) {
    final boolean intersectsValue = rect.intersects(testGeom);
    final boolean relateIntersectsValue = rect.relate(testGeom).isIntersects();
    final boolean intersectsOK = intersectsValue == relateIntersectsValue;

    final boolean containsValue = rect.contains(testGeom);
    final boolean relateContainsValue = rect.relate(testGeom).isContains();
    final boolean containsOK = containsValue == relateContainsValue;

    // System.out.println(testGeom);
    if (!intersectsOK || !containsOK) {
      // System.out.println(testGeom);
    }
    assertTrue(intersectsOK);
    assertTrue(containsOK);
  }

  public void testDenseLines() {
    // System.out.println(this.rect);

    final List<Geometry> testGeoms = getTestGeometries();
    for (final Object element : testGeoms) {
      final Geometry testGeom = (Geometry)element;

      final SegmentDensifier densifier = new SegmentDensifier((LineString)testGeom);
      final LineString denseLine = (LineString)densifier.densify(this.testGeomSize / 400);

      runRectanglePredicates(this.rect, denseLine);
    }
  }

  public void testLines() {
    // System.out.println(this.rect);

    final List<Geometry> testGeoms = getTestGeometries();
    for (final Geometry testGeom : testGeoms) {
      runRectanglePredicates(this.rect, testGeom);
    }
  }

  public void testPolygons() {
    final List<Geometry> testGeoms = getTestGeometries();
    for (final Geometry testGeom : testGeoms) {
      runRectanglePredicates(this.rect, testGeom.buffer(this.bufferWidth));
    }
  }
}
