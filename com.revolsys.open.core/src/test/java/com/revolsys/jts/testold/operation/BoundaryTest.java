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
package com.revolsys.jts.testold.operation;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.algorithm.BoundaryNodeRule;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.io.ParseException;
import com.revolsys.jts.io.WKTReader;
import com.revolsys.jts.operation.BoundaryOp;

/**
 * Tests {@link BoundaryOp} with different {@link BoundaryNodeRule}s.
 *
 * @author Martin Davis
 * @version 1.7
 */
public class BoundaryTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(BoundaryTest.class);
  }

  private final GeometryFactory fact = GeometryFactory.getFactory();

  private final WKTReader rdr = new WKTReader(this.fact);

  public BoundaryTest(final String name) {
    super(name);
  }

  private void runBoundaryTest(final String wkt, final BoundaryNodeRule bnRule,
    final String wktExpected) throws ParseException {
    final Geometry g = this.rdr.read(wkt);
    final Geometry expected = this.rdr.read(wktExpected);

    final BoundaryOp op = new BoundaryOp(g, bnRule);
    Geometry boundary = op.getBoundary();
    boundary = boundary.normalize();
    // System.out.println("Computed Boundary = " + boundary);
    assertTrue(boundary.equalsExact(expected));
  }

  /**
   * For testing only.
   *
   * @throws Exception
   */
  public void test1() throws Exception {
    final String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))";
    // under MultiValent, the common point is the only point on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
      "POINT (10 10)");
  }

  public void test2LinesTouchAtEndpoint2() throws Exception {
    final String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20))";

    // under Mod-2, the common point is not on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,
      "MULTIPOINT ((0 0), (20 20))");
    // under Endpoint, the common point is on the boundary
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
      "MULTIPOINT ((0 0), (10 10), (20 20))");
    // under MonoValent, the common point is not on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE,
      "MULTIPOINT ((0 0), (20 20))");
    // under MultiValent, the common point is the only point on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
      "POINT (10 10)");
  }

  public void test3LinesTouchAtEndpoint2() throws Exception {
    final String a = "MULTILINESTRING ((0 0, 10 10), (10 10, 20 20), (10 10, 10 20))";

    // under Mod-2, the common point is on the boundary (3 mod 2 = 1)
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,
      "MULTIPOINT ((0 0), (10 10), (10 20), (20 20))");
    // under Endpoint, the common point is on the boundary (it is an endpoint)
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
      "MULTIPOINT ((0 0), (10 10), (10 20), (20 20))");
    // under MonoValent, the common point is not on the boundary (it has valence
    // > 1)
    runBoundaryTest(a, BoundaryNodeRule.MONOVALENT_ENDPOINT_BOUNDARY_RULE,
      "MULTIPOINT ((0 0), (10 20), (20 20))");
    // under MultiValent, the common point is the only point on the boundary
    runBoundaryTest(a, BoundaryNodeRule.MULTIVALENT_ENDPOINT_BOUNDARY_RULE,
      "POINT (10 10)");
  }

  public void testMultiLineStringWithRingTouchAtEndpoint() throws Exception {
    final String a = "MULTILINESTRING ((100 100, 20 20, 200 20, 100 100), (100 200, 100 100))";

    // under Mod-2, the ring has no boundary, so the line intersects the
    // interior ==> not simple
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE,
      "MULTIPOINT ((100 100), (100 200))");
    // under Endpoint, the ring has a boundary point, so the line does NOT
    // intersect the interior ==> simple
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
      "MULTIPOINT ((100 100), (100 200))");
  }

  public void testRing() throws Exception {
    final String a = "LINESTRING (100 100, 20 20, 200 20, 100 100)";

    // rings are simple under all rules
    runBoundaryTest(a, BoundaryNodeRule.MOD2_BOUNDARY_RULE, "MULTIPOINT EMPTY");
    runBoundaryTest(a, BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE,
      "POINT (100 100)");
  }

}
