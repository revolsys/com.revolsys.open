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

package com.revolsys.geometry.test.old.io;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.geometry.wkb.ParseException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test for {@link GeometryFactory#geometry}
 *
 * @version 1.7
 */
public class GeometryFactoryWktTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(GeometryFactoryWktTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.fixed(0, 1.0, 1.0);

  public GeometryFactoryWktTest(final String name) {
    super(name);
  }

  private void assertReaderEquals(final String expected, final String sourceWkt)
    throws ParseException {
    final Geometry actualGeometry = this.geometryFactory.geometry(sourceWkt);
    final String actualWkt = actualGeometry.toEwkt();
    assertEquals(expected, actualWkt);
  }

  public void testReadGeometryCollection() throws Exception {

    assertEquals("GEOMETRYCOLLECTION(POINT(10 10),POINT(30 30),LINESTRING(15 15,20 20))",
      this.geometryFactory
        .geometry("GEOMETRYCOLLECTION (POINT(10 10), POINT(30 30), LINESTRING(15 15, 20 20))")
        .toEwkt());
    assertEquals("GEOMETRYCOLLECTION(POINT(10 10),LINESTRING(15 15,20 20))",
      this.geometryFactory
        .geometry("GEOMETRYCOLLECTION(POINT(10 10),LINEARRING EMPTY,LINESTRING(15 15, 20 20))")
        .toEwkt());
    assertReaderEquals(
      "GEOMETRYCOLLECTION(POINT(10 10),LINEARRING(10 10,20 20,30 40,10 10),LINESTRING(15 15,20 20))",
      "GEOMETRYCOLLECTION(POINT(10 10),LINEARRING(10 10,20 20,30 40,10 10),LINESTRING(15 15,20 20))");
    assertEquals("GEOMETRYCOLLECTION EMPTY",
      this.geometryFactory.geometry("GEOMETRYCOLLECTION EMPTY").toEwkt());
  }

  public void testReadLargeNumbers() throws Exception {
    final GeometryFactory geometryFactory = GeometryFactory.fixed(0, 1E9, 1E9);
    final Geometry point1 = geometryFactory.geometry("POINT(123456789.01234567890 10)");
    final Point point2 = geometryFactory.point(123456789.01234567890, 10);
    assertEquals(point1.getPoint().getX(), point2.getPoint().getX(), 1E-7);
    assertEquals(point1.getPoint().getY(), point2.getPoint().getY(), 1E-7);
  }

  public void testReadLinearRing() throws Exception {
    try {
      this.geometryFactory.geometry("LINEARRING(10 10,20 20,30 40,10 99)");
    } catch (final IllegalArgumentException e) {
      assertTrue(e.getMessage().indexOf("not form a closed linestring") > -1);
    }

    assertEquals("LINEARRING(10 10,20 20,30 40,10 10)",
      this.geometryFactory.geometry("LINEARRING(10 10,20 20,30 40,10 10)").toEwkt());

    assertEquals("LINEARRING EMPTY", this.geometryFactory.geometry("LINEARRING EMPTY").toEwkt());
  }

  public void testReadLineString() throws Exception {

    assertEquals("LINESTRING(10 10,20 20,30 40)",
      this.geometryFactory.geometry("LINESTRING(10 10,20 20,30 40)").toEwkt());

    assertEquals("LINESTRING EMPTY", this.geometryFactory.geometry("LINESTRING EMPTY").toEwkt());
  }

  public void testReadMultiLineString() throws Exception {

    assertEquals("MULTILINESTRING((10 10,20 20),(15 15,30 15))",
      this.geometryFactory.geometry("MULTILINESTRING((10 10,20 20),(15 15,30 15))").toEwkt());

    assertEquals("LINESTRING EMPTY",
      this.geometryFactory.geometry("MULTILINESTRING EMPTY").toEwkt());
  }

  public void testReadMultiPoint() throws Exception {

    assertEquals("MULTIPOINT((10 10),(20 20))",
      this.geometryFactory.geometry("MULTIPOINT((10 10),(20 20))").toEwkt());

    assertEquals("POINT EMPTY", this.geometryFactory.geometry("MULTIPOINT EMPTY").toEwkt());
  }

  public void testReadMultiPolygon() throws Exception {

    final Geometry geometry = this.geometryFactory.geometry(
      "MULTIPOLYGON(((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))");
    final String ewkt = geometry.toEwkt();
    assertEquals("MULTIPOLYGON(((10 10,20 15,20 20,10 20,10 10)),((60 60,80 60,70 70,60 60)))",
      ewkt);

    assertEquals("POLYGON EMPTY", this.geometryFactory.geometry("MULTIPOLYGON EMPTY").toEwkt());
  }

  public void testReadNaN() throws Exception {

    assertEquals("POINT(10 10)", this.geometryFactory.geometry("POINT(10 10 NaN)").toEwkt());

    assertEquals("POINT(10 10)", this.geometryFactory.geometry("POINT(10 10 nan)").toEwkt());
    assertEquals("POINT(10 10)", this.geometryFactory.geometry("POINT(10 10 NAN)").toEwkt());
  }

  public void testReadPoint() throws Exception {

    assertEquals("POINT(10 10)", this.geometryFactory.geometry("POINT(10 10)").toEwkt());

    assertEquals("POINT EMPTY", this.geometryFactory.geometry("POINT EMPTY").toEwkt());
  }

  public void testReadPolygon() throws Exception {

    final Geometry geometry = this.geometryFactory
      .geometry("POLYGON((10 10,10 20,20 20,20 15,10 10))");
    final String ewkt = geometry.toEwkt();
    assertEquals("POLYGON((10 10,20 15,20 20,10 20,10 10))", ewkt);

    assertEquals("POLYGON EMPTY", this.geometryFactory.geometry("POLYGON EMPTY").toEwkt());
  }

  public void testReadZ() throws Exception {
    assertEquals(new PointDoubleXYZ(1, 2, 3),
      this.geometryFactory.geometry("POINT(1 2 3)").getPoint());
  }

}
