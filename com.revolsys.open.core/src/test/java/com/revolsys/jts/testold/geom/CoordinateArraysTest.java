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

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

/**
 * Unit tests for {@link Envelope}
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
    assertTrue(CoordinateArrays.ptNotInList(
      new Point[] {
        new PointDouble((double)1.0, 1, Point.NULL_ORDINATE),
        new PointDouble((double)2.0, 2, Point.NULL_ORDINATE),
        new PointDouble((double)3.0, 3, Point.NULL_ORDINATE)
      },
      new Point[] {
        new PointDouble((double)1.0, 1, Point.NULL_ORDINATE),
        new PointDouble((double)1.0, 2, Point.NULL_ORDINATE),
        new PointDouble((double)1.0, 3, Point.NULL_ORDINATE)
      }).equals2d(new PointDouble((double)2, 2, Point.NULL_ORDINATE)));
  }

  public void testPtNotInList2() {
    assertTrue(CoordinateArrays.ptNotInList(new Point[] {
      new PointDouble((double)1.0, 1, Point.NULL_ORDINATE),
      new PointDouble((double)2.0, 2, Point.NULL_ORDINATE),
      new PointDouble((double)3.0, 3, Point.NULL_ORDINATE)
    }, new Point[] {
      new PointDouble((double)1.0, 1, Point.NULL_ORDINATE),
      new PointDouble((double)2.0, 2, Point.NULL_ORDINATE),
      new PointDouble((double)3.0, 3, Point.NULL_ORDINATE)
    }) == null);
  }
}
