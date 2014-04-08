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

package test.jts.junit;

import java.util.ArrayList;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateSequence;
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
    assertTrue(this.geometryFactory.createPoint((Coordinate)null)
      .getBoundary()
      .getClass() == GeometryCollection.class);
    assertTrue(this.geometryFactory.createLinearRing(new Coordinate[] {})
      .getBoundary()
      .getClass() == MultiPoint.class);
    assertTrue(this.geometryFactory.createLineString(new Coordinate[] {})
      .getBoundary()
      .getClass() == MultiPoint.class);
    assertTrue(this.geometryFactory.createPolygon(
      this.geometryFactory.createLinearRing(new Coordinate[] {}),
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
    final Coordinate c1 = new Coordinate();
    assertTrue(!Double.isNaN(c1.x));
    assertTrue(!Double.isNaN(c1.y));
    assertTrue(Double.isNaN(c1.z));

    final Coordinate c2 = new Coordinate(3, 4);
    assertEquals(3, c2.x, 1E-10);
    assertEquals(4, c2.y, 1E-10);
    assertTrue(Double.isNaN(c2.z));

    assertEquals(c1, c1);
    assertEquals(c2, c2);
    assertTrue(!c1.equals(c2));
    assertEquals(new Coordinate(), new Coordinate(0, 0));
    assertEquals(new Coordinate(3, 5), new Coordinate(3, 5));
    assertEquals(new Coordinate(3, 5, Double.NaN), new Coordinate(3, 5,
      Double.NaN));
    assertTrue(new Coordinate(3, 5, 0).equals(new Coordinate(3, 5, Double.NaN)));
  }

  public void testCreateEmptyGeometry() throws Exception {
    assertTrue(this.geometryFactory.createPoint((Coordinate)null).isEmpty());
    assertTrue(this.geometryFactory.createLinearRing(new Coordinate[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.createLineString(new Coordinate[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.createPolygon(
      this.geometryFactory.createLinearRing(new Coordinate[] {}),
      new LinearRing[] {}).isEmpty());
    assertTrue(this.geometryFactory.createMultiPolygon(new Polygon[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.createMultiLineString(new LineString[] {})
      .isEmpty());
    assertTrue(this.geometryFactory.createMultiPoint(new Point[] {}).isEmpty());

    assertTrue(this.geometryFactory.createPoint((Coordinate)null).isSimple());
    assertTrue(this.geometryFactory.createLinearRing(new Coordinate[] {})
      .isSimple());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(geometryFactory.createLineString(new Coordinate[] {
    // }).isSimple());
    // assertTrue(geometryFactory.createPolygon(geometryFactory.createLinearRing(new
    // Coordinate[] { }), new LinearRing[] { }).isSimple());
    // assertTrue(geometryFactory.createMultiPolygon(new Polygon[] {
    // }).isSimple());
    // assertTrue(geometryFactory.createMultiLineString(new LineString[] {
    // }).isSimple());
    // assertTrue(geometryFactory.createMultiPoint(new Point[] { }).isSimple());

    assertTrue(this.geometryFactory.createPoint((Coordinate)null)
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.createLinearRing(new Coordinate[] {})
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.createLineString(new Coordinate[] {})
      .getBoundary()
      .isEmpty());
    assertTrue(this.geometryFactory.createPolygon(
      this.geometryFactory.createLinearRing(new Coordinate[] {}),
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

    assertTrue(this.geometryFactory.createLinearRing((CoordinateSequence)null)
      .isEmpty());
    assertTrue(this.geometryFactory.createLineString((Coordinate[])null)
      .isEmpty());
    assertTrue(this.geometryFactory.createPolygon(null, null).isEmpty());
    assertTrue(this.geometryFactory.createMultiPolygon(null).isEmpty());
    assertTrue(this.geometryFactory.createMultiLineString(null).isEmpty());
    assertTrue(this.geometryFactory.createMultiPoint((Point[])null).isEmpty());

    assertEquals(-1, this.geometryFactory.createPoint((Coordinate)null)
      .getBoundaryDimension());
    assertEquals(-1,
      this.geometryFactory.createLinearRing((CoordinateSequence)null)
        .getBoundaryDimension());
    assertEquals(0, this.geometryFactory.createLineString((Coordinate[])null)
      .getBoundaryDimension());
    assertEquals(1, this.geometryFactory.createPolygon(null, null)
      .getBoundaryDimension());
    assertEquals(1, this.geometryFactory.createMultiPolygon(null)
      .getBoundaryDimension());
    assertEquals(0, this.geometryFactory.createMultiLineString(null)
      .getBoundaryDimension());
    assertEquals(-1, this.geometryFactory.createMultiPoint((Point[])null)
      .getBoundaryDimension());

    assertEquals(0, this.geometryFactory.createPoint((Coordinate)null)
      .getNumPoints());
    assertEquals(0,
      this.geometryFactory.createLinearRing((CoordinateSequence)null)
        .getNumPoints());
    assertEquals(0, this.geometryFactory.createLineString((Coordinate[])null)
      .getNumPoints());
    assertEquals(0, this.geometryFactory.createPolygon(null, null)
      .getNumPoints());
    assertEquals(0, this.geometryFactory.createMultiPolygon(null)
      .getNumPoints());
    assertEquals(0, this.geometryFactory.createMultiLineString(null)
      .getNumPoints());
    assertEquals(0, this.geometryFactory.createMultiPoint((Point[])null)
      .getNumPoints());

    assertEquals(0, this.geometryFactory.createPoint((Coordinate)null)
      .getCoordinates().length);
    assertEquals(0,
      this.geometryFactory.createLinearRing((CoordinateSequence)null)
        .getCoordinates().length);
    assertEquals(0, this.geometryFactory.createLineString((Coordinate[])null)
      .getCoordinates().length);
    assertEquals(0, this.geometryFactory.createPolygon(null, null)
      .getCoordinates().length);
    assertEquals(0, this.geometryFactory.createMultiPolygon(null)
      .getCoordinates().length);
    assertEquals(0, this.geometryFactory.createMultiLineString(null)
      .getCoordinates().length);
    assertEquals(0, this.geometryFactory.createMultiPoint((Point[])null)
      .getCoordinates().length);
  }

  public void testEmptyGeometryCollection() throws Exception {
    final GeometryCollection g = this.geometryFactory.createGeometryCollection(null);
    assertEquals(-1, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
    assertTrue(g.isSimple());
  }

  public void testEmptyLinearRing() throws Exception {
    final LineString l = this.geometryFactory.createLinearRing((CoordinateSequence)null);
    assertEquals(1, l.getDimension());
    assertEquals(new Envelope(), l.getEnvelopeInternal());
    assertTrue(l.isSimple());
    assertEquals(null, l.getStartPoint());
    assertEquals(null, l.getEndPoint());
    assertTrue(l.isClosed());
    assertTrue(l.isRing());
  }

  public void testEmptyLineString() throws Exception {
    final LineString l = this.geometryFactory.createLineString((Coordinate[])null);
    assertEquals(1, l.getDimension());
    assertEquals(new Envelope(), l.getEnvelopeInternal());
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
    final MultiLineString g = this.geometryFactory.createMultiLineString(null);
    assertEquals(1, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(g.isSimple());
    assertTrue(!g.isClosed());
  }

  public void testEmptyMultiPoint() throws Exception {
    final MultiPoint g = this.geometryFactory.createMultiPoint((Point[])null);
    assertEquals(0, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(g.isSimple());
  }

  public void testEmptyMultiPolygon() throws Exception {
    final MultiPolygon g = this.geometryFactory.createMultiPolygon(null);
    assertEquals(2, g.getDimension());
    assertEquals(new Envelope(), g.getEnvelopeInternal());
    assertTrue(g.isSimple());
  }

  public void testEmptyPoint() throws Exception {
    final Point p = this.geometryFactory.createPoint((Coordinate)null);
    assertEquals(0, p.getDimension());
    assertEquals(new Envelope(), p.getEnvelopeInternal());
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
    assertEquals("POINT EMPTY", p.toText());
  }

  public void testEmptyPolygon() throws Exception {
    final Polygon p = this.geometryFactory.createPolygon(null, null);
    assertEquals(2, p.getDimension());
    assertEquals(new Envelope(), p.getEnvelopeInternal());
    assertTrue(p.isSimple());
  }

  public void testEnvelopeCloned() throws Exception {
    final Geometry a = this.reader.read("LINESTRING(0 0, 10 10)");
    // Envelope is lazily initialized [Jon Aquino]
    a.getEnvelopeInternal();
    final Geometry b = (Geometry)a.clone();
    assertTrue(a.getEnvelopeInternal() != b.getEnvelopeInternal());
  }

  public void testGetGeometryType() throws Exception {
    final GeometryCollection g = this.geometryFactory.createMultiPolygon(null);
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
    final Coordinate[] coordinates = {
      new Coordinate(10, 10, 0), new Coordinate(10, 20, 0),
      new Coordinate(20, 20, 0), new Coordinate(20, 15, 0),
      new Coordinate(10, 10, 0)
    };
    final LinearRing linearRing = this.geometryFactory.createLinearRing(coordinates);
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
    assertTrue(boundary.getGeometryN(0).equals(g.getStartPoint()));
    assertTrue(boundary.getGeometryN(1).equals(g.getEndPoint()));
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
    final Coordinate[] coordinates = p.getCoordinates();
    assertEquals(10, p.getNumPoints());
    assertEquals(10, coordinates.length);
    assertEquals(new Coordinate(0, 0), coordinates[0]);
    assertEquals(new Coordinate(20, 20), coordinates[9]);
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
    final Point p1 = new GeometryFactory().createPoint((Coordinate)null);
    final Point p2 = new GeometryFactory().createPoint(new Coordinate(5, 5));
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
    list.add(this.geometryFactory.createPoint(new Coordinate(0, 0)));
    list.add(this.geometryFactory.createPoint(new Coordinate(10, 0)));
    list.add(this.geometryFactory.createPoint(new Coordinate(10, 10)));
    list.add(this.geometryFactory.createPoint(new Coordinate(0, 10)));
    list.add(this.geometryFactory.createPoint(new Coordinate(0, 0)));
    final Point[] points = GeometryFactory.toPointArray(list);
    assertEquals(10, points[1].getX(), 1E-1);
    assertEquals(0, points[1].getY(), 1E-1);
  }

}
