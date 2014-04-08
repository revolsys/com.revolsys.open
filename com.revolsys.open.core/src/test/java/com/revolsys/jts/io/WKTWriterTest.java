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

package com.revolsys.jts.io;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;

/**
 * Test for {@link WKTWriter}.
 *
 * @version 1.7
 */
public class WKTWriterTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(WKTWriterTest.class);
  }

  PrecisionModel precisionModel = new PrecisionModel(1);

  GeometryFactory geometryFactory = new GeometryFactory(this.precisionModel, 0);

  WKTWriter writer = new WKTWriter();

  WKTWriter writer3D = new WKTWriter(3);

  public WKTWriterTest(final String name) {
    super(name);
  }

  public void testWrite3D() {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory();
    final Point point = geometryFactory.createPoint(new Coordinate(1, 1, 1));
    final String wkt = this.writer3D.write(point);
    assertEquals("POINT (1 1 1)", wkt);
  }

  public void testWrite3D_withNaN() {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory();
    final Coordinate[] coordinates = {
      new Coordinate(1, 1), new Coordinate(2, 2, 2)
    };
    final LineString line = geometryFactory.createLineString(coordinates);
    final String wkt = this.writer3D.write(line);
    assertEquals("LINESTRING (1 1, 2 2 2)", wkt);
  }

  public void testWriteGeometryCollection() {
    final Point point1 = this.geometryFactory.createPoint(new Coordinate(10, 10));
    final Point point2 = this.geometryFactory.createPoint(new Coordinate(30, 30));
    final Coordinate[] coordinates = {
      new Coordinate(15, 15, 0), new Coordinate(20, 20, 0)
    };
    final LineString lineString1 = this.geometryFactory.createLineString(coordinates);
    final Geometry[] geometries = {
      point1, point2, lineString1
    };
    final GeometryCollection geometryCollection = this.geometryFactory.createGeometryCollection(geometries);
    assertEquals(
      "GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))",
      this.writer.write(geometryCollection).toString());
  }

  public void testWriteLargeNumbers1() {
    final PrecisionModel precisionModel = new PrecisionModel(1E9);
    final GeometryFactory geometryFactory = new GeometryFactory(precisionModel,
      0);
    final Point point1 = geometryFactory.createPoint(new Coordinate(
      123456789012345678d, 10E9));
    assertEquals("POINT (123456789012345680 10000000000)", point1.toText());
  }

  public void testWriteLargeNumbers2() {
    final PrecisionModel precisionModel = new PrecisionModel(1E9);
    final GeometryFactory geometryFactory = new GeometryFactory(precisionModel,
      0);
    final Point point1 = geometryFactory.createPoint(new Coordinate(1234d, 10E9));
    assertEquals("POINT (1234 10000000000)", point1.toText());
  }

  public void testWriteLargeNumbers3() {
    final PrecisionModel precisionModel = new PrecisionModel(1E9);
    final GeometryFactory geometryFactory = new GeometryFactory(precisionModel,
      0);
    final Point point1 = geometryFactory.createPoint(new Coordinate(
      123456789012345678000000E9d, 10E9));
    assertEquals("POINT (123456789012345690000000000000000 10000000000)",
      point1.toText());
  }

  public void testWriteLineString() {
    final Coordinate[] coordinates = {
      new Coordinate(10, 10, 0), new Coordinate(20, 20, 0),
      new Coordinate(30, 40, 0)
    };
    final LineString lineString = this.geometryFactory.createLineString(coordinates);
    assertEquals("LINESTRING (10 10, 20 20, 30 40)",
      this.writer.write(lineString).toString());
  }

  public void testWriteMultiLineString() {
    final Coordinate[] coordinates1 = {
      new Coordinate(10, 10, 0), new Coordinate(20, 20, 0)
    };
    final LineString lineString1 = this.geometryFactory.createLineString(coordinates1);
    final Coordinate[] coordinates2 = {
      new Coordinate(15, 15, 0), new Coordinate(30, 15, 0)
    };
    final LineString lineString2 = this.geometryFactory.createLineString(coordinates2);
    final LineString[] lineStrings = {
      lineString1, lineString2
    };
    final MultiLineString multiLineString = this.geometryFactory.createMultiLineString(lineStrings);
    assertEquals("MULTILINESTRING ((10 10, 20 20), (15 15, 30 15))",
      this.writer.write(multiLineString).toString());
  }

  public void testWriteMultiPoint() {
    final Point[] points = {
      this.geometryFactory.createPoint(new Coordinate(10, 10, 0)),
      this.geometryFactory.createPoint(new Coordinate(20, 20, 0))
    };
    final MultiPoint multiPoint = this.geometryFactory.createMultiPoint(points);
    assertEquals("MULTIPOINT ((10 10), (20 20))", this.writer.write(multiPoint)
      .toString());
  }

  public void testWriteMultiPolygon() throws Exception {
    final Coordinate[] coordinates1 = {
      new Coordinate(10, 10, 0), new Coordinate(10, 20, 0),
      new Coordinate(20, 20, 0), new Coordinate(20, 15, 0),
      new Coordinate(10, 10, 0)
    };
    final LinearRing linearRing1 = this.geometryFactory.createLinearRing(coordinates1);
    final Polygon polygon1 = this.geometryFactory.createPolygon(linearRing1,
      new LinearRing[] {});
    final Coordinate[] coordinates2 = {
      new Coordinate(60, 60, 0), new Coordinate(70, 70, 0),
      new Coordinate(80, 60, 0), new Coordinate(60, 60, 0)
    };
    final LinearRing linearRing2 = this.geometryFactory.createLinearRing(coordinates2);
    final Polygon polygon2 = this.geometryFactory.createPolygon(linearRing2,
      new LinearRing[] {});
    final Polygon[] polygons = {
      polygon1, polygon2
    };
    final MultiPolygon multiPolygon = this.geometryFactory.createMultiPolygon(polygons);
    // System.out.println("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))");
    // System.out.println(writer.write(multiPolygon).toString());
    assertEquals(
      "MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))",
      this.writer.write(multiPolygon).toString());
  }

  public void testWritePoint() {
    final Point point = this.geometryFactory.createPoint(new Coordinate(10, 10));
    assertEquals("POINT (10 10)", this.writer.write(point).toString());
  }

  public void testWritePolygon() throws Exception {
    final Coordinate[] coordinates = {
      new Coordinate(10, 10, 0), new Coordinate(10, 20, 0),
      new Coordinate(20, 20, 0), new Coordinate(20, 15, 0),
      new Coordinate(10, 10, 0)
    };
    final LinearRing linearRing = this.geometryFactory.createLinearRing(coordinates);
    final Polygon polygon = this.geometryFactory.createPolygon(linearRing,
      new LinearRing[] {});
    assertEquals("POLYGON ((10 10, 10 20, 20 20, 20 15, 10 10))",
      this.writer.write(polygon).toString());
  }

}
