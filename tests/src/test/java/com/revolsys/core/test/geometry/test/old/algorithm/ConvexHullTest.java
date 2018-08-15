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

package com.revolsys.core.test.geometry.test.old.algorithm;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.geometry.algorithm.ConvexHull;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.PointDoubleXY;

import junit.framework.TestCase;

/**
 * Test for {@link ConvexHull}.
 *
 * @version 1.7
 */
public class ConvexHullTest extends TestCase {

  private static final GeometryFactory GEOMETRY_FACTORY_1M = GeometryFactory.fixed2d(0, 1.0, 1.0);

  GeometryFactory geometryFactory = GeometryFactory.fixed2d(0, 1000.0, 1000.0);

  public ConvexHullTest(final String name) {
    super(name);
  }

  public void test1() throws Exception {
    final LineString lineString = (LineString)GEOMETRY_FACTORY_1M
      .geometry("LINESTRING(30 220,240 220,240 220)");
    final LineString convexHull = (LineString)GEOMETRY_FACTORY_1M
      .geometry("LINESTRING(30 220,240 220)");
    assertTrue(convexHull.equals(2, lineString.convexHull()));
  }

  public void test2() throws Exception {
    final Geometry geometry = GEOMETRY_FACTORY_1M.punctual(2, 130.0, 240.0, 130.0, 240.0, 130.0,
      240.0, 570.0, 240.0, 570.0, 240.0, 570.0, 240.0, 650.0, 240.0);
    final LineString expected = GEOMETRY_FACTORY_1M.lineString(2, 130.0, 240.0, 650.0, 240.0);
    final Geometry actual = geometry.convexHull();
    assertEquals(expected, actual);
  }

  public void test3() throws Exception {
    final Geometry geometry = GEOMETRY_FACTORY_1M.punctual(2, 0, 0, 0, 0, 10, 0);
    final LineString convexHull = (LineString)GEOMETRY_FACTORY_1M.geometry("LINESTRING(0 0,10 0)");
    assertTrue(convexHull.equals(2, geometry.convexHull()));
  }

  public void test4() throws Exception {
    final Geometry geometry = GEOMETRY_FACTORY_1M.punctual(2, 0, 0, 10, 0, 10, 0);
    final LineString convexHull = (LineString)GEOMETRY_FACTORY_1M.geometry("LINESTRING(0 0,10 0)");
    assertTrue(convexHull.equals(2, geometry.convexHull()));
  }

  public void test5() throws Exception {
    final Geometry geometry = GEOMETRY_FACTORY_1M.punctual(2, 0, 0, 5, 0, 10, 0);
    final LineString convexHull = (LineString)GEOMETRY_FACTORY_1M.geometry("LINESTRING(0 0,10 0)");
    assertTrue(convexHull.equals(2, geometry.convexHull()));
  }

  public void test6() throws Exception {
    final Punctual inputGeometry = GEOMETRY_FACTORY_1M.punctual(2, 0, 0, 5, 1, 10, 0);
    final Geometry actualGeometry = inputGeometry.convexHull();
    final Geometry expectedGeometry = GEOMETRY_FACTORY_1M.geometry("POLYGON((0 0,5 1,10 0,0 0))");
    assertEquals(expectedGeometry.toString(), actualGeometry.toString());
  }

  public void test7() throws Exception {
    final Geometry geometry = GEOMETRY_FACTORY_1M.punctual(2, 0, 0, 0, 0, 5, 0, 5, 0, 10, 0, 10, 0);
    final LineString convexHull = (LineString)GEOMETRY_FACTORY_1M.geometry("LINESTRING(0 0,10 0)");
    assertTrue(convexHull.equals(2, geometry.convexHull()));
  }

  public void testAllIdenticalPoints() throws Exception {
    final List<Point> points = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      points.add(new PointDoubleXY(0.0, 0));
    }
    final Geometry actualGeometry = ConvexHull.convexHull(this.geometryFactory, points);
    final Geometry expectedGeometry = this.geometryFactory.geometry("POINT(0 0)");
    assertTrue(expectedGeometry.equals(2, actualGeometry));
  }

  public void testManyIdenticalPoints() throws Exception {
    final List<Point> points = new ArrayList<>();
    for (int i = 0; i < 99; i++) {
      points.add(new PointDoubleXY(0.0, 0));
    }
    points.add(new PointDoubleXY(1.0, 1));
    final Geometry actualGeometry = ConvexHull.convexHull(this.geometryFactory, points);
    final Geometry expectedGeometry = this.geometryFactory.geometry("LINESTRING(0 0,1 1)");
    assertTrue(expectedGeometry.equals(2, actualGeometry));
  }

}
