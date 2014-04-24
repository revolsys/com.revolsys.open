package com.revolsys.jts.testold.geom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
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

  private final GeometryFactory fact = GeometryFactory.getFactory();

  double baseX = 10;

  double baseY = 10;

  double rectSize = 20;

  double bufSize = 10;

  double testGeomSize = 10;

  double bufferWidth = 1.0;

  BoundingBox rectEnv = new Envelope(2, this.baseX,
    this.baseY, this.baseX + this.rectSize, this.baseY + this.rectSize);

  Geometry rect = this.fact.toGeometry(this.rectEnv);

  public RectanglePredicateSyntheticTest(final String name) {
    super(name);
  }

  public Geometry createAngle(final Coordinates base, final double size,
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

    final Coordinates p0 = new Coordinate(base.getX() + xFac * size,
      base.getY() + yFac * size, Coordinates.NULL_ORDINATE);
    final Coordinates p2 = new Coordinate(base.getX() + yFac * size,
      base.getY() + -xFac * size, Coordinates.NULL_ORDINATE);

    return this.fact.lineString(new Coordinates[] {
      p0, base, p2
    });
  }

  public List<Geometry> createTestGeometries(final BoundingBox env,
    final double inc, final double size) {
    final List<Geometry> testGeoms = new ArrayList<Geometry>();

    for (double y = env.getMinY(); y <= env.getMaxY(); y += inc) {
      for (double x = env.getMinX(); x <= env.getMaxX(); x += inc) {
        final Coordinates base = new Coordinate(x, y, Coordinates.NULL_ORDINATE);
        testGeoms.add(createAngle(base, size, 0));
        testGeoms.add(createAngle(base, size, 1));
        testGeoms.add(createAngle(base, size, 2));
        testGeoms.add(createAngle(base, size, 3));
      }
    }
    return testGeoms;
  }

  private List<Geometry> getTestGeometries() {
    final BoundingBox testEnv = new Envelope(
      2, this.rectEnv.getMinX() - this.bufSize, this.rectEnv.getMinY() - this.bufSize,
      this.rectEnv.getMaxX()
          + this.bufSize, this.rectEnv.getMaxY() + this.bufSize);
    final List<Geometry> testGeoms = createTestGeometries(testEnv, 5,
      this.testGeomSize);
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

    final List<Geometry> testGeoms = getTestGeometries();
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
