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

import com.revolsys.jts.algorithm.PointLocator;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.io.WKTReader;

/**
 * Tests PointInRing algorithms
 *
 * @version 1.7
 */
public class PointLocatorTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(PointLocatorTest.class);
  }

  private final WKTReader reader = new WKTReader();

  public PointLocatorTest(final String name) {
    super(name);
  }

  private void runPtLocator(final Location expected, final Coordinates pt,
    final String wkt) throws Exception {
    final Geometry geom = this.reader.read(wkt);
    final PointLocator pointLocator = new PointLocator();
    final Location loc = pointLocator.locate(pt, geom);
    assertEquals(expected, loc);
  }

  public void testBox() throws Exception {
    runPtLocator(Location.INTERIOR, new Coordinate((double)10, 10,
      Coordinates.NULL_ORDINATE), "POLYGON ((0 0, 0 20, 20 20, 20 0, 0 0))");
  }

  public void testComplexRing() throws Exception {
    runPtLocator(
      Location.INTERIOR,
      new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE),
      "POLYGON ((-40 80, -40 -80, 20 0, 20 -100, 40 40, 80 -80, 100 80, 140 -20, 120 140, 40 180,     60 40, 0 120, -20 -20, -40 80))");
  }

  public void testPointLocatorLinearRingLineString() throws Exception {
    runPtLocator(
      Location.BOUNDARY,
      new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE),
      "GEOMETRYCOLLECTION( LINESTRING(0 0, 10 10), LINEARRING(10 10, 10 20, 20 10, 10 10))");
  }

  public void testPointLocatorPointInsideLinearRing() throws Exception {
    runPtLocator(Location.EXTERIOR, new Coordinate((double)11, 11,
      Coordinates.NULL_ORDINATE), "LINEARRING(10 10, 10 20, 20 10, 10 10)");
  }

}
