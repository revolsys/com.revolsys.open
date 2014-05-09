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

import java.util.Stack;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.revolsys.jts.algorithm.ConvexHull;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.io.WKTReader;

/**
 * Test for {@link ConvexHull}.
 *
 * @version 1.7
 */
public class ConvexHullTest extends TestCase {

  private static class ConvexHullEx extends ConvexHull {
    public ConvexHullEx(final Geometry geometry) {
      super(geometry);
    }

    @Override
    protected Coordinates[] toCoordinateArray(final Stack stack) {
      return super.toCoordinateArray(stack);
    }
  }

  public static void main(final String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(ConvexHullTest.class);
  }

  GeometryFactory geometryFactory = GeometryFactory.getFactory(0, 1000.0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public ConvexHullTest(final String name) {
    super(name);
  }

  public void test1() throws Exception {
    final WKTReader reader = new WKTReader(GeometryFactory.getFactory(0, 1.0));
    final LineString lineString = (LineString)reader.read("LINESTRING (30 220, 240 220, 240 220)");
    final LineString convexHull = (LineString)reader.read("LINESTRING (30 220, 240 220)");
    assertTrue(convexHull.equalsExact2d(lineString.convexHull()));
  }

  public void test2() throws Exception {
    final WKTReader reader = new WKTReader(GeometryFactory.getFactory(0, 1.0));
    final Geometry geometry = reader.read("MULTIPOINT (130 240, 130 240, 130 240, 570 240, 570 240, 570 240, 650 240)");
    final LineString convexHull = (LineString)reader.read("LINESTRING (130 240, 650 240)");
    assertTrue(convexHull.equalsExact2d(geometry.convexHull()));
  }

  public void test3() throws Exception {
    final WKTReader reader = new WKTReader(GeometryFactory.getFactory(0, 1.0));
    final Geometry geometry = reader.read("MULTIPOINT (0 0, 0 0, 10 0)");
    final LineString convexHull = (LineString)reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact2d(geometry.convexHull()));
  }

  public void test4() throws Exception {
    final WKTReader reader = new WKTReader(GeometryFactory.getFactory(0, 1.0));
    final Geometry geometry = reader.read("MULTIPOINT (0 0, 10 0, 10 0)");
    final LineString convexHull = (LineString)reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact2d(geometry.convexHull()));
  }

  public void test5() throws Exception {
    final WKTReader reader = new WKTReader(GeometryFactory.getFactory(0, 1.0));
    final Geometry geometry = reader.read("MULTIPOINT (0 0, 5 0, 10 0)");
    final LineString convexHull = (LineString)reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact2d(geometry.convexHull()));
  }

  public void test6() throws Exception {
    final WKTReader reader = new WKTReader(GeometryFactory.getFactory(0, 1.0));
    final Geometry actualGeometry = reader.read("MULTIPOINT (0 0, 5 1, 10 0)")
      .convexHull();
    final Geometry expectedGeometry = reader.read("POLYGON ((0 0, 5 1, 10 0, 0 0))");
    assertEquals(expectedGeometry.toString(), actualGeometry.toString());
  }

  public void test7() throws Exception {
    final WKTReader reader = new WKTReader(GeometryFactory.getFactory(0, 1.0));
    final Geometry geometry = reader.read("MULTIPOINT (0 0, 0 0, 5 0, 5 0, 10 0, 10 0)");
    final LineString convexHull = (LineString)reader.read("LINESTRING (0 0, 10 0)");
    assertTrue(convexHull.equalsExact2d(geometry.convexHull()));
  }

  public void testAllIdenticalPoints() throws Exception {
    final Coordinates[] pts = new Coordinates[100];
    for (int i = 0; i < 100; i++) {
      pts[i] = new Coordinate(0.0, 0, Coordinates.NULL_ORDINATE);
    }
    final ConvexHull ch = new ConvexHull(pts, this.geometryFactory);
    final Geometry actualGeometry = ch.getConvexHull();
    final Geometry expectedGeometry = this.reader.read("POINT (0 0)");
    assertTrue(expectedGeometry.equalsExact2d(actualGeometry));
  }

  public void testManyIdenticalPoints() throws Exception {
    final Coordinates[] pts = new Coordinates[100];
    for (int i = 0; i < 99; i++) {
      pts[i] = new Coordinate(0.0, 0, Coordinates.NULL_ORDINATE);
    }
    pts[99] = new Coordinate(1.0, 1, Coordinates.NULL_ORDINATE);
    final ConvexHull ch = new ConvexHull(pts, this.geometryFactory);
    final Geometry actualGeometry = ch.getConvexHull();
    final Geometry expectedGeometry = this.reader.read("LINESTRING (0 0, 1 1)");
    assertTrue(expectedGeometry.equalsExact2d(actualGeometry));
  }

  public void testToArray() throws Exception {
    final ConvexHullEx convexHull = new ConvexHullEx(
      this.geometryFactory.geometryCollection());
    final Stack stack = new Stack();
    stack.push(new Coordinate(0.0, 0, Coordinates.NULL_ORDINATE));
    stack.push(new Coordinate(1.0, 1, Coordinates.NULL_ORDINATE));
    stack.push(new Coordinate(2.0, 2, Coordinates.NULL_ORDINATE));
    final Object[] array1 = convexHull.toCoordinateArray(stack);
    assertEquals(3, array1.length);
    assertEquals(new Coordinate(0.0, 0, Coordinates.NULL_ORDINATE), array1[0]);
    assertEquals(new Coordinate(1.0, 1, Coordinates.NULL_ORDINATE), array1[1]);
    assertEquals(new Coordinate(2.0, 2, Coordinates.NULL_ORDINATE), array1[2]);
    assertTrue(!array1[0].equals(array1[1]));
  }

}
