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

package com.revolsys.geometry.test.old.geom;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Test for com.revolsys.geometry.testold.geom.impl.PointImpl.
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

  private final GeometryFactory albers2d = GeometryFactory.fixed(3005, 1000.0, 1000.0);

  private final GeometryFactory albers3d = GeometryFactory.fixed(3005, 1000.0, 1000.0, 1.0);

  private final GeometryFactory geometryFactory = GeometryFactory.fixed(0, 1000.0, 1000.0);

  private final GeometryFactory worldMercator = GeometryFactory.worldMercator();

  public PointImplTest(final String name) {
    super(name);
  }

  public void testEquals1() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(1.234 5.678)");
    final Point p2 = this.geometryFactory.geometry("POINT(1.234 5.678)");
    assertTrue(p1.equals(p2));
  }

  public void testEquals2() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(1.23 5.67)");
    final Point p2 = this.geometryFactory.geometry("POINT(1.23 5.67)");
    assertTrue(p1.equals(p2));
  }

  public void testEquals3() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(1.235 5.678)");
    final Point p2 = this.geometryFactory.geometry("POINT(1.234 5.678)");
    assertTrue(!p1.equals(p2));
  }

  public void testEquals4() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(1.2334 5.678)");
    final Point p2 = this.geometryFactory.geometry("POINT(1.2333 5.678)");
    assertTrue(p1.equals(p2));
  }

  public void testEquals5() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(1.2334 5.678)");
    final Point p2 = this.geometryFactory.geometry("POINT(1.2335 5.678)");
    assertTrue(!p1.equals(p2));
  }

  public void testEquals6() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(1.2324 5.678)");
    final Point p2 = this.geometryFactory.geometry("POINT(1.2325 5.678)");
    assertTrue(!p1.equals(p2));
  }

  public void testIsSimple() throws Exception {
    final Point p1 = this.geometryFactory.geometry("POINT(1.2324 5.678)");
    assertTrue(p1.isSimple());
    final Point p2 = this.geometryFactory.geometry("POINT EMPTY");
    assertTrue(p2.isSimple());
  }

  public void testNegRounding1() throws Exception {
    final Point pLo = this.geometryFactory.geometry("POINT(-1.233 5.678)");
    final Point pHi = this.geometryFactory.geometry("POINT(-1.232 5.678)");

    final Point p1 = this.geometryFactory.geometry("POINT(-1.2326 5.678)");
    final Point p2 = this.geometryFactory.geometry("POINT(-1.2325 5.678)");
    final Point p3 = this.geometryFactory.geometry("POINT(-1.2324 5.678)");

    assertTrue(!p1.equals(p2));
    assertTrue(p3.equals(p2));

    assertTrue(p1.equals(pLo));
    assertTrue(p2.equals(pHi));
    assertTrue(p3.equals(pHi));
  }

  public void testProjection() {
    final Point albersPoint = this.albers3d.geometry("SRID=3005;POINT Z(1000000 1500000 10)");
    final Point webMercatorPoint = albersPoint.convertGeometry(this.worldMercator);
    final Point albersPoint2 = webMercatorPoint.convertGeometry(this.albers3d);
    if (!albersPoint.equals(2, albersPoint2)) {
      failNotEquals("Not Equal Exact", albersPoint, albersPoint2);
    }
    final Point albersPoint3 = webMercatorPoint.convertGeometry(this.albers2d);
    final Point albersPoint4 = albersPoint3.convertGeometry(this.albers3d);
    // System.out.println(webMercatorPoint);
    // System.out.println(albersPoint);

  }
}
