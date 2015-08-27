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
package com.revolsys.jts.testold.algorithm;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RectangleLineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.PointDouble;

import junit.framework.TestCase;
import junit.framework.TestCase;
import junit.textui.TestRunner;
import junit.textui.TestRunner;

public class RectangleLineIntersectorTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(RectangleLineIntersectorTest.class);
  }

  public RectangleLineIntersectorTest(final String name) {
    super(name);
  }

  public void test300Points() {
    final RectangleLineIntersectorValidator test = new RectangleLineIntersectorValidator();
    test.init(300);
    assertTrue(test.validate());
  }
}

/**
 * Tests optimized RectangleLineIntersector against
 * a brute force approach (which is assumed to be correct).
 *
 * @author Martin Davis
 *
 */
class RectangleLineIntersectorValidator {
  private final double baseX = 0;

  private final double baseY = 0;

  private final GeometryFactory geomFact = GeometryFactory.floating3();

  private boolean isValid = true;

  private Point[] pts;

  private BoundingBox rectEnv;

  private final double rectSize = 100;

  public RectangleLineIntersectorValidator() {

  }

  private BoundingBox createRectangle() {
    final BoundingBox rectEnv = new BoundingBoxDoubleGf(
      new PointDouble(this.baseX, this.baseY, Point.NULL_ORDINATE),
      new PointDouble(this.baseX + this.rectSize, this.baseY + this.rectSize, Point.NULL_ORDINATE));
    return rectEnv;
  }

  private Point[] createTestPoints(final int nPts) {
    final Point pt = this.geomFact
      .point(new PointDouble(this.baseX, this.baseY, Point.NULL_ORDINATE));
    final Geometry circle = pt.buffer(2 * this.rectSize, nPts / 4);
    return CoordinatesListUtil.getCoordinateArray(circle);
  }

  public void init(final int nPts) {
    this.rectEnv = createRectangle();
    this.pts = createTestPoints(nPts);

  }

  public void run(final boolean useSegInt, final boolean useSideInt) {
    final RectangleLineIntersector rectSegIntersector = new RectangleLineIntersector(this.rectEnv);
    final SimpleRectangleIntersector rectSideIntersector = new SimpleRectangleIntersector(
      this.rectEnv);

    for (int i = 0; i < this.pts.length; i++) {
      for (int j = 0; j < this.pts.length; j++) {
        if (i == j) {
          continue;
        }

        boolean segResult = false;
        if (useSegInt) {
          segResult = rectSegIntersector.intersects(this.pts[i], this.pts[j]);
        }
        boolean sideResult = false;
        if (useSideInt) {
          sideResult = rectSideIntersector.intersects(this.pts[i], this.pts[j]);
        }

        if (useSegInt && useSideInt) {
          if (segResult != sideResult) {
            this.isValid = false;
          }
        }
      }
    }
  }

  public boolean validate() {
    run(true, true);
    return this.isValid;
  }

}

class SimpleRectangleIntersector {
  /**
   * The corners of the rectangle, in the order:
   *  10
   *  23
   */
  private final Point[] corner = new Point[4];

  // for intersection testing, don't need to set precision model
  private final LineIntersector li = new RobustLineIntersector();

  private final BoundingBox rectEnv;

  public SimpleRectangleIntersector(final BoundingBox rectEnv) {
    this.rectEnv = rectEnv;
    initCorners(rectEnv);
  }

  private void initCorners(final BoundingBox rectEnv) {
    this.corner[0] = new PointDouble(rectEnv.getMaxX(), rectEnv.getMaxY(), Point.NULL_ORDINATE);
    this.corner[1] = new PointDouble(rectEnv.getMinX(), rectEnv.getMaxY(), Point.NULL_ORDINATE);
    this.corner[2] = new PointDouble(rectEnv.getMinX(), rectEnv.getMinY(), Point.NULL_ORDINATE);
    this.corner[3] = new PointDouble(rectEnv.getMaxX(), rectEnv.getMinY(), Point.NULL_ORDINATE);
  }

  public boolean intersects(final Point p0, final Point p1) {
    final BoundingBoxDoubleGf segEnv = new BoundingBoxDoubleGf(p0, p1);
    if (!this.rectEnv.intersects(segEnv)) {
      return false;
    }

    this.li.computeIntersection(p0, p1, this.corner[0], this.corner[1]);
    if (this.li.hasIntersection()) {
      return true;
    }
    this.li.computeIntersection(p0, p1, this.corner[1], this.corner[2]);
    if (this.li.hasIntersection()) {
      return true;
    }
    this.li.computeIntersection(p0, p1, this.corner[2], this.corner[3]);
    if (this.li.hasIntersection()) {
      return true;
    }
    this.li.computeIntersection(p0, p1, this.corner[3], this.corner[0]);
    if (this.li.hasIntersection()) {
      return true;
    }

    return false;
  }

}
