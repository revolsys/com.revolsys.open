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

package com.revolsys.jts.geom;

import junit.framework.TestCase;

import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class NormalizeTest extends TestCase {

  public static void main(final String[] args) {
    final String[] testCaseName = {
      NormalizeTest.class.getName()
    };
    junit.textui.TestRunner.main(testCaseName);
  }

  PrecisionModel precisionModel = new PrecisionModel(1);

  GeometryFactory geometryFactory = new GeometryFactory(this.precisionModel, 0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public NormalizeTest(final String Name_) {
    super(Name_);
  }

  private void assertEqualsExact(final Geometry expectedValue,
    final Geometry actualValue) {
    assertTrue("Expected " + expectedValue + " but encountered " + actualValue,
      actualValue.equalsExact(expectedValue));
  }

  public void testCompareEmptyPoint() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT (30 30)");
    final Point p2 = (Point)this.reader.read("POINT EMPTY");
    assertTrue(p1.compareTo(p2) > 0);
  }

  public void testComparePoint() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT (30 30)");
    final Point p2 = (Point)this.reader.read("POINT (30 40)");
    assertTrue(p1.compareTo(p2) < 0);
  }

  public void testNormalizeEmptyLineString() throws Exception {
    final LineString l = (LineString)this.reader.read("LINESTRING EMPTY");
    l.normalize();
    final LineString expectedValue = (LineString)this.reader.read("LINESTRING EMPTY");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeEmptyPoint() throws Exception {
    final Point point = (Point)this.reader.read("POINT EMPTY");
    point.normalize();
    assertEquals(null, point.getCoordinate());
  }

  public void testNormalizeEmptyPolygon() throws Exception {
    final Polygon actualValue = (Polygon)this.reader.read("POLYGON EMPTY");
    actualValue.normalize();
    final Polygon expectedValue = (Polygon)this.reader.read("POLYGON EMPTY");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizeGeometryCollection() throws Exception {
    final GeometryCollection actualValue = (GeometryCollection)this.reader.read("GEOMETRYCOLLECTION (LINESTRING (200 300, 200 280, 220 280, 220 320, 180 320), POINT (140 220), POLYGON ((100 80, 100 160, 20 160, 20 80, 100 80), (40 140, 40 100, 80 100, 80 140, 40 140)), POINT (100 240))");
    actualValue.normalize();
    final GeometryCollection expectedValue = (GeometryCollection)this.reader.read("GEOMETRYCOLLECTION (POINT (100 240), POINT (140 220), LINESTRING (180 320, 220 320, 220 280, 200 280, 200 300), POLYGON ((20 80, 20 160, 100 160, 100 80, 20 80), (40 100, 80 100, 80 140, 40 140, 40 100)))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizeLineString1() throws Exception {
    final LineString l = (LineString)this.reader.read("LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    l.normalize();
    final LineString expectedValue = (LineString)this.reader.read("LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString2() throws Exception {
    final LineString l = (LineString)this.reader.read("LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    l.normalize();
    final LineString expectedValue = (LineString)this.reader.read("LINESTRING (20 20, 160 40, 160 100, 100 120, 60 60)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString3() throws Exception {
    final LineString l = (LineString)this.reader.read("LINESTRING (200 240, 140 160, 80 160, 160 80, 80 80)");
    l.normalize();
    final LineString expectedValue = (LineString)this.reader.read("LINESTRING (80 80, 160 80, 80 160, 140 160, 200 240)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString4() throws Exception {
    final LineString l = (LineString)this.reader.read("LINESTRING (200 240, 140 160, 80 160, 160 80, 80 80)");
    l.normalize();
    final LineString expectedValue = (LineString)this.reader.read("LINESTRING (80 80, 160 80, 80 160, 140 160, 200 240)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeLineString5() throws Exception {
    final LineString l = (LineString)this.reader.read("LINESTRING (200 340, 140 240, 140 160, 60 240, 140 240, 200 340)");
    l.normalize();
    final LineString expectedValue = (LineString)this.reader.read("LINESTRING (200 340, 140 240, 60 240, 140 160, 140 240, 200 340)");
    assertEqualsExact(expectedValue, l);
  }

  public void testNormalizeMultiLineString() throws Exception {
    final MultiLineString actualValue = (MultiLineString)this.reader.read("MULTILINESTRING ((200 260, 180 320, 260 340), (120 180, 140 100, 40 80), (200 180, 220 160, 200 180), (100 280, 120 260, 140 260, 140 240, 120 240, 120 260, 100 280))");
    actualValue.normalize();
    final MultiLineString expectedValue = (MultiLineString)this.reader.read("MULTILINESTRING ((40 80, 140 100, 120 180), (100 280, 120 260, 120 240, 140 240, 140 260, 120 260, 100 280), (200 180, 220 160, 200 180), (200 260, 180 320, 260 340))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizeMultiPoint() throws Exception {
    final MultiPoint m = (MultiPoint)this.reader.read("MULTIPOINT(30 20, 10 10, 20 20, 30 30, 20 10)");
    m.normalize();
    final MultiPoint expectedValue = (MultiPoint)this.reader.read("MULTIPOINT(10 10, 20 10, 20 20, 30 20, 30 30)");
    assertEqualsExact(expectedValue, m);
    final MultiPoint unexpectedValue = (MultiPoint)this.reader.read("MULTIPOINT(20 10, 20 20, 30 20, 30 30, 10 10)");
    assertTrue(!m.equalsExact(unexpectedValue));
  }

  public void testNormalizeMultiPolygon() throws Exception {
    final MultiPolygon actualValue = (MultiPolygon)this.reader.read("MULTIPOLYGON (((40 360, 40 280, 140 280, 140 360, 40 360), (60 340, 60 300, 120 300, 120 340, 60 340)), ((140 200, 260 200, 260 100, 140 100, 140 200), (160 180, 240 180, 240 120, 160 120, 160 180)))");
    actualValue.normalize();
    final MultiPolygon expectedValue = (MultiPolygon)this.reader.read("MULTIPOLYGON (((40 280, 40 360, 140 360, 140 280, 40 280), (60 300, 120 300, 120 340, 60 340, 60 300)), ((140 100, 140 200, 260 200, 260 100, 140 100), (160 120, 240 120, 240 180, 160 180, 160 120)))");
    assertEqualsExact(expectedValue, actualValue);
  }

  public void testNormalizePoint() throws Exception {
    final Point point = (Point)this.reader.read("POINT (30 30)");
    point.normalize();
    assertEquals(new Coordinate(30, 30), point.getCoordinate());
  }

  public void testNormalizePolygon1() throws Exception {
    final Polygon actualValue = (Polygon)this.reader.read("POLYGON ((120 320, 240 200, 120 80, 20 200, 120 320), (60 200, 80 220, 80 200, 60 200), (160 200, 180 200, 180 220, 160 200), (120 140, 140 140, 140 160, 120 140), (140 240, 140 220, 120 260, 140 240))");
    actualValue.normalize();
    final Polygon expectedValue = (Polygon)this.reader.read("POLYGON ((20 200, 120 320, 240 200, 120 80, 20 200), (60 200, 80 200, 80 220, 60 200), (120 140, 140 140, 140 160, 120 140), (120 260, 140 220, 140 240, 120 260), (160 200, 180 200, 180 220, 160 200))");
    assertEqualsExact(expectedValue, actualValue);
  }
}
