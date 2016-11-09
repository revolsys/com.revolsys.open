/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.test.old.geom;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDouble;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Test named predicate short-circuits
 */
/**
 * @version 1.7
 */
public class LineSegmentTest extends TestCase {

  private static double ROOT2 = Math.sqrt(2);

  public static boolean equalsTolerance(final Point p0, final Point p1, final double tolerance) {
    if (Math.abs(p0.getX() - p1.getX()) > tolerance) {
      return false;
    }
    if (Math.abs(p0.getY() - p1.getY()) > tolerance) {
      return false;
    }
    return true;
  }

  public static void main(final String args[]) {
    TestRunner.run(LineSegmentTest.class);
  }

  GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  public LineSegmentTest(final String name) {
    super(name);
  }

  void checkOffset(final double x0, final double y0, final double x1, final double y1,
    final double segFrac, final double offset, final double expectedX, final double expectedY) {
    final LineSegment seg = new LineSegmentDouble(2, x0, y0, x1, y1);
    final Point p = seg.pointAlongOffset(segFrac, offset);

    assertTrue(equalsTolerance(new PointDoubleXY(expectedX, expectedY), p, 0.000001));
  }

  void checkOrientationIndex(final double x0, final double y0, final double x1, final double y1,
    final double px, final double py, final int expectedOrient) {
    final LineSegment seg = new LineSegmentDouble(2, x0, y0, x1, y1);
    checkOrientationIndex(seg, px, py, expectedOrient);
  }

  void checkOrientationIndex(final LineSegment seg, final double s0x, final double s0y,
    final double s1x, final double s1y, final int expectedOrient) {
    final LineSegment seg2 = new LineSegmentDouble(2, s0x, s0y, s1x, s1y);
    final int orient = seg.orientationIndex(seg2);
    assertTrue(orient == expectedOrient);
  }

  void checkOrientationIndex(final LineSegment seg, final double px, final double py,
    final int expectedOrient) {
    final Point p = new PointDoubleXY(px, py);
    final int orient = seg.orientationIndex(p);
    assertTrue(orient == expectedOrient);
  }

  public void testOffset() throws Exception {
    checkOffset(0, 0, 10, 10, 0.0, ROOT2, -1, 1);
    checkOffset(0, 0, 10, 10, 0.0, -ROOT2, 1, -1);

    checkOffset(0, 0, 10, 10, 1.0, ROOT2, 9, 11);
    checkOffset(0, 0, 10, 10, 0.5, ROOT2, 4, 6);

    checkOffset(0, 0, 10, 10, 0.5, -ROOT2, 6, 4);
    checkOffset(0, 0, 10, 10, 0.5, -ROOT2, 6, 4);

    checkOffset(0, 0, 10, 10, 2.0, ROOT2, 19, 21);
    checkOffset(0, 0, 10, 10, 2.0, -ROOT2, 21, 19);

    checkOffset(0, 0, 10, 10, 2.0, 5 * ROOT2, 15, 25);
    checkOffset(0, 0, 10, 10, -2.0, 5 * ROOT2, -25, -15);

  }

  public void testOrientationIndexCoordinate() {
    final LineSegment seg = new LineSegmentDouble(2, 0, 0, 10, 10);
    checkOrientationIndex(seg, 10, 11, 1);
    checkOrientationIndex(seg, 10, 9, -1);

    checkOrientationIndex(seg, 11, 11, 0);

    checkOrientationIndex(seg, 11, 11.0000001, 1);
    checkOrientationIndex(seg, 11, 10.9999999, -1);

    checkOrientationIndex(seg, -2, -1.9999999, 1);
    checkOrientationIndex(seg, -2, -2.0000001, -1);
  }

  public void testOrientationIndexSegment() {
    final LineSegment seg = new LineSegmentDouble(2, 100, 100, 110, 110);

    checkOrientationIndex(seg, 100, 101, 105, 106, 1);
    checkOrientationIndex(seg, 100, 99, 105, 96, -1);

    checkOrientationIndex(seg, 200, 200, 210, 210, 0);

  }

  public void testProjectionFactor() {
    // zero-length line
    final LineSegment seg = new LineSegmentDouble(2, 10, 0, 10, 0);
    assertTrue(Double.isNaN(seg.projectionFactor(new PointDoubleXY(11.0, 0))));

    final LineSegment seg2 = new LineSegmentDouble(2, 10, 0, 20, 0);
    assertTrue(seg2.projectionFactor(new PointDoubleXY(11.0, 0)) == 0.1);

  }

}
