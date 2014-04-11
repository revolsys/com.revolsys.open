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

package com.revolsys.jts.testold;

import java.util.Date;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.NonRobustLineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

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
    System.out.println(lineIntersector.getClass().getName());
    final Date start = new Date();
    for (int i = 0; i < 1000000; i++) {
      exerciseOnce(lineIntersector);
    }
    final Date end = new Date();
    System.out.println("Milliseconds elapsed: "
      + (end.getTime() - start.getTime()));
    System.out.println();
  }

  private void exerciseOnce(final LineIntersector lineIntersector) {
    Coordinates p1 = new Coordinate((double)10.0, 10, Coordinates.NULL_ORDINATE);
    Coordinates p2 = new Coordinate((double)20.0, 20, Coordinates.NULL_ORDINATE);
    Coordinates q1 = new Coordinate((double)20.0, 10, Coordinates.NULL_ORDINATE);
    Coordinates q2 = new Coordinate((double)10.0, 20, Coordinates.NULL_ORDINATE);
    final Coordinates x = new Coordinate((double)15.0, 15, Coordinates.NULL_ORDINATE);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.getIntersectionNum();
    lineIntersector.getIntersection(0);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate((double)10.0, 10, Coordinates.NULL_ORDINATE);
    p2 = new Coordinate((double)20.0, 10, Coordinates.NULL_ORDINATE);
    q1 = new Coordinate((double)22.0, 10, Coordinates.NULL_ORDINATE);
    q2 = new Coordinate((double)30.0, 10, Coordinates.NULL_ORDINATE);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate((double)10.0, 10, Coordinates.NULL_ORDINATE);
    p2 = new Coordinate((double)20.0, 10, Coordinates.NULL_ORDINATE);
    q1 = new Coordinate((double)20.0, 10, Coordinates.NULL_ORDINATE);
    q2 = new Coordinate((double)30.0, 10, Coordinates.NULL_ORDINATE);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate((double)10.0, 10, Coordinates.NULL_ORDINATE);
    p2 = new Coordinate((double)20.0, 10, Coordinates.NULL_ORDINATE);
    q1 = new Coordinate((double)15.0, 10, Coordinates.NULL_ORDINATE);
    q2 = new Coordinate((double)30.0, 10, Coordinates.NULL_ORDINATE);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.isProper();
    lineIntersector.hasIntersection();

    p1 = new Coordinate((double)30.0, 10, Coordinates.NULL_ORDINATE);
    p2 = new Coordinate((double)20.0, 10, Coordinates.NULL_ORDINATE);
    q1 = new Coordinate((double)10.0, 10, Coordinates.NULL_ORDINATE);
    q2 = new Coordinate((double)30.0, 10, Coordinates.NULL_ORDINATE);
    lineIntersector.computeIntersection(p1, p2, q1, q2);
    lineIntersector.hasIntersection();

    lineIntersector.computeIntersection(new Coordinate((double)100.0, 100,
      Coordinates.NULL_ORDINATE), new Coordinate((double)10.0, 100,
      Coordinates.NULL_ORDINATE), new Coordinate((double)100.0, 10,
      Coordinates.NULL_ORDINATE), new Coordinate((double)100.0, 100,
      Coordinates.NULL_ORDINATE));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();

    lineIntersector.computeIntersection(new Coordinate((double)190.0, 50,
      Coordinates.NULL_ORDINATE), new Coordinate((double)120.0, 100,
      Coordinates.NULL_ORDINATE), new Coordinate((double)120.0, 100,
      Coordinates.NULL_ORDINATE), new Coordinate((double)50.0, 150,
      Coordinates.NULL_ORDINATE));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();
    lineIntersector.getIntersection(1);

    lineIntersector.computeIntersection(new Coordinate((double)180.0, 200,
      Coordinates.NULL_ORDINATE), new Coordinate((double)160.0, 180,
      Coordinates.NULL_ORDINATE), new Coordinate((double)220.0, 240,
      Coordinates.NULL_ORDINATE), new Coordinate((double)140.0, 160,
      Coordinates.NULL_ORDINATE));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();

    lineIntersector.computeIntersection(new Coordinate((double)30.0, 10,
      Coordinates.NULL_ORDINATE), new Coordinate((double)30.0, 30,
      Coordinates.NULL_ORDINATE), new Coordinate((double)10.0, 10,
      Coordinates.NULL_ORDINATE), new Coordinate((double)90.0, 11,
      Coordinates.NULL_ORDINATE));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();
    lineIntersector.isProper();

    lineIntersector.computeIntersection(new Coordinate((double)10.0, 30,
      Coordinates.NULL_ORDINATE), new Coordinate((double)10.0, 0,
      Coordinates.NULL_ORDINATE), new Coordinate((double)11.0, 90,
      Coordinates.NULL_ORDINATE), new Coordinate((double)10.0, 10,
      Coordinates.NULL_ORDINATE));
    lineIntersector.hasIntersection();
    lineIntersector.getIntersectionNum();
    lineIntersector.isProper();
  }

  @Override
  public void run() {
    exercise(new NonRobustLineIntersector());
    exercise(new RobustLineIntersector());
  }
}
