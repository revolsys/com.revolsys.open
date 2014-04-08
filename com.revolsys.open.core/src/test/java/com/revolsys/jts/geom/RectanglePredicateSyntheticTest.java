package com.revolsys.jts.geom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.io.WKTReader;

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

  private final WKTReader rdr = new WKTReader();

  private final GeometryFactory fact = new GeometryFactory();

  double baseX = 10;

  double baseY = 10;

  double rectSize = 20;

  double bufSize = 10;

  double testGeomSize = 10;

  double bufferWidth = 1.0;

  Envelope rectEnv = new Envelope(this.baseX, this.baseX + this.rectSize,
    this.baseY, this.baseY + this.rectSize);

  Geometry rect = this.fact.toGeometry(this.rectEnv);

  public RectanglePredicateSyntheticTest(final String name) {
    super(name);
  }

  public Geometry createAngle(final Coordinate base, final double size,
    final int quadrant) {
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

    final Coordinate p0 = new Coordinate(base.x + xFac * size, base.y + yFac
      * size);
    final Coordinate p2 = new Coordinate(base.x + yFac * size, base.y + -xFac
      * size);

    return this.fact.createLineString(new Coordinate[] {
      p0, base, p2
    });
  }

  public List createTestGeometries(final Envelope env, final double inc,
    final double size) {
    final List testGeoms = new ArrayList();

    for (double y = env.getMinY(); y <= env.getMaxY(); y += inc) {
      for (double x = env.getMinX(); x <= env.getMaxX(); x += inc) {
        final Coordinate base = new Coordinate(x, y);
        testGeoms.add(createAngle(base, size, 0));
        testGeoms.add(createAngle(base, size, 1));
        testGeoms.add(createAngle(base, size, 2));
        testGeoms.add(createAngle(base, size, 3));
      }
    }
    return testGeoms;
  }

  private List getTestGeometries() {
    final Envelope testEnv = new Envelope(
      this.rectEnv.getMinX() - this.bufSize, this.rectEnv.getMaxX()
        + this.bufSize, this.rectEnv.getMinY() - this.bufSize,
      this.rectEnv.getMaxY() + this.bufSize);
    final List testGeoms = createTestGeometries(testEnv, 5, this.testGeomSize);
    return testGeoms;
  }

  private void runRectanglePredicates(final Geometry rect,
    final Geometry testGeom) {
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

  public void testDenseLines() {
    System.out.println(this.rect);

    final List testGeoms = getTestGeometries();
    for (final Iterator i = testGeoms.iterator(); i.hasNext();) {
      final Geometry testGeom = (Geometry)i.next();

      final SegmentDensifier densifier = new SegmentDensifier(
        (LineString)testGeom);
      final LineString denseLine = (LineString)densifier.densify(this.testGeomSize / 400);

      runRectanglePredicates(this.rect, denseLine);
    }
  }

  public void testLines() {
    System.out.println(this.rect);

    final List testGeoms = getTestGeometries();
    for (final Iterator i = testGeoms.iterator(); i.hasNext();) {
      final Geometry testGeom = (Geometry)i.next();
      runRectanglePredicates(this.rect, testGeom);
    }
  }

  public void testPolygons() {
    final List testGeoms = getTestGeometries();
    for (final Iterator i = testGeoms.iterator(); i.hasNext();) {
      final Geometry testGeom = (Geometry)i.next();
      runRectanglePredicates(this.rect, testGeom.buffer(this.bufferWidth));
    }
  }
}
