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

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;

/**
 * Tests CGAlgorithms.isCCW
 * @version 1.7
 */
public class IsCCWTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(IsCCWTest.class);
  }

  private final WKTReader reader = new WKTReader();

  public IsCCWTest(final String name) {
    super(name);
  }

  private Coordinates[] getCoordinates(final String wkt) throws ParseException {
    final Geometry geom = this.reader.read(wkt);
    return geom.getCoordinateArray();
  }

  public void testCCW() throws Exception {
    final Coordinates[] pts = getCoordinates("POLYGON ((60 180, 140 240, 140 240, 140 240, 200 180, 120 120, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts), false);

    final Coordinates[] pts2 = getCoordinates("POLYGON ((60 180, 140 120, 100 180, 140 240, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts2), true);
    // same pts list with duplicate top point - check that isCCW still works
    final Coordinates[] pts2x = getCoordinates("POLYGON ((60 180, 140 120, 100 180, 140 240, 140 240, 60 180))");
    assertEquals(CGAlgorithms.isCCW(pts2x), true);
  }
}
