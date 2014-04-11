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

import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.jts.algorithm.MinimumBoundingCircle;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class MinimumBoundingCircleTest extends TestCase {

  static final double TOLERANCE = 1.0e-5;

  public static void main(final String args[]) {
    TestRunner.run(MinimumBoundingCircleTest.class);
  }

  private final PrecisionModel precisionModel = new PrecisionModel(1);

  private final GeometryFactory geometryFactory = new GeometryFactory(
    this.precisionModel, 0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public MinimumBoundingCircleTest(final String name) {
    super(name);
  }

  private void doMinimumBoundingCircleTest(final String wkt,
    final String expectedWKT) throws ParseException {
    doMinimumBoundingCircleTest(wkt, expectedWKT, null, -1);
  }

  private void doMinimumBoundingCircleTest(final String wkt,
    final String expectedWKT, final AbstractCoordinates expectedCentre,
    final double expectedRadius) throws ParseException {
    final MinimumBoundingCircle mbc = new MinimumBoundingCircle(
      this.reader.read(wkt));
    final Coordinates[] exPts = mbc.getExtremalPoints();
    final Geometry actual = this.geometryFactory.createMultiPoint(exPts);
    final double actualRadius = mbc.getRadius();
    final Coordinates actualCentre = mbc.getCentre();
    System.out.println("   Centre = " + actualCentre + "   Radius = "
      + actualRadius);

    final Geometry expected = this.reader.read(expectedWKT);
    boolean isEqual = actual.equals(expected);
    // need this hack because apparently equals does not work for MULTIPOINT
    // EMPTY
    if (actual.isEmpty() && expected.isEmpty()) {
      isEqual = true;
    }
    if (!isEqual) {
      System.out.println("Actual = " + actual + ", Expected = " + expected);
    }
    assertTrue(isEqual);

    if (expectedCentre != null) {
      assertTrue(expectedCentre.distance(actualCentre) < TOLERANCE);
    }
    if (expectedRadius >= 0) {
      assertTrue(Math.abs(expectedRadius - actualRadius) < TOLERANCE);
    }
  }

  public void testEmptyPoint() throws Exception {
    doMinimumBoundingCircleTest("POINT EMPTY", "MULTIPOINT EMPTY");
  }

  public void testObtuseTriangle() throws Exception {
    doMinimumBoundingCircleTest(
      "POLYGON ((100 100, 200 100, 150 90, 100 100))",
      "MULTIPOINT ((100 100), (200 100))", new Coordinate(150, 100, Coordinates.NULL_ORDINATE), 50);
  }

  public void testPoint() throws Exception {
    doMinimumBoundingCircleTest("POINT (10 10)", "POINT (10 10)",
      new Coordinate(10, 10, Coordinates.NULL_ORDINATE), 0);
  }

  public void testPoints2() throws Exception {
    doMinimumBoundingCircleTest("MULTIPOINT ((10 10), (20 20))",
      "MULTIPOINT ((10 10), (20 20))", new Coordinate(15, 15, Coordinates.NULL_ORDINATE),
      7.0710678118654755);
  }

  public void testPoints3() throws Exception {
    doMinimumBoundingCircleTest("MULTIPOINT ((10 10), (20 20), (10 20))",
      "MULTIPOINT ((10 10), (20 20), (10 20))", new Coordinate(15, 15, Coordinates.NULL_ORDINATE),
      7.0710678118654755);
  }

  public void testPointsInLine() throws Exception {
    doMinimumBoundingCircleTest("MULTIPOINT ((10 10), (20 20), (30 30))",
      "MULTIPOINT ((10 10), (30 30))", new Coordinate(20, 20, Coordinates.NULL_ORDINATE),
      14.142135623730951);
  }

  public void testTriangleWithMiddlePoint() throws Exception {
    doMinimumBoundingCircleTest(
      "MULTIPOINT ((10 10), (20 20), (10 20), (15 19))",
      "MULTIPOINT ((10 10), (20 20), (10 20))", new Coordinate(15, 15, Coordinates.NULL_ORDINATE),
      7.0710678118654755);
  }

}
