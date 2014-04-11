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

import junit.framework.TestCase;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.NonRobustCGAlgorithms;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;

/**
 * Basic functionality tests for RobustLineIntersector.
 * 
 * @version 1.7
 */
public class RobustLineIntersectorTest extends TestCase {

  public static void main(final String[] args) {
    final String[] testCaseName = {
      RobustLineIntersectorTest.class.getName()
    };
    junit.textui.TestRunner.main(testCaseName);
  }// public static void main(String[] args)

  RobustLineIntersector i = new RobustLineIntersector();

  public RobustLineIntersectorTest(final String Name_) {
    super(Name_);
  }// public RobustLineIntersectorTest(String Name_)

  public void test2Lines() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Coordinate p1 = new Coordinate(10, 10, Coordinates.NULL_ORDINATE);
    final Coordinate p2 = new Coordinate(20, 20, Coordinates.NULL_ORDINATE);
    final Coordinate q1 = new Coordinate(20, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q2 = new Coordinate(10, 20, Coordinates.NULL_ORDINATE);
    final Coordinate x = new Coordinate(15, 15, Coordinates.NULL_ORDINATE);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.POINT_INTERSECTION, i.getIntersectionNum());
    assertEquals(1, i.getIntersectionNum());
    assertEquals(x, i.getIntersection(0));
    assertTrue(i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testA() {
    final Coordinate p1 = new Coordinate(-123456789, -40, Coordinates.NULL_ORDINATE);
    final Coordinate p2 = new Coordinate(381039468754763d, 123456789, Coordinates.NULL_ORDINATE);
    final Coordinate q = new Coordinate(0, 0, Coordinates.NULL_ORDINATE);
    final LineString l = GeometryFactory.getFactory().createLineString(new Coordinates[] {
      p1, p2
    });
    final Point p = GeometryFactory.getFactory().createPoint(q);
    assertEquals(false, l.intersects(p));
    assertEquals(false, CGAlgorithms.isOnLine(q, new Coordinate[] {
      p1, p2
    }));
    assertEquals(-1, CGAlgorithms.computeOrientation(p1, p2, q));
  }

  public void testCollinear1() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Coordinate p1 = new Coordinate(10, 10, Coordinates.NULL_ORDINATE);
    final Coordinate p2 = new Coordinate(20, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q1 = new Coordinate(22, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q2 = new Coordinate(30, 10, Coordinates.NULL_ORDINATE);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.NO_INTERSECTION, i.getIntersectionNum());
    assertTrue(!i.isProper());
    assertTrue(!i.hasIntersection());
  }

  public void testCollinear2() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Coordinate p1 = new Coordinate(10, 10, Coordinates.NULL_ORDINATE);
    final Coordinate p2 = new Coordinate(20, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q1 = new Coordinate(20, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q2 = new Coordinate(30, 10, Coordinates.NULL_ORDINATE);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.POINT_INTERSECTION, i.getIntersectionNum());
    assertTrue(!i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testCollinear3() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Coordinate p1 = new Coordinate(10, 10, Coordinates.NULL_ORDINATE);
    final Coordinate p2 = new Coordinate(20, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q1 = new Coordinate(15, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q2 = new Coordinate(30, 10, Coordinates.NULL_ORDINATE);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.COLLINEAR_INTERSECTION, i.getIntersectionNum());
    assertTrue(!i.isProper());
    assertTrue(i.hasIntersection());
  }

  public void testCollinear4() {
    final RobustLineIntersector i = new RobustLineIntersector();
    final Coordinate p1 = new Coordinate(30, 10, Coordinates.NULL_ORDINATE);
    final Coordinate p2 = new Coordinate(20, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q1 = new Coordinate(10, 10, Coordinates.NULL_ORDINATE);
    final Coordinate q2 = new Coordinate(30, 10, Coordinates.NULL_ORDINATE);
    i.computeIntersection(p1, p2, q1, q2);
    assertEquals(LineIntersector.COLLINEAR_INTERSECTION, i.getIntersectionNum());
    assertTrue(i.hasIntersection());
  }

  public void testEndpointIntersection() {
    this.i.computeIntersection(new Coordinate(100, 100, Coordinates.NULL_ORDINATE),
      new Coordinate(10, 100, Coordinates.NULL_ORDINATE), new Coordinate(100, 10, Coordinates.NULL_ORDINATE),
      new Coordinate(100, 100, Coordinates.NULL_ORDINATE));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionNum());
  }

  public void testEndpointIntersection2() {
    this.i.computeIntersection(new Coordinate(190, 50, Coordinates.NULL_ORDINATE),
      new Coordinate(120, 100, Coordinates.NULL_ORDINATE), new Coordinate(120, 100, Coordinates.NULL_ORDINATE), new Coordinate(50,
        150, Coordinates.NULL_ORDINATE));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionNum());
    assertEquals(new Coordinate(120, 100, Coordinates.NULL_ORDINATE), this.i.getIntersection(1));
  }

  public void testIsCCW() {
    assertEquals(1, CGAlgorithms.computeOrientation(new Coordinate(-123456789,
      -40, Coordinates.NULL_ORDINATE), new Coordinate(0, 0, Coordinates.NULL_ORDINATE), new Coordinate(381039468754763d, 123456789, Coordinates.NULL_ORDINATE)));
  }

  public void testIsCCW2() {
    assertEquals(0, CGAlgorithms.computeOrientation(new Coordinate(10, 10, Coordinates.NULL_ORDINATE),
      new Coordinate(20, 20, Coordinates.NULL_ORDINATE), new Coordinate(0, 0, Coordinates.NULL_ORDINATE)));
    assertEquals(0, NonRobustCGAlgorithms.computeOrientation(new Coordinate(10,
      10, Coordinates.NULL_ORDINATE), new Coordinate(20, 20, Coordinates.NULL_ORDINATE), new Coordinate(0, 0, Coordinates.NULL_ORDINATE)));
  }

  public void testIsProper1() {
    this.i.computeIntersection(new Coordinate(30, 10, Coordinates.NULL_ORDINATE), new Coordinate(30, 30, Coordinates.NULL_ORDINATE),
      new Coordinate(10, 10, Coordinates.NULL_ORDINATE), new Coordinate(90, 11, Coordinates.NULL_ORDINATE));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionNum());
    assertTrue(this.i.isProper());
  }

  public void testIsProper2() {
    this.i.computeIntersection(new Coordinate(10, 30, Coordinates.NULL_ORDINATE), new Coordinate(10, 0, Coordinates.NULL_ORDINATE),
      new Coordinate(11, 90, Coordinates.NULL_ORDINATE), new Coordinate(10, 10, Coordinates.NULL_ORDINATE));
    assertTrue(this.i.hasIntersection());
    assertEquals(1, this.i.getIntersectionNum());
    assertTrue(!this.i.isProper());
  }

  public void testOverlap() {
    this.i.computeIntersection(new Coordinate(180, 200, Coordinates.NULL_ORDINATE), new Coordinate(160,
      180, Coordinates.NULL_ORDINATE), new Coordinate(220, 240, Coordinates.NULL_ORDINATE), new Coordinate(140, 160, Coordinates.NULL_ORDINATE));
    assertTrue(this.i.hasIntersection());
    assertEquals(2, this.i.getIntersectionNum());
  }

}
