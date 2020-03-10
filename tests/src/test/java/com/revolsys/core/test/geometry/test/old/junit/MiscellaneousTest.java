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

package com.revolsys.core.test.geometry.test.old.junit;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.geometry.util.Assert;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @version 1.7
 */
public class MiscellaneousTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(MiscellaneousTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.fixed2d(0, 1.0, 1.0);

  public MiscellaneousTest(final String name) {
    super(name);
  }

  public void testBoundaryOfEmptyGeometry() throws Exception {
    Assert.equals(this.geometryFactory.point().getBoundary().getDataType(),
      GeometryDataTypes.GEOMETRY_COLLECTION);
    org.junit.Assert.assertTrue("Instanceof Point",
      this.geometryFactory.linearRing().getBoundary() instanceof Point);
    org.junit.Assert.assertTrue("instanceof Point",
      this.geometryFactory.lineString(new Point[0]).getBoundary() instanceof Point);
    org.junit.Assert.assertTrue(this.geometryFactory.polygon().getBoundary() instanceof LineString);
    try {
      this.geometryFactory.geometryCollection().getBoundary();
      assertTrue(false);
    } catch (final IllegalArgumentException e) {
    }
  }

  public void testCoordinateNaN() {
    final Point c1 = GeometryFactory.DEFAULT_3D.point();

    final Point c2 = new PointDoubleXY(3, 4);
    assertEquals(3, c2.getX(), 1E-10);
    assertEquals(4, c2.getY(), 1E-10);
    assertTrue(Double.isNaN(c2.getZ()));

    assertEquals(c1, c1);
    assertEquals(c2, c2);
    assertTrue(!c1.equals(c2));
    assertEquals(new PointDoubleXY(3, 5), new PointDoubleXY(3, 5));
    assertEquals(new PointDoubleXY(3, 5), new PointDoubleXY(3, 5));
    assertTrue(new PointDoubleXYZ(3, 5, 0).equals(new PointDoubleXY(3, 5)));
  }

  public void testCreateEmptyGeometry() throws Exception {
    assertTrue(this.geometryFactory.point().isEmpty());
    assertTrue(this.geometryFactory.linearRing(new Point[] {}).isEmpty());
    assertTrue(this.geometryFactory.lineString(new Point[] {}).isEmpty());
    assertTrue(this.geometryFactory.polygon().isEmpty());
    assertTrue(this.geometryFactory.polygonal(new Polygon[] {}).isEmpty());
    assertTrue(this.geometryFactory.lineal(new LineString[] {}).isEmpty());
    assertTrue(this.geometryFactory.punctual(new Point[] {}).isEmpty());

    assertTrue(this.geometryFactory.point().isSimple());
    assertTrue(this.geometryFactory.linearRing(new Point[] {}).isSimple());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(geometryFactory.createLineString(new Point[] {
    // }).isSimple());
    // assertTrue(geometryFactory.createPolygon(geometryFactory.createLinearRing(new
    // Point[] { }), new LinearRing[] { }).isSimple());
    // assertTrue(geometryFactory.multiPolygon(new Polygon[] {
    // }).isSimple());
    // assertTrue(geometryFactory.multiLineString(new LineString[] {
    // }).isSimple());
    // assertTrue(geometryFactory.multiPoint(new Point[] { }).isSimple());

    assertTrue(this.geometryFactory.point().getBoundary().isEmpty());
    assertTrue(this.geometryFactory.linearRing(new Point[] {}).getBoundary().isEmpty());
    assertTrue(this.geometryFactory.lineString(new Point[] {}).getBoundary().isEmpty());
    assertTrue(this.geometryFactory.polygon().getBoundary().isEmpty());
    assertTrue(this.geometryFactory.polygonal(new Polygon[] {}).getBoundary().isEmpty());
    assertTrue(this.geometryFactory.lineal(new LineString[] {}).getBoundary().isEmpty());
    assertTrue(this.geometryFactory.punctual(new Point[] {}).getBoundary().isEmpty());

    assertTrue(this.geometryFactory.linearRing().isEmpty());
    assertTrue(this.geometryFactory.lineString().isEmpty());
    assertTrue(this.geometryFactory.polygon().isEmpty());

    assertEquals(Dimension.FALSE, this.geometryFactory.point((Point)null).getBoundaryDimension());
    assertEquals(Dimension.FALSE, this.geometryFactory.linearRing().getBoundaryDimension());
    assertEquals(Dimension.P, this.geometryFactory.lineString().getBoundaryDimension());
    assertEquals(Dimension.L, this.geometryFactory.polygon().getBoundaryDimension());

    assertEquals(0, this.geometryFactory.point().getVertexCount());
    assertEquals(0, this.geometryFactory.linearRing().getVertexCount());
    assertEquals(0, this.geometryFactory.lineString().getVertexCount());
    assertEquals(0, this.geometryFactory.polygon().getVertexCount());

    assertEquals(0, this.geometryFactory.point().getVertexCount());
    assertEquals(0, this.geometryFactory.linearRing().getVertexCount());
    assertEquals(0, this.geometryFactory.lineString().getVertexCount());
    assertEquals(0, this.geometryFactory.polygon().getVertexCount());
  }

  public void testEmptyGeometryCollection() throws Exception {
    final Geometry g = this.geometryFactory.geometryCollection();
    assertEquals(Dimension.FALSE, g.getDimension());
    assertEquals(BoundingBox.empty(), g.getBoundingBox());
    assertTrue(g.isSimple());
  }

  public void testEmptyLinearRing() throws Exception {
    final LineString l = this.geometryFactory.linearRing();
    assertEquals(Dimension.L, l.getDimension());
    assertEquals(BoundingBox.empty(), l.getBoundingBox());
    assertTrue(l.isSimple());
    assertEquals(null, l.getFromPoint());
    assertEquals(null, l.getToPoint());
    assertTrue(l.isClosed());
    assertTrue(l.isRing());
  }

  public void testEmptyLineString() throws Exception {
    final LineString l = this.geometryFactory.lineString();
    assertEquals(Dimension.L, l.getDimension());
    assertEquals(BoundingBox.empty(), l.getBoundingBox());
    /**
     * @todo Enable when #isSimple implemented
     */
    // assertTrue(l.isSimple());
    assertEquals(null, l.getFromPoint());
    assertEquals(null, l.getToPoint());
    assertTrue(!l.isClosed());
    assertTrue(!l.isRing());
  }

  public void testEmptyPoint() throws Exception {
    final Point p = this.geometryFactory.point();
    assertEquals(Dimension.P, p.getDimension());
    assertEquals(BoundingBox.empty(), p.getBoundingBox());
    assertTrue(p.isSimple());

    assertEquals("POINT EMPTY", p.toString());
    assertEquals("POINT EMPTY", p.toEwkt());
  }

  public void testEmptyPolygon() throws Exception {
    final Polygon p = this.geometryFactory.polygon();
    assertEquals(Dimension.A, p.getDimension());
    assertEquals(BoundingBox.empty(), p.getBoundingBox());
    assertTrue(p.isSimple());
  }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testLineStringIsSimple2() throws Exception {
  // Geometry g = reader.read("LINESTRING(10 10, 20 10, 15 20, 15 0)");
  // assertTrue(! g.isSimple());
  // }

  public void testLinearRingIsSimple() throws Exception {
    final LinearRing linearRing = this.geometryFactory.linearRing(2, 10.0, 10, 10, 20, 20, 20, 20,
      15, 10, 10);
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
    final LineString g = (LineString)this.geometryFactory
      .geometry("LINESTRING(10 10, 20 10, 15 20)");
    assertTrue(g.getBoundary() instanceof Punctual);
    final Punctual boundary = (Punctual)g.getBoundary();
    assertTrue(boundary.getPoint(0).equals(g.getFromPoint()));
    assertTrue(boundary.getPoint(1).equals(g.getToPoint()));
  }

  public void testLineStringGetBoundary2() throws Exception {
    final LineString g = (LineString)this.geometryFactory
      .geometry("LINESTRING(10 10, 20 10, 15 20, 10 10)");
    assertTrue(g.getBoundary().isEmpty());
  }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testMultiLineStringIsSimple2() throws Exception {
  // Geometry g = reader.read("MULTILINESTRING("
  // + "(0 0, 100 0),"
  // + "(50 0, 100 10))");
  // assertTrue(! g.isSimple());
  // }

  public void testMultiLineStringGetBoundary1() throws Exception {
    final Geometry g = this.geometryFactory
      .geometry("MULTILINESTRING((0 0,100 0,50 50),(50 50,50 -50))");
    final Geometry m = this.geometryFactory.geometry("MULTIPOINT((0 0),(50 -50))");
    assertTrue(m.equals(2, g.getBoundary()));
  }

  public void testMultiLineStringGetBoundary2() throws Exception {
    final Geometry g = this.geometryFactory
      .geometry("MULTILINESTRING((0 0,100 0,50 50),(50 50,50 0))");
    final Geometry m = this.geometryFactory.geometry("MULTIPOINT((0 0),(50 0))");
    assertTrue(m.equals(2, g.getBoundary()));
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
    final Geometry g = this.geometryFactory.geometry("MULTIPOINT((10 10),(20 20),(30 30))");
    assertTrue(g.getBoundary().isEmpty());
  }

  public void testMultiPolygonGetBoundary1() throws Exception {
    final Geometry g = this.geometryFactory
      .geometry("MULTIPOLYGON(" + "((0 0,40 0,40 40,0 40,0 0),(10 10,30 10,30 30,10 30,10 10)),"
        + "((200 200,210 200,210 210,200 200)))");
    final Geometry b = this.geometryFactory
      .geometry("MULTILINESTRING(" + "(0 0,40 0,40 40,0 40,0 0),"
        + "(10 10,30 10,30 30,10 30,10 10)," + "(200 200,210 200,210 210,200 200))");
    assertTrue(b.equals(2, g.getBoundary()));
  }

  public void testMultiPolygonIsSimple1() throws Exception {
    final Geometry g = this.geometryFactory.geometry(
      "MULTIPOLYGON (((10 10, 10 20, 20 20, 20 15, 10 10)), ((60 60, 70 70, 80 60, 60 60)))");
    assertTrue(g.isSimple());
  }

  public void testMultiPolygonIsSimple2() throws Exception {
    final Geometry g = this.geometryFactory
      .geometry("MULTIPOLYGON(((10 10,10 20,20 20,20 15,10 10)),((60 60,70 70,80 60,60 60)))");
    assertTrue(g.isSimple());
  }

  public void testPointGetBoundary() throws Exception {
    final Geometry g = this.geometryFactory.geometry("POINT (10 10)");
    assertTrue(g.getBoundary().isEmpty());
  }

  public void testPointIsSimple() throws Exception {
    final Geometry g = this.geometryFactory.geometry("POINT (10 10)");
    assertTrue(g.isSimple());
  }

  public void testPolygonGetBoundary() throws Exception {
    final Geometry g = this.geometryFactory.geometry(
      "POLYGON(" + "(0 0, 40 0, 40 40, 0 40, 0 0)," + "(10 10, 30 10, 30 30, 10 30, 10 10))");
    final Geometry b = this.geometryFactory.geometry("MULTILINESTRING("
      + "(0 0, 40 0, 40 40, 0 40, 0 0)," + "(10 10, 30 10, 30 30, 10 30, 10 10))");
    assertTrue(b.equals(2, g.getBoundary()));
  }

  // public void testGeometryCollectionIsSimple1() throws Exception {
  // Geometry g = reader.read("GEOMETRYCOLLECTION("
  // + "LINESTRING(0 0, 100 0),"
  // + "LINESTRING(0 10, 100 10))");
  // assertTrue(g.isSimple());
  // }

  // public void testGeometryCollectionIsSimple2() throws Exception {
  // Geometry g = reader.read("GEOMETRYCOLLECTION("
  // + "LINESTRING(0 0, 100 0),"
  // + "LINESTRING(50 0, 100 10))");
  // assertTrue(! g.isSimple());
  // }

  /**
   * @todo Enable when #isSimple implemented
   */
  // public void testMultiLineStringIsSimple1() throws Exception {
  // Geometry g = reader.read("MULTILINESTRING("
  // + "(0 0, 100 0),"
  // + "(0 10, 100 10))");
  // assertTrue(g.isSimple());
  // }

  public void testPolygonGetCoordinates() throws Exception {
    final Polygon p = (Polygon)this.geometryFactory
      .geometry("POLYGON ( (0 0, 100 0, 100 100, 0 100, 0 0), "
        + "          (20 20, 20 80, 80 80, 80 20, 20 20)) ");
    assertEquals(10, p.getVertexCount());
  }

  public void testPolygonIsSimple() throws Exception {
    final Geometry g = this.geometryFactory
      .geometry("POLYGON((10 10, 10 20, 202 0, 20 15, 10 10))");
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
    final Point p1 = GeometryFactory.DEFAULT_3D.point();
    final Point p2 = GeometryFactory.DEFAULT_3D.point(5.0, 5.0);
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

}
