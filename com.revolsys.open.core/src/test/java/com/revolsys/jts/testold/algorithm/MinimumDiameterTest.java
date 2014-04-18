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
package com.revolsys.jts.testold.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.algorithm.MinimumDiameter;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class MinimumDiameterTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(MinimumDiameterTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.getFactory(0,
    1.0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public MinimumDiameterTest(final String name) {
    super(name);
  }

  private void doMinimumDiameterTest(final boolean convex, final String wkt,
    final Coordinates c0, final Coordinates c1) throws ParseException {
    final Coordinates[] minimumDiameter = new MinimumDiameter(
      new WKTReader().read(wkt), convex).getDiameter().getCoordinateArray();
    final double tolerance = 1E-10;
    assertEquals(c0.getX(), minimumDiameter[0].getX(), tolerance);
    assertEquals(c0.getY(), minimumDiameter[0].getY(), tolerance);
    assertEquals(c1.getX(), minimumDiameter[1].getX(), tolerance);
    assertEquals(c1.getY(), minimumDiameter[1].getY(), tolerance);
  }

  public void testMinimumDiameter1() throws Exception {
    doMinimumDiameterTest(true, "POINT (0 240)", new Coordinate((double)0, 240,
      Coordinates.NULL_ORDINATE), new Coordinate((double)0, 240,
      Coordinates.NULL_ORDINATE));
  }

  public void testMinimumDiameter2() throws Exception {
    doMinimumDiameterTest(true, "LINESTRING (0 240, 220 240)", new Coordinate(
      (double)0, 240, Coordinates.NULL_ORDINATE), new Coordinate((double)0,
      240, Coordinates.NULL_ORDINATE));
  }

  public void testMinimumDiameter3() throws Exception {
    doMinimumDiameterTest(true,
      "POLYGON ((0 240, 220 240, 220 0, 0 0, 0 240))", new Coordinate(
        (double)220, 240, Coordinates.NULL_ORDINATE), new Coordinate((double)0,
        240, Coordinates.NULL_ORDINATE));
  }

  public void testMinimumDiameter4() throws Exception {
    doMinimumDiameterTest(true,
      "POLYGON ((0 240, 220 240, 220 0, 0 0, 0 240))", new Coordinate(
        (double)220, 240, Coordinates.NULL_ORDINATE), new Coordinate((double)0,
        240, Coordinates.NULL_ORDINATE));
  }

  public void testMinimumDiameter5() throws Exception {
    doMinimumDiameterTest(true,
      "POLYGON ((0 240, 160 140, 220 0, 0 0, 0 240))", new Coordinate(
        185.86206896551724, 79.65517241379311, Coordinates.NULL_ORDINATE),
      new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE));
  }

  public void testMinimumDiameter6() throws Exception {
    doMinimumDiameterTest(
      false,
      "LINESTRING ( 39 119, 162 197, 135 70, 95 35, 33 66, 111 82, 97 131, 48 160, -4 182, 57 195, 94 202, 90 174, 75 134, 47 114, 0 100, 59 81, 123 60, 136 43, 163 75, 145 114, 93 136, 92 159, 105 175 )",
      new Coordinate(64.46262341325811, 196.41184767277855,
        Coordinates.NULL_ORDINATE), new Coordinate((double)95, 35,
        Coordinates.NULL_ORDINATE));
  }

}
