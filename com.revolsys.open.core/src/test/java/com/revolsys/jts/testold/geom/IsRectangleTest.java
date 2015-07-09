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

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Test named predicate short-circuits
 */
/**
 * @version 1.7
 */
public class IsRectangleTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(IsRectangleTest.class);
  }

  WKTReader rdr = new WKTReader();

  public IsRectangleTest(final String name) {
    super(name);
  }

  public boolean isRectangle(final String wkt) throws Exception {
    final Geometry a = this.rdr.read(wkt);
    return a.isRectangle();
  }

  public void testNotRectilinear() throws Exception {
    assertTrue(!isRectangle("POLYGON ((0 0, 0 100, 99 100, 100 0, 0 0))"));
  }

  public void testPointsInWrongOrder() throws Exception {
    assertTrue(!isRectangle("POLYGON ((0 0, 0 100, 100 0, 100 100, 0 0))"));
  }

  public void testRectangleWithHole() throws Exception {
    assertTrue(!isRectangle("POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0), (10 10, 10 90, 90 90, 90 10, 10 10) ))"));
  }

  public void testRectangularLinestring() throws Exception {
    assertTrue(!isRectangle("LINESTRING (0 0, 0 100, 100 100, 100 0, 0 0)"));
  }

  public void testTooFewPoints() throws Exception {
    assertTrue(!isRectangle("POLYGON ((0 0, 0 100, 100 0, 0 0))"));
  }

  public void testTooManyPoints() throws Exception {
    assertTrue(!isRectangle("POLYGON ((0 0, 0 100, 100 50, 100 100, 100 0, 0 0))"));
  }

  public void testValidRectangle() throws Exception {
    assertTrue(isRectangle("POLYGON ((0 0, 0 100, 100 100, 100 0, 0 0))"));
  }

  public void testValidRectangle2() throws Exception {
    assertTrue(isRectangle("POLYGON ((0 0, 0 200, 100 200, 100 0, 0 0))"));
  }
}
