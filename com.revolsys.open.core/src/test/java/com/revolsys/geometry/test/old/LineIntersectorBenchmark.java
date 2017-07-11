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
import com.revolsys.geometry.model.impl.PointDouble;

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
    Point p1 = new PointDouble(10.0, 10);
    Point p2 = new PointDouble(20.0, 20);
    Point q1 = new PointDouble(20.0, 10);
    Point q2 = new PointDouble(10.0, 20);
    final Point x = new PointDouble(15.0, 15);
    final Point p11 = p1;
    final Point p21 = p2;
    final Point p3 = q1;
    final Point p4 = q2;
    lineIntersector.computeIntersectionPoints(p11, p21, p3, p4);
    lineIntersector.getIntersectionCount();
    lineIntersector.getIntersection(0);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDouble(10.0, 10);
    p2 = new PointDouble(20.0, 10);
    q1 = new PointDouble(22.0, 10);
    q2 = new PointDouble(30.0, 10);
    final Point p12 = p1;
    final Point p22 = p2;
    final Point p31 = q1;
    final Point p41 = q2;
    lineIntersector.computeIntersectionPoints(p12, p22, p31, p41);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDouble(10.0, 10);
    p2 = new PointDouble(20.0, 10);
    q1 = new PointDouble(20.0, 10);
    q2 = new PointDouble(30.0, 10);
    final Point p13 = p1;
    final Point p23 = p2;
    final Point p32 = q1;
    final Point p42 = q2;
    lineIntersector.computeIntersectionPoints(p13, p23, p32, p42);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDouble(10.0, 10);
    p2 = new PointDouble(20.0, 10);
    q1 = new PointDouble(15.0, 10);
    q2 = new PointDouble(30.0, 10);
    final Point p14 = p1;
    final Point p24 = p2;
    final Point p33 = q1;
    final Point p43 = q2;
    lineIntersector.computeIntersectionPoints(p14, p24, p33, p43);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new PointDouble(30.0, 10);
    p2 = new PointDouble(20.0, 10);
    q1 = new PointDouble(10.0, 10);
    q2 = new PointDouble(30.0, 10);
    final Point p15 = p1;
    final Point p25 = p2;
    final Point p34 = q1;
    final Point p44 = q2;
    lineIntersector.computeIntersectionPoints(p15, p25, p34, p44);
    lineIntersector.hasIntersection();

    lineIntersector.computeIntersectionPoints(new PointDouble(100.0, 100), new PointDouble(10.0, 100), new PointDouble(100.0, 10), new PointDouble(100.0, 100));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();

    lineIntersector.computeIntersectionPoints(new PointDouble(190.0, 50), new PointDouble(120.0, 100), new PointDouble(120.0, 100), new PointDouble(50.0, 150));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();
    lineIntersector.getIntersection(1);

    lineIntersector.computeIntersectionPoints(new PointDouble(180.0, 200), new PointDouble(160.0, 180), new PointDouble(220.0, 240), new PointDouble(140.0, 160));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();

    lineIntersector.computeIntersectionPoints(new PointDouble(30.0, 10), new PointDouble(30.0, 30), new PointDouble(10.0, 10), new PointDouble(90.0, 11));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionCount();
    lineIntersector.isProper();

    lineIntersector.computeIntersectionPoints(new PointDouble(10.0, 30), new PointDouble(10.0, 0), new PointDouble(11.0, 90), new PointDouble(10.0, 10));
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
