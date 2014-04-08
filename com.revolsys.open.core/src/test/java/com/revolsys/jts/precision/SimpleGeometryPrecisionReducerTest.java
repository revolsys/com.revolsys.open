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
package com.revolsys.jts.precision;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.WKTReader;

/**
 * @version 1.7
 */
public class SimpleGeometryPrecisionReducerTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(SimpleGeometryPrecisionReducerTest.class);
  }

  private final PrecisionModel pmFloat = new PrecisionModel();

  private final PrecisionModel pmFixed1 = new PrecisionModel(1);

  private final SimpleGeometryPrecisionReducer reducer = new SimpleGeometryPrecisionReducer(
    this.pmFixed1);

  private final SimpleGeometryPrecisionReducer reducerKeepCollapse = new SimpleGeometryPrecisionReducer(
    this.pmFixed1);

  private final GeometryFactory gfFloat = new GeometryFactory(this.pmFloat, 0);

  WKTReader reader = new WKTReader(this.gfFloat);

  public SimpleGeometryPrecisionReducerTest(final String name) {
    super(name);
    this.reducerKeepCollapse.setRemoveCollapsedComponents(false);

  }

  public void testLine() throws Exception {
    final Geometry g = this.reader.read("LINESTRING ( 0 0, 0 1.4 )");
    final Geometry g2 = this.reader.read("LINESTRING (0 0, 0 1)");
    final Geometry gReduce = this.reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }

  public void testLineKeepCollapse() throws Exception {
    final Geometry g = this.reader.read("LINESTRING ( 0 0, 0 .4 )");
    final Geometry g2 = this.reader.read("LINESTRING ( 0 0, 0 0 )");
    final Geometry gReduce = this.reducerKeepCollapse.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }

  public void testLineRemoveCollapse() throws Exception {
    final Geometry g = this.reader.read("LINESTRING ( 0 0, 0 .4 )");
    final Geometry g2 = this.reader.read("LINESTRING EMPTY");
    final Geometry gReduce = this.reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }

  public void testSquare() throws Exception {
    final Geometry g = this.reader.read("POLYGON (( 0 0, 0 1.4, 1.4 1.4, 1.4 0, 0 0 ))");
    final Geometry g2 = this.reader.read("POLYGON (( 0 0, 0 1, 1 1, 1 0, 0 0 ))");
    final Geometry gReduce = this.reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }

  public void testSquareCollapse() throws Exception {
    final Geometry g = this.reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
    final Geometry g2 = this.reader.read("POLYGON EMPTY");
    final Geometry gReduce = this.reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }

  public void testSquareKeepCollapse() throws Exception {
    final Geometry g = this.reader.read("POLYGON (( 0 0, 0 1.4, .4 .4, .4 0, 0 0 ))");
    final Geometry g2 = this.reader.read("POLYGON (( 0 0, 0 1, 0 0, 0 0, 0 0 ))");
    final Geometry gReduce = this.reducerKeepCollapse.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }

  public void testTinySquareCollapse() throws Exception {
    final Geometry g = this.reader.read("POLYGON (( 0 0, 0 .4, .4 .4, .4 0, 0 0 ))");
    final Geometry g2 = this.reader.read("POLYGON EMPTY");
    final Geometry gReduce = this.reducer.reduce(g);
    assertTrue(gReduce.equalsExact(g2));
  }

}
