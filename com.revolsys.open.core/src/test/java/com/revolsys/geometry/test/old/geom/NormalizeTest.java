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

package com.revolsys.geometry.test.old.geom;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.MultiLineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.test.model.CoordinateTest;

import junit.framework.TestCase;

/**
 * @version 1.7
 */
public class NormalizeTest extends TestCase {

  private final GeometryFactory geometryFactory = GeometryFactory.fixed(0, 1.0);

  public NormalizeTest(final String name) {
    super(name);
  }

  private void assertEqualsExact(final Geometry expectedValue, final Geometry actualValue) {
    assertTrue("Expected " + expectedValue + " but encountered " + actualValue,
      actualValue.equals(2, expectedValue));
  }

  public void testCompareEmptyPoint() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(30 30)");
    final Point p2 = this.geometryFactory.geometry("POINT EMPTY");
    assertTrue(p1.compareTo(p2) > 0);
  }

  public void testComparePoint() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(30 30)");
    final Point p2 = this.geometryFactory.geometry("POINT(30 40)");
    assertTrue(p1.compareTo(p2) < 0);
  }

  public void testNormalizeEmptyLineString() throws Exception {
    LineString l = (LineString)this.geometryFactory.geometry("LINESTRING EMPTY");
    l = l.normalize();
    final LineString expectedValue = (LineString)this.geometryFactory.geometry("LINESTRING EMPTY");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeEmptyPoint() throws Exception {
    Point point = this.geometryFactory.geometry("POINT EMPTY");
    point = point.normalize();
    assertEquals(null, point.getPoint());
  }

  public void testNormalizeEmptyPolygon() throws Exception {
    Polygon actualValue = (Polygon)this.geometryFactory.geometry("POLYGON EMPTY");
    actualValue = actualValue.normalize();
    final Polygon expectedValue = (Polygon)this.geometryFactory.geometry("POLYGON EMPTY");
    assertEqualsExact(expectedValue, actualValue);
  }

  // public void testNormalizeGeometryCollection() throws Exception {
  // GeometryCollection actualValue =
  // (GeometryCollection)geometryFactory.geometry("GEOMETRYCOLLECTION
  // (LINESTRING (200 300,200 280,220 280,220 320,180 320),POINT (140
  // 220),POLYGON ((100 80,100 160,20 160,20 80,100 80),(40 140,40 100,80 100,80
  // 140,40 140)),POINT (100 240))");
  // actualValue = actualValue.normalize();
  // final GeometryCollection expectedValue =
  // (GeometryCollection)geometryFactory.geometry("GEOMETRYCOLLECTION (POINT
  // (100 240),POINT (140 220),LINESTRING (180 320,220 320,220 280,200 280,200
  // 300),POLYGON ((20 80,20 160,100 160,100 80,20 80),(40 100,80 100,80 140,40
  // 140,40 100)))");
  // assertEqualsExact(expectedValue,actualValue);
  // }

  public void testNormalizeLineString1() throws Exception {
    LineString l = (LineString)this.geometryFactory
      .geometry("LINESTRING(20 20,160 40,160 100,100 120,60 60)");
    l = l.normalize();
    final LineString expectedValue = (LineString)this.geometryFactory
      .geometry("LINESTRING(20 20,160 40,160 100,100 120,60 60)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString2() throws Exception {
    LineString l = (LineString)this.geometryFactory
      .geometry("LINESTRING(20 20,160 40,160 100,100 120,60 60)");
    l = l.normalize();
    final LineString expectedValue = (LineString)this.geometryFactory
      .geometry("LINESTRING (20 20,160 40,160 100,100 120,60 60)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString3() throws Exception {
    LineString l = (LineString)this.geometryFactory
      .geometry("LINESTRING(200 240,140 160,80 160,160 80,80 80)");
    l = l.normalize();
    final LineString expectedValue = (LineString)this.geometryFactory
      .geometry("LINESTRING(80 80,160 80,80 160,140 160,200 240)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString4() throws Exception {
    LineString l = (LineString)this.geometryFactory
      .geometry("LINESTRING(200 240,140 160,80 160,160 80,80 80)");
    l = l.normalize();
    final LineString expectedValue = (LineString)this.geometryFactory
      .geometry("LINESTRING(80 80,160 80,80 160,140 160,200 240)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString5() throws Exception {
    final LineString geometry = (LineString)this.geometryFactory
      .geometry("LINESTRING(200 340,140 240,140 160,60 240,140 240,200 340)");
    final LineString normalized = geometry.normalize();
    final LineString expectedValue = (LineString)this.geometryFactory
      .geometry("LINESTRING (200 340,140 240,60 240,140 160,140 240,200 340)");
    assertEqualsExact(expectedValue, normalized);
  }

  public void testNormalizeMultiLineString() throws Exception {
    MultiLineString actualValue = (MultiLineString)this.geometryFactory.geometry(
      "MULTILINESTRING ((200 260,180 320,260 340),(120 180,140 100,40 80),(200 180,220 160,200 180),(100 280,120 260,140 260,140 240,120 240,120 260,100 280))");
    actualValue = actualValue.normalize();
    final MultiLineString expectedValue = (MultiLineString)this.geometryFactory.geometry(
      "MULTILINESTRING ((40 80,140 100,120 180),(100 280,120 260,120 240,140 240,140 260,120 260,100 280),(200 180,220 160,200 180),(200 260,180 320,260 340))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizeMultiPoint() throws Exception {
    Geometry m = this.geometryFactory
      .geometry("MULTIPOINT((30 20),(10 10),(20 20),(30 30),(20 10))");
    m = m.normalize();
    final Punctual expectedValue = (Punctual)this.geometryFactory
      .geometry("MULTIPOINT((10 10),(20 10),(20 20),(30 20),(30 30))");
    assertEqualsExact(expectedValue, m);
    final Punctual unexpectedValue = (Punctual)this.geometryFactory
      .geometry("MULTIPOINT((20 10),(20 20),(30 20),(30 30),(10 10))");
    assertTrue(!m.equals(2, unexpectedValue));
  }

  public void testNormalizeMultiPolygon() throws Exception {
    Polygonal actualValue = (Polygonal)this.geometryFactory.geometry(
      "MULTIPOLYGON(((40 360,40 280,140 280,140 360,40 360),(60 340,60 300,120 300,120 340,60 340)),((140 200,260 200,260 100,140 100,140 200),(160 180,240 180,240 120,160 120,160 180)))");
    actualValue = actualValue.normalize();
    final Polygonal expectedValue = (Polygonal)this.geometryFactory.geometry(
      "MULTIPOLYGON(((40 280,40 360,140 360,140 280,40 280),(60 300,120 300,120 340,60 340,60 300)),((140 100,140 200,260 200,260 100,140 100),(160 120,240 120,240 180,160 180,160 120)))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizePoint() throws Exception {
    Point point = this.geometryFactory.geometry("POINT (30 30)");
    point = point.normalize();
    CoordinateTest.assertEquals(point.getPoint(), 30, 30);
  }

  public void testNormalizePolygon1() throws Exception {
    Polygon actualValue = (Polygon)this.geometryFactory.geometry(
      "POLYGON((120 320,240 200,120 80,20 200,120 320),(60 200,80 220,80 200,60 200),(160 200,180 200,180 220,160 200),(120 140,140 140,140 160,120 140),(140 240,140 220,120 260,140 240))");
    actualValue = actualValue.normalize();
    final Polygon expectedValue = (Polygon)this.geometryFactory.geometry(
      "POLYGON((20 200,120 320,240 200,120 80,20 200),(60 200,80 200,80 220,60 200),(120 140,140 140,140 160,120 140),(120 260,140 220,140 240,120 260),(160 200,180 200,180 220,160 200))");
    assertEqualsExact(expectedValue, actualValue);
  }
}
