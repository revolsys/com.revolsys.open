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

import java.util.Random;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

public class DistanceLineLineStressTest extends TestCase {

  // make results reproducible
  static Random randGen = new Random(123456);

  public static void main(final String args[]) {
    TestRunner.run(DistanceLineLineStressTest.class);
  }

  private static Point[] randomDisjointCollinearSegments() {
    final double slope = randGen.nextDouble();
    final Point[] seg = new Point[4];

    final double gap = 1;
    final double x1 = 10;
    final double x2 = x1 + gap;
    final double x3 = x1 + gap + 10;
    seg[0] = new PointDouble((double)0, 0, Point.NULL_ORDINATE);
    seg[1] = new PointDouble((double)x1, slope * x1, Point.NULL_ORDINATE);
    seg[2] = new PointDouble((double)x2, slope * x2, Point.NULL_ORDINATE);
    seg[3] = new PointDouble((double)x3, slope * x3, Point.NULL_ORDINATE);

    return seg;
  }

  public DistanceLineLineStressTest(final String name) {
    super(name);
  }

  public void testRandomDisjointCollinearSegments() throws Exception {
    final int n = 1000000;
    int failCount = 0;
    for (int i = 0; i < n; i++) {
      // System.out.println(i);
      final Point[] seg = randomDisjointCollinearSegments();
      if (0 == LineSegmentUtil.distanceLineLine(seg[0], seg[1], seg[2], seg[3])) {
        /*
         * System.out.println("FAILED! - " + WKTWriter.toLineString(seg[0],
         * seg[1]) + "  -  " + WKTWriter.toLineString(seg[2], seg[3]));
         */
        failCount++;
      }
    }
  //  System.out.println("# failed = " + failCount + " out of " + n);
  }

}
