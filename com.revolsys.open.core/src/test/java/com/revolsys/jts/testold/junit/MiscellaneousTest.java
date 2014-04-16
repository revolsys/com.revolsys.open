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

package com.revolsys.jts.testold.junit;

import java.util.ArrayList;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
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
import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class MiscellaneousTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(MiscellaneousTest.class);
  }

  PrecisionModel precisionModel = new PrecisionModel(1);

  GeometryFactory geometryFactory = new GeometryFactory(this.precisionModel, 0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public MiscellaneousTest(final String name) {
    super(name);
  }

  public void testBoundaryOfEmptyGeometry() throws Exception {
    assertTrue(this.geometryFactory.point((Coordinates)null)
      .getBoundary()
      .getClass() == GeometryCollection.class);
    assertTrue(this.geometryFactory.linearRing(new Coordinates[] {})
      .getBoundary()
      .getClass() == MultiPoint.class);
    assertTrue(this.geometryFactory.lineString(new Coordinates[] {})
      .getBoundary()
      .getClass() == MultiPoint.class);
    assertTrue(this.geometryFactory.createPolygon(
      this.geometryFactory.linearRing(new Coordinates[] {}),
      new LinearRing[] {})
      .getBoundary()
      .getClass() == MultiLineString.class);
    assertTrue(this.geometryFactory.createMultiPolygon(new Polygon[] {})
      .getBoundary()
      .getClass() == MultiLineString.class);
    assertTrue(this.geometryFactory.createMultiLineString(new LineString[] {})
      .getBoundary()
      .getClass() == MultiPoint.class);
    assertTrue(this.geometryFactory.createMultiPoint(new Point[] {})
      .getBoundary()
      .getClass() == GeometryCollection.class);
    try {
      this.geometryFactory.createGeometryCollection(new Geometry[] {})
        .getBoundary();
      assertTrue(false);
    } catch (final IllegalArgumentException e) {
    }
  }

  public void testCoordinateNaN() {
    final Coordinates c1 = new Coordinate();
    assertTrue(!Double.isNaN(c1.getX()));
    assertTrue(!Double.isNaN(c1.getY()));
    assertTrue(Double.isNaN(c1.getZ()));

    final Coordinates c2 = new Coordinate((double)3, 4,
      Coordinates.NULL_ORDINATE);
    assertEquals(3, c2.getX(), 1E-10);
    assertEquals(4, c2.getY(), 1E-10);
    assertTrue(Double.isNaN(c2.getZ()));

    assertEquals(c1, c1);
    assertEquals(c2, c2);
    assertTrue(!c1.equals(c2));
    assertEquals(new Coordinate(), new Coordinate((double)0, 0,
      Coordinates.NULL_ORDINATE));
    assertEquals(new Coordinate((double)3, 5, Coordinates.NULL_ORDINATE),
      new Coordinate((double)3, 5, Coordinates.NULL_ORDINATE));
    assertEquals(new Coordinate((double)3, 5, Double.NaN), new Coordinate(
      (double)3, 5, Double.NaN));
    assertTrue(new Coordinate((double)3, 5, 0).equals(new Coordinate((double)3,
      5, Double.NaN)));
  }

  public void testCreateEmptyGeometry() throws Exception {
    assertTrue(this.geometryFactory.point((Coordinates)null).isEmpty());
    assertTrue(this.geometryFactory.linearRing(new Coordinates[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.lineString(new Coordinates[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.createPolygon(
      this.geometryFactory.linearRing(new Coordinates[] {}),
      new LinearRing[] {}).isEmpty());
    assertTrue(this.geometryFactory.createMultiPolygon(new Polygon[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.createMultiLineString(new LineString[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.createMultiPoint(new Point[] {}).isEmpty());

    assertTrue(this.geometryFactory.point((Coordinates)null).isSimple());
    assertTrue(this.geometryFactory.linearRing(new Coordinates[] {})
      .isSimple());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(geometryFactory.createLineString(new Coordinates[] {
    // }).isSimple());
    // assertTrue(geometryFactory.createPolygon(geometryFactory.createLinearRing(new
    // Coordinates[] { }), new LinearRing[] { }).isSimple());
    // assertTrue(geometryFactory.createMultiPolygon(new Polygon[] {
    // }).isSimple());
    // assertTrue(geometryFactory.createMultiLineString(new LineString[] {
    // }).isSimple());
    // assertTrue(geometryFactory.createMultiPoint(new Point[] { }).isSimple());

    assertTrue(this.geometryFactory.point((Coordinates)null)
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.linearRing(new Coordinates[] {})
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.lineString(new Coordinates[] {})
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.createPolygon(
      this.geometryFactory.linearRing(new Coordinates[] {}),
      new LinearRing[] {})
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.createMultiPolygon(new Polygon[] {})
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.createMultiLineString(new LineString[] {})
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.createMultiPoint(new Point[] {})
      .getBoundary()
      .isEmpty());

    assertTrue(this.geometryFactory.linearRing((CoordinatesList)null)
      .isEmpty());
    assertTrue(this.geometryFactory.lineString((Coordinates[])null)
      .isEmpty());
    assertTrue(this.geometryFactory.createPolygon(null, null).isEmpty());
    assertTrue(this.geometryFactory.createMultiPolygon().isEmpty());
    assertTrue(this.geometryFactory.createMultiLineString().isEmpty());
    assertTrue(this.geometryFactory.createMultiPoint((Point[])null).isEmpty());

    assertEquals(-1, this.geometryFactory.point((Coordinates)null)
      .getBoundaryDimension());
    assertEquals(-1,
      this.geometryFactory.linearRing((CoordinatesList)null)
        .getBoundaryDimension());
    assertEquals(0, this.geometryFactory.lineString((Coordinates[])null)
      .getBoundaryDimension());
    assertEquals(1, this.geometryFactory.createPolygon(null, null)
      .getBoundaryDimension());
    assertEquals(1, this.geometryFactory.createMultiPolygon()
      .getBoundaryDimension());
    assertEquals(0, this.geometryFactory.createMultiLineString()
      .getBoundaryDimension());
    assertEquals(-1, this.geometryFactory.createMultiPoint((Point[])null)
      .getBoundaryDimension());

    assertEquals(0, this.geometryFactory.point((Coordinates)null)
      .getVertexCount());
    assertEquals(0,
      this.geometryFactory.linearRing((CoordinatesList)null)
        .getVertexCount());
    assertEquals(0, this.geometryFactory.lineString((Coordinates[])null)
      .getVertexCount());
    assertEquals(0, this.geometryFactory.createPolygon(null, null)
      .getVertexCount());
    assertEquals(0, this.geometryFactory.createMultiPolygon().getVertexCount());
    assertEquals(0, this.geometryFactory.createMultiLineString().getVertexCount());
    assertEquals(0, this.geometryFactory.createMultiPoint((Point[])null)
      .getVertexCount());

    assertEquals(0, this.geometryFactory.point((Coordinates)null)
      .getCoordinateArray().length);
    assertEquals(0,
      this.geometryFactory.linearRing((CoordinatesList)null)
        .getCoordinateArray().length);
    assertEquals(0, this.geometryFactory.lineString((Coordinates[])null)
      .getCoordinateArray().length);
    assertEquals(0, this.geometryFactory.createPolygon(null, null)
      .getCoordinateArray().length);
    assertEquals(0, this.geometryFactory.createMultiPolygon()
      .getCoordinateArray().length);
    assertEquals(0, this.geometryFactory.createMultiLineString()
      .getCoordinateArray().length);
    assertEquals(0, this.geometryFactory.createMultiPoint((Point[])null)
      .getCoordinateArray().length);
  }

  public void testEmptyGeometryCollection() throws Exception {
    final GeometryCollection g = this.geometryFactory.createGeometryCollection();
    assertEquals(-1, g.getDimension());
    assertEquals(new Envelope(), g.getBoundingBox());
    assertTrue(g.isSimple());
  }

  public void testEmptyLinearRing() throws Exception {
    final LineString l = this.geometryFactory.linearRing((CoordinatesList)null);
    assertEquals(1, l.getDimension());
    assertEquals(new Envelope(), l.getBoundingBox());
    assertTrue(l.isSimple());
    assertEquals(null, l.getStartPoint());
    assertEquals(null, l.getEndPoint());
    assertTrue(l.isClosed());
    assertTrue(l.isRing());
  }

  public void testEmptyLineString() throws Exception {
    final LineString l = this.geometryFactory.lineString((Coordinates[])null);
    assertEquals(1, l.getDimension());
    assertEquals(new Envelope(), l.getBoundingBox());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(l.isSimple());
    assertEquals(null, l.getStartPoint());
    assertEquals(null, l.getEndPoint());
    assertTrue(!l.isClosed());
    assertTrue(!l.isRing());
  }

  public void testEmptyMultiLineString() throws Exception {
    final MultiLineString g = this.geometryFactory.createMultiLineString();
    assertEquals(1, g.getDimension());
    assertEquals(new Envelope(), g.getBoundingBox());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(g.isSimple());
    assertTrue(!g.isClosed());
  }

  public void testEmptyMultiPoint() throws Exception {
    final MultiPoint g = this.geometryFactory.createMultiPoint((Point[])null);
    assertEquals(0, g.getDimension());
    assertEquals(new Envelope(), g.getBoundingBox());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(g.isSimple());
  }

  public void testEmptyMultiPolygon() throws Exception {
    final MultiPolygon g = this.geometryFactory.createMultiPolygon();
    assertEquals(2, g.getDimension());
    assertEquals(new Envelope(), g.getBoundingBox());
    assertTrue(g.isSimple());
  }

  public void testEmptyPoint() throws Exception {
    final Point p = this.geometryFactory.point((Coordinates)null);
    assertEquals(0, p.getDimension());
    assertEquals(new Envelope(), p.getBoundingBox());
    assertTrue(p.isSimple());
    try {
      p.getX();
      assertTrue(false);
    } catch (final IllegalStateException e1) {
    }
    try {
      p.getY();
      assertTrue(false);
    } catch (final IllegalStateException e2) {
    }

    assertEquals("POINT EMPTY", p.toString());
    assertEquals("POINT EMPTY", p.toWkt());
  }

  public void testEmptyPolygon() throws Exception {
    final Polygon p = this.geometryFactory.createPolygon(null, null);
    assertEquals(2, p.getDimension());
    assertEquals(new Envelope(), p.getBoundingBox());
    assertTrue(p.isSimple());
  }

  public void testEnvelopeCloned() throws Exception {
    final Geometry a = this.reader.read("LINESTRING(0 0, 10 10)");
    // Envelope is lazily initialized [Jon Aquino]
    a.getBoundingBox();
    final Geometry b = (Geometry)a.clone();
    assertTrue(a.getBoundingBox() != b.getBoundingBox());
  }

  public void testGetGeometryType() throws Exception {
    final GeometryCollection g = this.geometryFactory.createMultiPolygon();
    assertEquals("MultiPolygon", g.getGeometryType());
  }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testLineStringIsSimple2() throws Exception {
  // Geometry g = reader.read("LINESTRING(10 10, 20 10, 15 20, 15 0)");
  // assertTrue(! g.isSimple());
  // }

  public void testLinearRingIsSimple() throws Exception {
    final Coordinates[] coordinates = {
      new Coordinate((double)10, 10, 0), new Coordinate((double)10, 20, 0),
      new Coordinate((double)20, 20, 0), new Coordinate((double)20, 15, 0),
      new Coordinate((double)10, 10, 0)
    };
    final LinearRing linearRing = this.geometryFactory.linearRing(coordinates);
    assertTrue(linearRing.isSimple());
  }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testLineStringIsSimple1() throws Exception {
  // Geometry g = reader.read("LINESTRING(10 10, 20 10, 15 20)");
  // assertTrue(g.isSimple());
  // }

  public void testLineStringGetBoundary1() throws Exception {
    final LineString g = (LineString)this.reader.read("LINESTRING(10 10, 20 10, 15 20)");
    assertTrue(g.getBoundary() instanceof MultiPoint);
    final MultiPoint boundary = (MultiPoint)g.getBoundary();
    assertTrue(boundary.getGeometry(0).equals(g.getStartPoint()));
    assertTrue(boundary.getGeometry(1).equals(g.getEndPoint()));
  }

  public void testLineStringGetBoundary2() throws Exception {
    final LineString g = (LineString)this.reader.read("LINESTRING(10 10, 20 10, 15 20, 10 10)");
    assertTrue(g.getBoundary().isEmpty());
  }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testMultiLineStringIsSimple2() throws Exception {
  // Geometry g = reader.read("MULTILINESTRING("
  // + "(0 0,  100 0),"
  // + "(50 0, 100 10))");
  // assertTrue(! g.isSimple());
  // }

  public void testMultiLineStringGetBoundary1() throws Exception {
    final Geometry g = this.reader.read("MULTILINESTRING("
      + "(0 0,  100 0, 50 50)," + "(50 50, 50 -50))");
    final Geometry m = this.reader.read("MULTIPOINT(0 0, 50 -50)");
    assertTrue(m.equalsExact(g.getBoundary()));
  }

  public void testMultiLineStringGetBoundary2() throws Exception {
    final Geometry g = this.reader.read("MULTILINESTRING("
      + "(0 0,  100 0, 50 50)," + "(50 50, 50 0))");
    final Geometry m = this.reader.read("MULTIPOINT(0 0, 50 0)");
    assertTrue(m.equalsExact(g.getBoundary()));
  }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testMultiPointIsSimple2() throws Exception {
  // Geometry g = reader.read("MULTIPOINT(10 10, 30 30, 30 30)");
  // assertTrue(! g.isSimple());
  // }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testMultiPointIsSimple1() throws Exception {
  // Geometry g = reader.read("MULTIPOINT(10 10, 20 20, 30 30)");
  // assertTrue(g.isSimple());
  // }

  public void testMultiPointGetBoundary() throws Exception {
    final Geometry g = this.reader.read("MULTIPOINT(10 10, 20 20, 30 30)");
    assertTrue(g.getBoundary().isEmpty());
  }

  public void testMultiPolygonGetBoundary1() throws Exception {
    final Geometry g = this.reader.read("MULTIPOLYGON("
      + "(  (0 0, 40 0, 40 40, 0 40, 0 0),"
      + "   (10 10, 30 10, 30 30, 10 30, 10 10)  ),"
      + "(  (200 200, 210 200, 210 210, 200 200) )  )");
    final Geometry b = this.reader.read("MULTILINESTRING("
      + "(0 0, 40 0, 40 40, 0 40, 0 0),"
      + "(10 10, 30 10, 30 30, 10 30, 10 10),"
      + "(200 200, 210 200, 210 210, 200 200))");
    assertTrue(b.equalsExact(g.getBoundary()));
  }

  public void testMultiPolygonIsSimple1() throws Exception {
    final Geometry g = this.reader.read("MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))");
    assertTrue(g.isSimple());
  }

  public void testMultiPolygonIsSimple2() throws Exception {
    final Geometry g = this.reader.read("MULTIPOLYGON("
      + "((10 10, 10 20, 20 20, 20 15, 10 10)), "
      + "((60 60, 70 70, 80 60, 60 60))  )");
    assertTrue(g.isSimple());
  }

  public void testPointGetBoundary() throws Exception {
    final Geometry g = this.reader.read("POINT (10 10)");
    assertTrue(g.getBoundary().isEmpty());
  }

  public void testPointIsSimple() throws Exception {
    final Geometry g = this.reader.read("POINT (10 10)");
    assertTrue(g.isSimple());
  }

  public void testPolygonGetBoundary() throws Exception {
    final Geometry g = this.reader.read("POLYGON("
      + "(0 0, 40 0, 40 40, 0 40, 0 0),"
      + "(10 10, 30 10, 30 30, 10 30, 10 10))");
    final Geometry b = this.reader.read("MULTILINESTRING("
      + "(0 0, 40 0, 40 40, 0 40, 0 0),"
      + "(10 10, 30 10, 30 30, 10 30, 10 10))");
    assertTrue(b.equalsExact(g.getBoundary()));
  }

  // public void testGeometryCollectionIsSimple1() throws Exception {
  // Geometry g = reader.read("GEOMETRYCOLLECTION("
  // + "LINESTRING(0 0,  100 0),"
  // + "LINESTRING(0 10, 100 10))");
  // assertTrue(g.isSimple());
  // }

  // public void testGeometryCollectionIsSimple2() throws Exception {
  // Geometry g = reader.read("GEOMETRYCOLLECTION("
  // + "LINESTRING(0 0,  100 0),"
  // + "LINESTRING(50 0, 100 10))");
  // assertTrue(! g.isSimple());
  // }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testMultiLineStringIsSimple1() throws Exception {
  // Geometry g = reader.read("MULTILINESTRING("
  // + "(0 0,  100 0),"
  // + "(0 10, 100 10))");
  // assertTrue(g.isSimple());
  // }

  public void testPolygonGetCoordinates() throws Exception {
    final Polygon p = (Polygon)this.reader.read("POLYGON ( (0 0, 100 0, 100 100, 0 100, 0 0), "
      + "          (20 20, 20 80, 80 80, 80 20, 20 20)) ");
    final Coordinates[] coordinates = p.getCoordinateArray();
    assertEquals(10, p.getVertexCount());
    assertEquals(10, coordinates.length);
    assertEquals(new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE),
      coordinates[0]);
    assertEquals(new Coordinate((double)20, 20, Coordinates.NULL_ORDINATE),
      coordinates[9]);
  }

  public void testPolygonIsSimple() throws Exception {
    final Geometry g = this.reader.read("POLYGON((10 10, 10 20, 202 0, 20 15, 10 10))");
    assertTrue(g.isSimple());
  }

  // public void testGeometryCollectionGetBoundary1() throws Exception {
  // Geometry g = reader.read("GEOMETRYCOLLECTION("
  // + "POLYGON((0 0, 100 0, 100 100, 0 100, 0 0)),"
  // + "LINESTRING(200 100, 200 0))");
  // Geometry b = reader.read("GEOMETRYCOLLECTION("
  // + "LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0),"
  // + "LINESTRING(200 100, 200 0))");
  // assertEquals(b, g.getBoundary());
  // assertTrue(! g.equals(g.getBoundary()));
  // }

  // public void testGeometryCollectionGetBoundary2() throws Exception {
  // Geometry g = reader.read("GEOMETRYCOLLECTION("
  // + "POLYGON((0 0, 100 0, 100 100, 0 100, 0 0)),"
  // + "LINESTRING(50 50, 60 60))");
  // Geometry b = reader.read("GEOMETRYCOLLECTION("
  // + "LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0))");
  // assertEquals(b, g.getBoundary());
  // }

  // public void testGeometryCollectionGetBoundary3() throws Exception {
  // Geometry g = reader.read("GEOMETRYCOLLECTION("
  // + "POLYGON((0 0, 100 0, 100 100, 0 100, 0 0)),"
  // + "LINESTRING(50 50, 150 50))");
  // Geometry b = reader.read("GEOMETRYCOLLECTION("
  // + "LINESTRING(0 0, 100 0, 100 100, 0 100, 0 0),"
  // + "POINT(150 50))");
  // assertEquals(b, g.getBoundary());
  // }

  public void testPredicatesReturnFalseForEmptyGeometries() {
    final Point p1 = GeometryFactory.getFactory()
      .point((Coordinates)null);
    final Point p2 = GeometryFactory.getFactory().point(
      new Coordinate((double)5, 5, Coordinates.NULL_ORDINATE));
    assertEquals(false, p1.equals(p2));
    assertEquals(true, p1.disjoint(p2));
    assertEquals(false, p1.intersects(p2));
    assertEquals(false, p1.touches(p2));
    assertEquals(false, p1.crosses(p2));
    assertEquals(false, p1.within(p2));
    assertEquals(false, p1.contains(p2));
    assertEquals(false, p1.overlaps(p2));

    assertEquals(false, p2.equals(p1));
    assertEquals(true, p2.disjoint(p1));
    assertEquals(false, p2.intersects(p1));
    assertEquals(false, p2.touches(p1));
    assertEquals(false, p2.crosses(p1));
    assertEquals(false, p2.within(p1));
    assertEquals(false, p2.contains(p1));
    assertEquals(false, p2.overlaps(p1));
  }

  public void testToPointArray() {
    final ArrayList list = new ArrayList();
    list.add(this.geometryFactory.point(new Coordinate((double)0, 0,
      Coordinates.NULL_ORDINATE)));
    list.add(this.geometryFactory.point(new Coordinate((double)10, 0,
      Coordinates.NULL_ORDINATE)));
    list.add(this.geometryFactory.point(new Coordinate((double)10, 10,
      Coordinates.NULL_ORDINATE)));
    list.add(this.geometryFactory.point(new Coordinate((double)0, 10,
      Coordinates.NULL_ORDINATE)));
    list.add(this.geometryFactory.point(new Coordinate((double)0, 0,
      Coordinates.NULL_ORDINATE)));
    final Point[] points = GeometryFactory.toPointArray(list);
    assertEquals(10, points[1].getX(), 1E-1);
    assertEquals(0, points[1].getY(), 1E-1);
  }

}
