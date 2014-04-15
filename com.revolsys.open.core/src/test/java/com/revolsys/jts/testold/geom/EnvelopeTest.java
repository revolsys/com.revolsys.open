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
package com.revolsys.jts.testold.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.test.geometry.CoordinateTest;

/**
 * @version 1.7
 */
public class EnvelopeTest extends TestCase {
  public static void main(final String[] args) {
    TestRunner.run(EnvelopeTest.class);
  }

  private final PrecisionModel precisionModel = new PrecisionModel(1);

  private final GeometryFactory geometryFactory = new GeometryFactory(
    this.precisionModel, 0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public EnvelopeTest(final String name) {
    super(name);
  }

  void checkExpectedEnvelopeGeometry(final String wktInput)
    throws ParseException {
    checkExpectedEnvelopeGeometry(wktInput, wktInput);
  }

  void checkExpectedEnvelopeGeometry(final String wktInput,
    final String wktEnvGeomExpected) throws ParseException {
    final Geometry input = this.reader.read(wktInput);
    final Geometry envGeomExpected = this.reader.read(wktEnvGeomExpected);

    final Envelope env = input.getEnvelopeInternal();
    final Geometry envGeomActual = this.geometryFactory.toGeometry(env);
    final boolean isEqual = envGeomActual.equalsNorm(envGeomExpected);
    assertTrue(isEqual);
  }

  private Envelope expandToInclude(final Envelope a, final Envelope b) {
    a.expandToInclude(b);
    return a;
  }

  public void testAsGeometry() throws Exception {
    assertTrue(this.geometryFactory.point((Coordinates)null)
      .getEnvelope()
      .isEmpty());

    final Geometry g = this.geometryFactory.point(5.0, 6).getEnvelope();
    assertTrue(!g.isEmpty());
    assertTrue(g instanceof Point);

    final Point p = (Point)g;
    assertEquals(5, p.getX(), 1E-1);
    assertEquals(6, p.getY(), 1E-1);

    final LineString l = (LineString)this.reader.read("LINESTRING(10 10, 20 20, 30 40)");
    final Geometry g2 = l.getEnvelope();
    assertTrue(!g2.isEmpty());
    assertTrue(g2 instanceof Polygon);

    Polygon poly = (Polygon)g2;
    poly = poly.normalize();
    final LineString exteriorRing = poly.getExteriorRing();
    assertEquals(5, exteriorRing.getVertexCount());
    CoordinateTest.assertEquals(exteriorRing.getCoordinate(0), 10.0, 10);
    CoordinateTest.assertEquals(exteriorRing.getCoordinate(1), 10.0, 40);
    CoordinateTest.assertEquals(exteriorRing.getCoordinate(2), 30.0, 40);
    CoordinateTest.assertEquals(exteriorRing.getCoordinate(3), 30.0, 10);
    CoordinateTest.assertEquals(exteriorRing.getCoordinate(4), 10.0, 10);
  }

  public void testContainsEmpty() {
    assertTrue(!new Envelope(-5, 5, -5, 5).contains(new Envelope()));
    assertTrue(!new Envelope().contains(new Envelope(-5, 5, -5, 5)));
    assertTrue(!new Envelope().contains(new Envelope(100, 101, 100, 101)));
    assertTrue(!new Envelope(100, 101, 100, 101).contains(new Envelope()));
  }

  public void testCopyConstructor() throws Exception {
    final Envelope e1 = new Envelope(1, 2, 3, 4);
    final Envelope e2 = new Envelope(e1);
    assertEquals(1, e2.getMinX(), 1E-5);
    assertEquals(2, e2.getMaxX(), 1E-5);
    assertEquals(3, e2.getMinY(), 1E-5);
    assertEquals(4, e2.getMaxY(), 1E-5);
  }

  public void testEmpty() {
    assertEquals(0, new Envelope().getHeight(), 0);
    assertEquals(0, new Envelope().getWidth(), 0);
    assertEquals(new Envelope(), new Envelope());
    final Envelope e = new Envelope(100, 101, 100, 101);
    e.init(new Envelope());
    assertEquals(new Envelope(), e);
  }

  public void testEquals() throws Exception {
    final Envelope e1 = new Envelope(1, 2, 3, 4);
    final Envelope e2 = new Envelope(1, 2, 3, 4);
    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());

    final Envelope e3 = new Envelope(1, 2, 3, 5);
    assertTrue(!e1.equals(e3));
    assertTrue(e1.hashCode() != e3.hashCode());
    e1.setToNull();
    assertTrue(!e1.equals(e2));
    assertTrue(e1.hashCode() != e2.hashCode());
    e2.setToNull();
    assertEquals(e1, e2);
    assertEquals(e1.hashCode(), e2.hashCode());
  }

  public void testEquals2() {
    assertTrue(new Envelope().equals(new Envelope()));
    assertTrue(new Envelope(1, 2, 1, 2).equals(new Envelope(1, 2, 1, 2)));
    assertTrue(!new Envelope(1, 2, 1.5, 2).equals(new Envelope(1, 2, 1, 2)));
  }

  public void testEverything() throws Exception {
    final Envelope e1 = new Envelope();
    assertTrue(e1.isNull());
    assertEquals(0, e1.getWidth(), 1E-3);
    assertEquals(0, e1.getHeight(), 1E-3);
    e1.expandToInclude(100, 101);
    e1.expandToInclude(200, 202);
    e1.expandToInclude(150, 151);
    assertEquals(200, e1.getMaxX(), 1E-3);
    assertEquals(202, e1.getMaxY(), 1E-3);
    assertEquals(100, e1.getMinX(), 1E-3);
    assertEquals(101, e1.getMinY(), 1E-3);
    assertTrue(e1.contains(120, 120));
    assertTrue(e1.contains(120, 101));
    assertTrue(!e1.contains(120, 100));
    assertEquals(101, e1.getHeight(), 1E-3);
    assertEquals(100, e1.getWidth(), 1E-3);
    assertTrue(!e1.isNull());

    final Envelope e2 = new Envelope(499, 500, 500, 501);
    assertTrue(!e1.contains(e2));
    assertTrue(!e1.intersects(e2));
    e1.expandToInclude(e2);
    assertTrue(e1.contains(e2));
    assertTrue(e1.intersects(e2));
    assertEquals(500, e1.getMaxX(), 1E-3);
    assertEquals(501, e1.getMaxY(), 1E-3);
    assertEquals(100, e1.getMinX(), 1E-3);
    assertEquals(101, e1.getMinY(), 1E-3);

    final Envelope e3 = new Envelope(300, 700, 300, 700);
    assertTrue(!e1.contains(e3));
    assertTrue(e1.intersects(e3));

    final Envelope e4 = new Envelope(300, 301, 300, 301);
    assertTrue(e1.contains(e4));
    assertTrue(e1.intersects(e4));
  }

  public void testExpandToIncludeEmpty() {
    assertEquals(new Envelope(-5, 5, -5, 5),
      expandToInclude(new Envelope(-5, 5, -5, 5), new Envelope()));
    assertEquals(new Envelope(-5, 5, -5, 5),
      expandToInclude(new Envelope(), new Envelope(-5, 5, -5, 5)));
    assertEquals(new Envelope(100, 101, 100, 101),
      expandToInclude(new Envelope(), new Envelope(100, 101, 100, 101)));
    assertEquals(new Envelope(100, 101, 100, 101),
      expandToInclude(new Envelope(100, 101, 100, 101), new Envelope()));
  }

  public void testGeometryFactoryCreateEnvelope() throws Exception {
    checkExpectedEnvelopeGeometry("POINT (0 0)");
    checkExpectedEnvelopeGeometry("POINT (100 13)");
    checkExpectedEnvelopeGeometry("LINESTRING (0 0, 0 10)");
    checkExpectedEnvelopeGeometry("LINESTRING (0 0, 10 0)");

    final String poly10 = "POLYGON ((0 10, 10 10, 10 0, 0 0, 0 10))";
    checkExpectedEnvelopeGeometry(poly10);

    checkExpectedEnvelopeGeometry("LINESTRING (0 0, 10 10)", poly10);
    checkExpectedEnvelopeGeometry("POLYGON ((5 10, 10 6, 5 0, 0 6, 5 10))",
      poly10);

  }

  public void testIntersectsEmpty() {
    assertTrue(!new Envelope(-5, 5, -5, 5).intersects(new Envelope()));
    assertTrue(!new Envelope().intersects(new Envelope(-5, 5, -5, 5)));
    assertTrue(!new Envelope().intersects(new Envelope(100, 101, 100, 101)));
    assertTrue(!new Envelope(100, 101, 100, 101).intersects(new Envelope()));
  }

  public void testSetToNull() throws Exception {
    final Envelope e1 = new Envelope();
    assertTrue(e1.isNull());
    e1.expandToInclude(5, 5);
    assertTrue(!e1.isNull());
    e1.setToNull();
    assertTrue(e1.isNull());
  }
}
