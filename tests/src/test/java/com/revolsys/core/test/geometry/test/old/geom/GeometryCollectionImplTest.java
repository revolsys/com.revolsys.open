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

package com.revolsys.core.test.geometry.test.old.geom;

import com.revolsys.geometry.model.Dimension;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;

import junit.framework.TestCase;

/**
 * Test for com.revolsys.core.test.geometry.testold.geom.GeometryCollectionImpl.
 *
 * @version 1.7
 */
public class GeometryCollectionImplTest extends TestCase {

  private final GeometryFactory geometryFactory = GeometryFactory.fixed2d(0, 1000.0, 1000.0);

  public GeometryCollectionImplTest(final String name) {
    super(name);
  }

  public void testGetDimension() throws Exception {
    final Geometry g = this.geometryFactory
      .geometry("GEOMETRYCOLLECTION (POINT (10 10), POINT (30 30), LINESTRING (15 15, 20 20))");
    assertEquals(Dimension.L, g.getDimension());
  }

  public void testGetLength() throws Exception {
    final Geometry g = GeometryFactory.DEFAULT_3D.geometry("MULTIPOLYGON("
      + "((0 0, 10 0, 10 10, 0 10, 0 0), (3 3, 3 7, 7 7, 7 3, 3 3)),"
      + "((100 100, 110 100, 110 110, 100 110, 100 100), (103 103, 103 107, 107 107, 107 103, 103 103)))");
    assertEquals(112, g.getLength(), 1E-15);
  }

}
