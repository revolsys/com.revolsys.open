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

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.CoordinateArrays;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXY;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Unit tests for {@link BoundingBox}
 *
 * @author Martin Davis
 * @version 1.7
 */
public class CoordinateArraysTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(CoordinateArraysTest.class);
  }

  public CoordinateArraysTest(final String name) {
    super(name);
  }

  public void testPtNotInList1() {
    assertTrue(CoordinateArrays.ptNotInList(new Point[] {
      new PointDoubleXY(1.0, 1),
      new PointDoubleXY(2.0, 2),
      new PointDoubleXY(3.0, 3)
    }, new Point[] {
      new PointDoubleXY(1.0, 1),
      new PointDoubleXY(1.0, 2),
      new PointDoubleXY(1.0, 3)
    }).equals(2, new PointDoubleXY((double)2, 2)));
  }

  public void testPtNotInList2() {
    assertTrue(CoordinateArrays.ptNotInList(new Point[] {
      new PointDoubleXY(1.0, 1),
      new PointDoubleXY(2.0, 2),
      new PointDoubleXY(3.0, 3)
    }, new Point[] {
      new PointDoubleXY(1.0, 1),
      new PointDoubleXY(2.0, 2),
      new PointDoubleXY(3.0, 3)
    }) == null);
  }
}
