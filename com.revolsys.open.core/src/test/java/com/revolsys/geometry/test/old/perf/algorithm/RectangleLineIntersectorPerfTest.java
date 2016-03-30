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
package com.revolsys.geometry.test.old.perf.algorithm;

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
import com.revolsys.geometry.util.Stopwatch;

public class RectangleLineIntersectorPerfTest {
  public static void main(final String[] args) {
    final RectangleLineIntersectorPerfTest test = new RectangleLineIntersectorPerfTest();
    test.runBoth(5);
    test.runBoth(30);
    test.runBoth(30);
    test.runBoth(100);
    test.runBoth(300);
    test.runBoth(600);
    test.runBoth(1000);
    test.runBoth(6000);
  }

  private final double baseX = 0;

  private final double baseY = 0;

  private final GeometryFactory geomFact = GeometryFactory.DEFAULT;

  private Point[] pts;

  private BoundingBox rectEnv;

  private final double rectSize = 100;

  public RectangleLineIntersectorPerfTest() {

  }

  public void init(final int nPts) {
    this.rectEnv = newRectangle();
    this.pts = newTestPoints(nPts);
  }

  private BoundingBox newRectangle() {
    final BoundingBox rectEnv = new BoundingBoxDoubleGf(
      new PointDouble(this.baseX, this.baseY, Geometry.NULL_ORDINATE), new PointDouble(
        this.baseX + this.rectSize, this.baseY + this.rectSize, Geometry.NULL_ORDINATE));
    return rectEnv;
  }

  private Point[] newTestPoints(final int nPts) {
    final Point pt = this.geomFact
      .point(new PointDouble(this.baseX, this.baseY, Geometry.NULL_ORDINATE));
    final Geometry circle = pt.buffer(2 * this.rectSize, nPts / 4);
    return CoordinatesListUtil.getCoordinateArray(circle);
  }

  public void run(final boolean useSegInt, final boolean useSideInt) {
    if (useSegInt) {
      // System.out.println("Using Segment Intersector");
    }
    if (useSideInt) {
      // System.out.println("Using Side Intersector");
    }
    // System.out.println("# pts: " + this.pts.length);

    final RectangleLineIntersector rectSegIntersector = new RectangleLineIntersector(this.rectEnv);
    final SimpleRectangleIntersector rectSideIntersector = new SimpleRectangleIntersector(
      this.rectEnv);

    final Stopwatch sw = new Stopwatch();

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
            throw new IllegalStateException("Seg and Side values do not match");
          }
        }
      }
    }

    // System.out.println("Finished in " + sw.getTimeString());
    // System.out.println();
  }

  public void runBoth(final int nPts) {
    init(nPts);
    run(true, false);
    run(false, true);
  }

}

/**
 * Tests intersection of a segment against a rectangle
 * by computing intersection against all side segments.
 *
 * @author Martin Davis
 *
 */
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
    this.corner[0] = new PointDouble(rectEnv.getMaxX(), rectEnv.getMaxY(), Geometry.NULL_ORDINATE);
    this.corner[1] = new PointDouble(rectEnv.getMinX(), rectEnv.getMaxY(), Geometry.NULL_ORDINATE);
    this.corner[2] = new PointDouble(rectEnv.getMinX(), rectEnv.getMinY(), Geometry.NULL_ORDINATE);
    this.corner[3] = new PointDouble(rectEnv.getMaxX(), rectEnv.getMinY(), Geometry.NULL_ORDINATE);
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
