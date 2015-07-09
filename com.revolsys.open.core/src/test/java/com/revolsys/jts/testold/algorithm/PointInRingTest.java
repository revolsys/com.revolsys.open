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

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.io.WKTReader;

import junit.textui.TestRunner;

/**
 * Tests PointInRing algorithms
 *
 * @version 1.7
 */
public class PointInRingTest extends AbstractPointInRingTest {

  public static void main(final String args[]) {
    TestRunner.run(PointInRingTest.class);
  }

  private final WKTReader reader = new WKTReader();

  public PointInRingTest(final String name) {
    super(name);
  }

  @Override
  protected void runPtInRing(final Location expectedLoc, final Point pt, final String wkt)
    throws Exception {
    // isPointInRing is not defined for pts on boundary
    if (expectedLoc == Location.BOUNDARY) {
      return;
    }

    final Polygon geom = (Polygon)this.reader.read(wkt);
    final boolean expected = expectedLoc == Location.INTERIOR;
    assertEquals(expected, CGAlgorithms.isPointInRing(pt, geom.getShell()));
  }

}
