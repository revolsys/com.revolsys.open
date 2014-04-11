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

import com.revolsys.jts.algorithm.CGAlgorithmsDD;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

/**
 * Tests failure cases of CGAlgorithms.computeOrientation
 * @version 1.7
 */
public class OrientationIndexFailureTest extends TestCase {
  public static boolean isAllOrientationsEqual(final double p0x,
    final double p0y, final double p1x, final double p1y, final double p2x,
    final double p2y) {
    final Coordinates[] pts = {
      new Coordinate((double)p0x, p0y, Coordinates.NULL_ORDINATE), new Coordinate((double)p1x, p1y, Coordinates.NULL_ORDINATE),
      new Coordinate((double)p2x, p2y, Coordinates.NULL_ORDINATE)
    };
    if (!isAllOrientationsEqualDD(pts)) {
      throw new IllegalStateException(
        "High-precision orientation computation FAILED");
    }
    return OrientationIndexTest.isAllOrientationsEqual(pts);
  }

  public static boolean isAllOrientationsEqualDD(final Coordinates[] pts) {
    final int orient0 = CGAlgorithmsDD.orientationIndex(pts[0], pts[1], pts[2]);
    final int orient1 = CGAlgorithmsDD.orientationIndex(pts[1], pts[2], pts[0]);
    final int orient2 = CGAlgorithmsDD.orientationIndex(pts[2], pts[0], pts[1]);
    return orient0 == orient1 && orient0 == orient2;
  }

  public static boolean isAllOrientationsEqualSD(final Coordinates[] pts) {
    final int orient0 = ShewchuksDeterminant.orientationIndex(pts[0], pts[1],
      pts[2]);
    final int orient1 = ShewchuksDeterminant.orientationIndex(pts[1], pts[2],
      pts[0]);
    final int orient2 = ShewchuksDeterminant.orientationIndex(pts[2], pts[0],
      pts[1]);
    return orient0 == orient1 && orient0 == orient2;
  }

  public static void main(final String args[]) {
    TestRunner.run(OrientationIndexFailureTest.class);
  }

  public OrientationIndexFailureTest(final String name) {
    super(name);
  }

  private void checkDD(final Coordinates[] pts, final boolean expected) {
    assertTrue("DD", expected == isAllOrientationsEqualDD(pts));
  }

  /**
   * Shorthand method for most common case,
   * where the high-precision methods work but JTS Robust algorithm fails.
   * @param pts
   */
  void checkOrientation(final Coordinates[] pts) {
    // this should succeed
    checkDD(pts, true);
    checkShewchuk(pts, true);

    // this is expected to fail
    checkOriginalJTS(pts, false);
  }

  private void checkOriginalJTS(final Coordinates[] pts, final boolean expected) {
    assertTrue("JTS Robust FAIL",
      expected == OrientationIndexTest.isAllOrientationsEqual(pts));
  }

  private void checkShewchuk(final Coordinates[] pts, final boolean expected) {
    assertTrue("Shewchuk", expected == isAllOrientationsEqualSD(pts));
  }

  public void testBadCCW() throws Exception {
    // this case fails because subtraction of small from large loses precision
    final Coordinates[] pts = {
      new Coordinate((double)1.4540766091864998, -7.989685402102996, Coordinates.NULL_ORDINATE),
      new Coordinate((double)23.131039116367354, -7.004368924503866, Coordinates.NULL_ORDINATE),
      new Coordinate((double)1.4540766091865, -7.989685402102996, Coordinates.NULL_ORDINATE),
    };
    checkOrientation(pts);
  }

  public void testBadCCW2() throws Exception {
    // this case fails because subtraction of small from large loses precision
    final Coordinates[] pts = {
      new Coordinate((double)219.3649559090992, 140.84159161824724, Coordinates.NULL_ORDINATE),
      new Coordinate((double)168.9018919682399, -5.713787599646864, Coordinates.NULL_ORDINATE),
      new Coordinate((double)186.80814046338352, 46.28973405831556, Coordinates.NULL_ORDINATE),
    };
    checkOrientation(pts);
  }

  public void testBadCCW3() throws Exception {
    // this case fails because subtraction of small from large loses precision
    final Coordinates[] pts = {
      new Coordinate((double)279.56857838488514, -186.3790522565901, Coordinates.NULL_ORDINATE),
      new Coordinate((double)-20.43142161511487, 13.620947743409914, Coordinates.NULL_ORDINATE),
      new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE)
    };
    checkOrientation(pts);
  }

  public void testBadCCW4() throws Exception {
    // from JTS list - 5/15/2012 strange case for the GeometryNoder
    final Coordinates[] pts = {
      new Coordinate((double)-26.2, 188.7, Coordinates.NULL_ORDINATE), new Coordinate((double)37.0, 290.7, Coordinates.NULL_ORDINATE),
      new Coordinate((double)21.2, 265.2, Coordinates.NULL_ORDINATE)
    };
    checkOrientation(pts);
  }

  public void testBadCCW5() throws Exception {
    // from JTS list - 6/15/2012 another case from Tomas Fa
    final Coordinates[] pts = {
      new Coordinate((double)-5.9, 163.1, Coordinates.NULL_ORDINATE), new Coordinate((double)76.1, 250.7, Coordinates.NULL_ORDINATE),
      new Coordinate((double)14.6, 185, Coordinates.NULL_ORDINATE)
    // new Coordinate((double)96.6, 272.6)
    };
    checkOrientation(pts);
  }

  public void testBadCCW6() throws Exception {
    // from JTS Convex Hull "Almost collinear" unit test
    final Coordinates[] pts = {
      new Coordinate((double)-140.8859438214298, 140.88594382142983, Coordinates.NULL_ORDINATE),
      new Coordinate((double)-57.309236848216706, 57.30923684821671, Coordinates.NULL_ORDINATE),
      new Coordinate((double)-190.9188309203678, 190.91883092036784, Coordinates.NULL_ORDINATE)
    };
    checkOrientation(pts);
  }

  public void testBadCCW7() throws Exception {
    // from JTS list - 6/26/2012 another case from Tomas Fa
    final Coordinates[] pts = {
      new Coordinate((double)-0.9575, 0.4511, Coordinates.NULL_ORDINATE), new Coordinate((double)-0.9295, 0.3291, Coordinates.NULL_ORDINATE),
      new Coordinate((double)-0.8945, 0.1766, Coordinates.NULL_ORDINATE)
    };
    checkDD(pts, true);
    checkShewchuk(pts, false);
    checkOriginalJTS(pts, false);
  }

  public void testBadCCW7_2() throws Exception {
    // from JTS list - 6/26/2012 another case from Tomas Fa
    // scale to integers - all methods work on this
    final Coordinates[] pts = {
      new Coordinate((double)-9575, 4511, Coordinates.NULL_ORDINATE), new Coordinate((double)-9295, 3291, Coordinates.NULL_ORDINATE),
      new Coordinate((double)-8945, 1766, Coordinates.NULL_ORDINATE)
    };
    checkDD(pts, true);
    checkShewchuk(pts, true);
    checkOriginalJTS(pts, true);
  }

  /**
   * This is included to confirm this test is operating correctly
   * @throws Exception
   */
  public void testSanity() throws Exception {
    assertTrue(OrientationIndexTest.isAllOrientationsEqual(OrientationIndexTest.getCoordinates("LINESTRING ( 0 0, 0 1, 1 1)")));
  }

}
