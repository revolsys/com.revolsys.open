package com.revolsys.geometry.test.old.geom;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDouble;

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

  double baseX = 10;

  double baseY = 10;

  double bufferWidth = 1.0;

  double bufSize = 10;

  private final GeometryFactory fact = GeometryFactory.DEFAULT_3D;

  BoundingBox rectEnv = new BoundingBoxDoubleXY(this.baseX, this.baseY, this.baseX + this.rectSize,
    this.baseY + this.rectSize);

  Geometry rect = this.rectEnv.toGeometry();

  double rectSize = 20;

  double testGeomSize = 10;

  public RectanglePredicateSyntheticTest(final String name) {
    super(name);
  }

  private List<Geometry> getTestGeometries() {
    final BoundingBox testEnv = new BoundingBoxDoubleXY(this.rectEnv.getMinX() - this.bufSize,
      this.rectEnv.getMinY() - this.bufSize, this.rectEnv.getMaxX() + this.bufSize,
      this.rectEnv.getMaxY() + this.bufSize);
    final List<Geometry> testGeoms = newTestGeometries(testEnv, 5, this.testGeomSize);
    return testGeoms;
  }

  public Geometry newAngle(final Point base, final double size, final int quadrant) {
    final int[][] factor = {
      {
        1, 0
      }, {
        0, 1
      }, {
        -1, 0
      }, {
        0, -1
      }
    };

    final int xFac = factor[quadrant][0];
    final int yFac = factor[quadrant][1];

    final Point p0 = new PointDouble(base.getX() + xFac * size, base.getY() + yFac * size);
    final Point p2 = new PointDouble(base.getX() + yFac * size, base.getY() + -xFac * size);

    return this.fact.lineString(new Point[] {
      p0, base, p2
    });
  }

  public List<Geometry> newTestGeometries(final BoundingBox env, final double inc,
    final double size) {
    final List<Geometry> testGeoms = new ArrayList<>();

    for (double y = env.getMinY(); y <= env.getMaxY(); y += inc) {
      for (double x = env.getMinX(); x <= env.getMaxX(); x += inc) {
        final Point base = new PointDouble(x, y);
        testGeoms.add(newAngle(base, size, 0));
        testGeoms.add(newAngle(base, size, 1));
        testGeoms.add(newAngle(base, size, 2));
        testGeoms.add(newAngle(base, size, 3));
      }
    }
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
