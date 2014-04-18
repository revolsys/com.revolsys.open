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

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * Tests for {@link GeometryFactoryI}.
 *
 * @version 1.13
 */
public class GeometryFactoryTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(GeometryFactoryTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.getFactory(0,
    2);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public GeometryFactoryTest(final String name) {
    super(name);
  }

  private void checkCreateGeometryExact(final String wkt) throws ParseException {
    final Geometry g = geometryFactory.geometry(wkt);
    final Geometry g2 = this.geometryFactory.geometry(g);
    if (!g.equalsExact(g2)) {
      failNotEquals("Geometry not equal exact", g, g2);
    }
  }

  private Geometry read(final String wkt) throws ParseException {
    return this.reader.read(wkt);
  }

  public void testCreateGeometry() throws ParseException {
    checkCreateGeometryExact("POINT EMPTY");
    checkCreateGeometryExact("POINT(10 20)");
    checkCreateGeometryExact("LINESTRING EMPTY");
    checkCreateGeometryExact("LINESTRING(0 0,10 10)");
    checkCreateGeometryExact("MULTILINESTRING((50 100, 100 200),(100 100,150 200))");
    checkCreateGeometryExact("POLYGON((100 200,200 200,200 100,100 100,100 200))");
    checkCreateGeometryExact("MULTIPOLYGON(((100 200,200 200,200 100,100 100,100 200)),((300 200,400 200,400 100,300 100,300 200)))");
    // checkCreateGeometryExact("GEOMETRYCOLLECTION(POLYGON((100 200,200 200,200 100,100 100,100 200)),LINESTRING(250 100,350 200),POINT(350 150))");
  }

  //
  // public void testDeepCopy() throws ParseException {
  // final Point g = (Point)read("POINT(10 10) ");
  // final Geometry g2 = this.geometryFactory.createGeometry(g);
  // g.getCoordinatesList().setOrdinate(0, 0, 99);
  // assertTrue(!g.equalsExact(g2));
  // }

  public void testMultiPointCS() {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(3005, 4,
      1, 1);
    final CoordinatesList mpSeq = new DoubleCoordinatesList(2, 50, -2, 10, 20);

    final MultiPoint mp = geometryFactory.createMultiPoint(mpSeq);
    final CoordinatesList pSeq = ((Point)mp.getGeometry(0)).getCoordinatesList();
    assertEquals(4, pSeq.getNumAxis());
    for (int i = 0; i < 4; i++) {
      assertEquals(mpSeq.getValue(0, i), pSeq.getValue(0, i));
    }
  }
}
