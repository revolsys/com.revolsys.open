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
 * version 2.1 of the License,or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not,write to the Free Software
 * Foundation,Inc.,59 Temple Place,Suite 330,Boston,MA  02111-1307  USA
 *
 * For more information,contact:
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
package com.revolsys.core.test.geometry.test.old.operation;

import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.operation.distance.DistanceWithPoints;
import com.revolsys.geometry.wkb.ParseException;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @version 1.7
 */
public class DistanceTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(DistanceTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.floating2d(0);

  public DistanceTest(final String name) {
    super(name);
  }

  private void doNearestPointsTest(final String wkt0, final String wkt1, final double distance,
    final Point p0, final Point p1) throws ParseException {
    final Geometry geometry1 = this.geometryFactory.geometry(wkt0);
    final Geometry geometry2 = this.geometryFactory.geometry(wkt1);
    final DistanceWithPoints op = new DistanceWithPoints(geometry1, geometry2);
    final double tolerance = 1E-10;
    final List<Point> nearestPoints = op.nearestPoints();
    final Point nearestPoint1 = nearestPoints.get(0);
    final Point nearestPoint2 = nearestPoints.get(1);
    final double p1p2Distance = nearestPoint1.distancePoint(nearestPoint2);
    assertEquals(distance, p1p2Distance, tolerance);
    assertEquals(p0.getX(), nearestPoint1.getX(), tolerance);
    assertEquals(p0.getY(), nearestPoint1.getY(), tolerance);
    assertEquals(p1.getX(), nearestPoint2.getX(), tolerance);
    assertEquals(p1.getY(), nearestPoint2.getY(), tolerance);
  }

  public void testClosestPoints1() throws Exception {
    final String wkt1 = "POLYGON ((200 180,60 140,60 260,200 180))";
    final String wkt2 = "POINT (140 280)";
    final Point p1 = this.geometryFactory.point(111.6923076923077, 230.46153846153845);
    final Point p2 = this.geometryFactory.point(140.0, 280);
    doNearestPointsTest(wkt1, wkt2, 57.05597791103589, p1, p2);
  }

  public void testClosestPoints2() throws Exception {
    doNearestPointsTest("POLYGON ((200 180,60 140,60 260,200 180))",
      "MULTIPOINT ((140 280),(140 320))", 57.05597791103589,
      this.geometryFactory.point(111.6923076923077, 230.46153846153845),
      new PointDoubleXY(140, 280));
  }

  public void testClosestPoints3() throws Exception {
    doNearestPointsTest("LINESTRING (100 100,200 100,200 200,100 200,100 100)", "POINT (10 10)",
      127.27922061357856, new PointDoubleXY(100, 100), new PointDoubleXY(10, 10));
  }

  public void testClosestPoints4() throws Exception {
    doNearestPointsTest("LINESTRING (100 100,200 200)", "LINESTRING (100 200,200 100)", 0.0,
      this.geometryFactory.point(150.0, 150.0), this.geometryFactory.point(150.0, 150.0));
  }

  public void testClosestPoints5() throws Exception {
    doNearestPointsTest("LINESTRING (100 100,200 200)", "LINESTRING (150 121,200 0)",
      20.506096654409877, this.geometryFactory.point(135.5, 135.5), new PointDoubleXY(150, 121));
  }

  public void testClosestPoints6() throws Exception {
    final Point p1 = this.geometryFactory.point(139.4956500724988, 206.78661188980183);
    final Point p2 = this.geometryFactory.point(153.0, 204);
    final String wkt1 = "POLYGON((76 185,125 283,331 276,324 122,177 70,184 155,69 123,76 185),(267 237,148 248,135 185,223 189,251 151,286 183,267 237))";
    final String wkt2 = "LINESTRING(153 204,185 224,209 207,238 222,254 186)";
    doNearestPointsTest(wkt1, wkt2, 13.788860460124573, p1, p2);
  }

  public void testClosestPoints7() throws Exception {
    doNearestPointsTest(
      "POLYGON ((76 185,125 283,331 276,324 122,177 70,184 155,69 123,76 185),(267 237,148 248,135 185,223 189,251 151,286 183,267 237))",
      "LINESTRING (120 215,185 224,209 207,238 222,254 186)", 0.0, new PointDoubleXY(120, 215),
      new PointDoubleXY(120, 215));
  }

  public void testDisjointCollinearSegments() throws Exception {
    final Geometry g1 = this.geometryFactory.lineString(2, 0.0, 0.0, 9.9, 1.4);
    final Geometry g2 = this.geometryFactory.lineString(2, 11.88, 1.68, 21.78, 3.08);
    final double distance = g1.distanceGeometry(g2);
    assertEquals(1.9996999774966246, distance, 0.0001);
  }

  public void testEmpty() throws Exception {
    final Geometry g1 = this.geometryFactory.point(0, 0);
    final Geometry g2 = this.geometryFactory.polygon();
    assertEquals(0.0, g1.distanceGeometry(g2), Double.POSITIVE_INFINITY);
  }

  public void testEverything() throws Exception {
    final Geometry g1 = this.geometryFactory.geometry(
      "POLYGON ((40 320,200 380,320 80,40 40,40 320), (180 280,80 280,100 100,220 140,180 280))");
    final Geometry g2 = this.geometryFactory
      .geometry("POLYGON ((160 240,120 240,120 160,160 140,160 240))");
    final double distanceG1G2 = g1.distanceGeometry(g2);
    assertEquals(18.97366596, distanceG1G2, 1E-5);

    final Geometry g3 = this.geometryFactory
      .geometry("POLYGON ((160 240,120 240,120 160,180 100,160 240))");
    final double distanceG1G3 = g1.distanceGeometry(g3);
    assertEquals(0.0, distanceG1G3, 1E-5);

    final LineString l1 = this.geometryFactory.lineString(2, 10.0, 10, 20, 20, 30, 40);
    final LineString l2 = this.geometryFactory.lineString(2, 10.0, 10, 20, 20, 30, 40);
    assertEquals(0.0, l1.distanceLine(l2), 1E-5);
  }
}
