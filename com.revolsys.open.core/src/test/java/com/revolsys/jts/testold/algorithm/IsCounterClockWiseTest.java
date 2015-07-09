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

import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests {@link LineString#isCounterClockwise()}
 * @version 1.7
 */
public class IsCounterClockWiseTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(IsCounterClockWiseTest.class);
  }

  private final WKTReader reader = new WKTReader();

  public IsCounterClockWiseTest(final String name) {
    super(name);
  }

  private LineString getLineString(final String wkt) throws ParseException {
    final Polygon geom = (Polygon)this.reader.read(wkt);
    return geom.getShell();
  }

  public void testCounterClockWise() throws Exception {
    final LineString pts = getLineString("POLYGON ((60 180, 140 240, 140 240, 140 240, 200 180, 120 120, 60 180))");
    assertEquals(pts.isCounterClockwise(), false);

    final LineString pts2 = getLineString("POLYGON ((60 180, 140 120, 100 180, 140 240, 60 180))");
    assertEquals(pts2.isCounterClockwise(), true);
    // same pts list with duplicate top point - check that isCounterClockise
    // still works
    final LineString pts2x = getLineString("POLYGON ((60 180, 140 120, 100 180, 140 240, 140 240, 60 180))");
    assertEquals(pts2x.isCounterClockwise(), true);
  }
}
