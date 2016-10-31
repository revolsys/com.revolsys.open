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

package com.revolsys.geometry.test.old;

import java.util.Date;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.NonRobustLineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

/**
 * @version 1.7
 */
public class LineIntersectorBenchmark implements Runnable {

  public static void main(final String[] args) {
    final LineIntersectorBenchmark lineIntersectorBenchmark = new LineIntersectorBenchmark();
    lineIntersectorBenchmark.run();
  }

  public LineIntersectorBenchmark() {
  }

  private void exercise(final LineIntersector lineIntersector) {
    // System.out.println(lineIntersector.getClass().getName());
    final Date start = new Date();
    for (int i = 0; i < 1000000; i++) {
      exerciseOnce(lineIntersector);
    }
    final Date end = new Date();
    // System.out.println("Milliseconds elapsed: "
    // + (end.getTime() - start.getTime()));
    // System.out.println();
  }

  private void exerciseOnce(final LineIntersector lineIntersector) {
    Point p1 = new PointDoubleXY(10.0, 10);
    Point p2 = new PointDoubleXY(20.0, 20);
    Point q1 = new PointDoubleXY(20.0, 10);
    Point q2 = new PointDoubleXY(10.0, 20);
    final Point x = new PointDoubleXY(15.0, 15);
    lineIntersector.computeIntersectionPoints(p1, p2, q1, q2);
    lineIntersector.getIntersectionCount();
    lineIntersector.getIntersection(0);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDoubleXY(10.0, 10);
    p2 = new PointDoubleXY(20.0, 10);
    q1 = new PointDoubleXY(22.0, 10);
    q2 = new PointDoubleXY(30.0, 10);
    lineIntersector.computeIntersectionPoints(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDoubleXY(10.0, 10);
    p2 = new PointDoubleXY(20.0, 10);
    q1 = new PointDoubleXY(20.0, 10);
    q2 = new PointDoubleXY(30.0, 10);
    lineIntersector.computeIntersectionPoints(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDoubleXY(10.0, 10);
    p2 = new PointDoubleXY(20.0, 10);
    q1 = new PointDoubleXY(15.0, 10);
    q2 = new PointDoubleXY(30.0, 10);
    lineIntersector.computeIntersectionPoints(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDoubleXY(30.0, 10);
    p2 = new PointDoubleXY(20.0, 10);
    q1 = new PointDoubleXY(10.0, 10);
    q2 = new PointDoubleXY(30.0, 10);
    lineIntersector.computeIntersectionPoints(p1, p2, q1, q2);
    lineIntersector.hasIntersection();

    lineIntersector.computeIntersectionPoints(new PointDoubleXY(100.0, 100),
      new PointDoubleXY(10.0, 100),
      new PointDoubleXY(100.0, 10),
      new PointDoubleXY(100.0, 100));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();

    lineIntersector.computeIntersectionPoints(new PointDoubleXY(190.0, 50),
      new PointDoubleXY(120.0, 100),
      new PointDoubleXY(120.0, 100),
      new PointDoubleXY(50.0, 150));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();
    lineIntersector.getIntersection(1);

    lineIntersector.computeIntersectionPoints(new PointDoubleXY(180.0, 200),
      new PointDoubleXY(160.0, 180),
      new PointDoubleXY(220.0, 240),
      new PointDoubleXY(140.0, 160));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();

    lineIntersector.computeIntersectionPoints(new PointDoubleXY(30.0, 10),
      new PointDoubleXY(30.0, 30),
      new PointDoubleXY(10.0, 10),
      new PointDoubleXY(90.0, 11));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();
    lineIntersector.isProper();

    lineIntersector.computeIntersectionPoints(new PointDoubleXY(10.0, 30),
      new PointDoubleXY(10.0, 0),
      new PointDoubleXY(11.0, 90),
      new PointDoubleXY(10.0, 10));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();
    lineIntersector.isProper();
  }

  @Override
  public void run() {
    exercise(new NonRobustLineIntersector());
    exercise(new RobustLineIntersector());
  }
}
