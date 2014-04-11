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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.io.WKTReader;

/**
 * Test for com.revolsys.jts.testold.geom.impl.PointImpl.
 *
 * @version 1.7
 */
public class PointImplTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() {
    return new TestSuite(PointImplTest.class);
  }

  PrecisionModel precisionModel = new PrecisionModel(1000);

  GeometryFactory geometryFactory = new GeometryFactory(this.precisionModel, 0);

  WKTReader reader = new WKTReader(this.geometryFactory);

  public PointImplTest(final String name) {
    super(name);
  }

  public void testEquals1() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT(1.234 5.678)");
    final Point p2 = (Point)this.reader.read("POINT(1.234 5.678)");
    assertTrue(p1.equals(p2));
  }

  public void testEquals2() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT(1.23 5.67)");
    final Point p2 = (Point)this.reader.read("POINT(1.23 5.67)");
    assertTrue(p1.equals(p2));
  }

  public void testEquals3() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT(1.235 5.678)");
    final Point p2 = (Point)this.reader.read("POINT(1.234 5.678)");
    assertTrue(!p1.equals(p2));
  }

  public void testEquals4() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT(1.2334 5.678)");
    final Point p2 = (Point)this.reader.read("POINT(1.2333 5.678)");
    assertTrue(p1.equals(p2));
  }

  public void testEquals5() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT(1.2334 5.678)");
    final Point p2 = (Point)this.reader.read("POINT(1.2335 5.678)");
    assertTrue(!p1.equals(p2));
  }

  public void testEquals6() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT(1.2324 5.678)");
    final Point p2 = (Point)this.reader.read("POINT(1.2325 5.678)");
    assertTrue(!p1.equals(p2));
  }

  public void testIsSimple() throws Exception {
    final Point p1 = (Point)this.reader.read("POINT(1.2324 5.678)");
    assertTrue(p1.isSimple());
    final Point p2 = (Point)this.reader.read("POINT EMPTY");
    assertTrue(p2.isSimple());
  }

  public void testNegRounding1() throws Exception {
    final Point pLo = (Point)this.reader.read("POINT(-1.233 5.678)");
    final Point pHi = (Point)this.reader.read("POINT(-1.232 5.678)");

    final Point p1 = (Point)this.reader.read("POINT(-1.2326 5.678)");
    final Point p2 = (Point)this.reader.read("POINT(-1.2325 5.678)");
    final Point p3 = (Point)this.reader.read("POINT(-1.2324 5.678)");

    assertTrue(!p1.equals(p2));
    assertTrue(p3.equals(p2));

    assertTrue(p1.equals(pLo));
    assertTrue(p2.equals(pHi));
    assertTrue(p3.equals(pHi));
  }

}
