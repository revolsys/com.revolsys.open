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
package com.revolsys.jts.testold.precision;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @version 1.12
 */
public class GeometryPrecisionReducerTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(GeometryPrecisionReducerTest.class);
  }
  //
  // private final PrecisionModel pmFixed1 = new PrecisionModel(1);
  //
  // private final GeometryPrecisionReducer reducer = new
  // GeometryPrecisionReducer(
  // this.pmFixed1);
  //
  // private final GeometryPrecisionReducer reducerKeepCollapse = new
  // GeometryPrecisionReducer(
  // this.pmFixed1);
  //
  // private final GeometryFactory gfFloat = GeometryFactory.getFactory(0, 2);
  //
  // WKTReader reader = new WKTReader(this.gfFloat);
  //
  // public GeometryPrecisionReducerTest(final String name) {
  // super(name);
  // this.reducerKeepCollapse.setRemoveCollapsedComponents(false);
  // }
  //
  // private void assertEqualsExactAndHasSameFactory(final Geometry a,
  // final Geometry b) {
  // assertTrue(a.equalsExact(b));
  // assertTrue(a.getGeometryFactory() == b.getGeometryFactory());
  // }
  //
  // public void testLine() throws Exception {
  // final Geometry g = this.reader.read("LINESTRING ( 0 0, 0 1.4 )");
  // final Geometry g2 = this.reader.read("LINESTRING (0 0, 0 1)");
  // final Geometry gReduce = this.reducer.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testLineKeepCollapse() throws Exception {
  // final Geometry g = this.reader.read("LINESTRING ( 0 0, 0 .4 )");
  // final Geometry g2 = this.reader.read("LINESTRING ( 0 0, 0 0 )");
  // final Geometry gReduce = this.reducerKeepCollapse.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testLineRemoveCollapse() throws Exception {
  // final Geometry g = this.reader.read("LINESTRING ( 0 0, 0 .4 )");
  // final Geometry g2 = this.reader.read("LINESTRING EMPTY");
  // final Geometry gReduce = this.reducer.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testPolgonWithCollapsedLine() throws Exception {
  // final Geometry g =
  // this.reader.read("POLYGON ((10 10, 100 100, 200 10.1, 300 10, 10 10))");
  // final Geometry g2 =
  // this.reader.read("POLYGON ((10 10, 100 100, 200 10, 10 10))");
  // final Geometry gReduce = this.reducer.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testPolgonWithCollapsedPoint() throws Exception {
  // final Geometry g =
  // this.reader.read("POLYGON ((10 10, 100 100, 200 10.1, 300 100, 400 10, 10 10))");
  // final Geometry g2 =
  // this.reader.read("MULTIPOLYGON (((10 10, 100 100, 200 10, 10 10)), ((200 10, 300 100, 400 10, 200 10)))");
  // final Geometry gReduce = this.reducer.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testSquare() throws Exception {
  // final Geometry g =
  // this.reader.read("POLYGON (( 0 0, 0 1.4, 1.4 1.4, 1.4 0, 0 0 ))");
  // final Geometry g2 =
  // this.reader.read("POLYGON (( 0 0, 0 1, 1 1, 1 0, 0 0 ))");
  // final Geometry gReduce = this.reducer.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testSquareCollapse() throws Exception {
  // final Geometry g =
  // this.reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
  // final Geometry g2 = this.reader.read("POLYGON EMPTY");
  // final Geometry gReduce = this.reducer.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testSquareKeepCollapse() throws Exception {
  // final Geometry g =
  // this.reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
  // final Geometry g2 = this.reader.read("POLYGON EMPTY");
  // final Geometry gReduce = this.reducerKeepCollapse.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }
  //
  // public void testTinySquareCollapse() throws Exception {
  // final Geometry g =
  // this.reader.read("POLYGON (( 0 0, 0 .4, .4 .4, .4 0, 0 0 ))");
  // final Geometry g2 = this.reader.read("POLYGON EMPTY");
  // final Geometry gReduce = this.reducer.reduce(g);
  // assertEqualsExactAndHasSameFactory(gReduce, g2);
  // }

}
