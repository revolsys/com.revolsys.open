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
package com.revolsys.jts.testold.perf.algorithm;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RectangleLineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.util.Stopwatch;

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

  private final GeometryFactory geomFact = GeometryFactory.getFactory();

  private final double baseX = 0;

  private final double baseY = 0;

  private final double rectSize = 100;

  private BoundingBox rectEnv;

  private Coordinates[] pts;

  public RectangleLineIntersectorPerfTest() {

  }

  private BoundingBox createRectangle() {
    final BoundingBox rectEnv = new Envelope(new Coordinate(this.baseX,
      this.baseY, Coordinates.NULL_ORDINATE), new Coordinate(this.baseX
      + this.rectSize, this.baseY + this.rectSize, Coordinates.NULL_ORDINATE));
    return rectEnv;
  }

  private Coordinates[] createTestPoints(final int nPts) {
    final Point pt = this.geomFact.point(new Coordinate(this.baseX,
      this.baseY, Coordinates.NULL_ORDINATE));
    final Geometry circle = pt.buffer(2 * this.rectSize, nPts / 4);
    return circle.getCoordinateArray();
  }

  public void init(final int nPts) {
    this.rectEnv = createRectangle();
    this.pts = createTestPoints(nPts);
  }

  public void run(final boolean useSegInt, final boolean useSideInt) {
    if (useSegInt) {
    //  System.out.println("Using Segment Intersector");
    }
    if (useSideInt) {
    //  System.out.println("Using Side Intersector");
    }
  //  System.out.println("# pts: " + this.pts.length);

    final RectangleLineIntersector rectSegIntersector = new RectangleLineIntersector(
      this.rectEnv);
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

  //  System.out.println("Finished in " + sw.getTimeString());
  //  System.out.println();
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
  // for intersection testing, don't need to set precision model
  private final LineIntersector li = new RobustLineIntersector();

  private final BoundingBox rectEnv;

  /**
   * The corners of the rectangle, in the order:
   *  10
   *  23
   */
  private final Coordinates[] corner = new Coordinates[4];

  public SimpleRectangleIntersector(final BoundingBox rectEnv) {
    this.rectEnv = rectEnv;
    initCorners(rectEnv);
  }

  private void initCorners(final BoundingBox rectEnv) {
    this.corner[0] = new Coordinate(rectEnv.getMaxX(), rectEnv.getMaxY(),
      Coordinates.NULL_ORDINATE);
    this.corner[1] = new Coordinate(rectEnv.getMinX(), rectEnv.getMaxY(),
      Coordinates.NULL_ORDINATE);
    this.corner[2] = new Coordinate(rectEnv.getMinX(), rectEnv.getMinY(),
      Coordinates.NULL_ORDINATE);
    this.corner[3] = new Coordinate(rectEnv.getMaxX(), rectEnv.getMinY(),
      Coordinates.NULL_ORDINATE);
  }

  public boolean intersects(final Coordinates p0, final Coordinates p1) {
    final Envelope segEnv = new Envelope(p0, p1);
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
