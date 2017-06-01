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
package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.segment.Segment;

/**
 * Specifies and implements various fundamental Computational Geometric
 * algorithms. The algorithms supplied in this class are robust for
 * double-precision floating point.
 *
 * @version 1.7
 */
public class CGAlgorithms {

  /**
   * A value that indicates an orientation of clockwise, or a right turn.
   */
  public static final int CLOCKWISE = -1;

  /**
   * A value that indicates an orientation of collinear, or no turn (straight).
   */
  public static final int COLLINEAR = 0;

  /**
   * A value that indicates an orientation of counterclockwise, or a left turn.
   */
  public static final int COUNTERCLOCKWISE = 1;

  /**
   * A value that indicates an orientation of counterclockwise, or a left turn.
   */
  public static final int LEFT = COUNTERCLOCKWISE;

  /**
   * A value that indicates an orientation of clockwise, or a right turn.
   */
  public static final int RIGHT = CLOCKWISE;

  /**
   * A value that indicates an orientation of collinear, or no turn (straight).
   */
  public static final int STRAIGHT = COLLINEAR;

  /**
   * Computes the perpendicular distance from a point p to the (infinite) line
   * containing the points AB
   *
   * @param p
   *          the point to compute the distance for
   * @param A
   *          one point of the line
   * @param B
   *          another point of the line (must be different to A)
   * @return the distance from p to line AB
   */
  public static double distancePointLinePerpendicular(final Point p, final Point A, final Point B) {
    // use comp.graphics.algorithms Frequently Asked Questions method
    /*
     * (2) s = (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay) ----------------------------- L^2 Then the distance
     * from C to P = |s|*L.
     */
    final double len2 = (B.getX() - A.getX()) * (B.getX() - A.getX())
      + (B.getY() - A.getY()) * (B.getY() - A.getY());
    final double s = ((A.getY() - p.getY()) * (B.getX() - A.getX())
      - (A.getX() - p.getX()) * (B.getY() - A.getY())) / len2;

    return Math.abs(s) * Math.sqrt(len2);
  }

  /**
   * Computes whether a ring defined by an array of {@link Coordinates}s is
   * oriented counter-clockwise.
   * <ul>
   * <li>The list of points is assumed to have the first and last points equal.
   * <li>This will handle coordinate lists which contain repeated points.
   * </ul>
   * This algorithm is <b>only</b> guaranteed to work with valid rings. If the
   * ring is invalid (e.g. self-crosses or touches), the computed result may not
   * be correct.
   *
   * @param ring
   *          an array of Point forming a ring
   * @return true if the ring is oriented counter-clockwise.
   * @throws IllegalArgumentException
   *           if there are too few points to determine orientation (< 4)
   */
  public static boolean isCCW(final Point[] ring) {
    // # of points without closing endpoint
    final int nPts = ring.length - 1;
    // sanity check
    if (nPts < 3) {
      throw new IllegalArgumentException(
        "Ring has fewer than 4 points, so orientation cannot be determined");
    }

    // find highest point
    Point hiPt = ring[0];
    int hiIndex = 0;
    for (int i = 1; i <= nPts; i++) {
      final Point p = ring[i];
      if (p.getY() > hiPt.getY()) {
        hiPt = p;
        hiIndex = i;
      }
    }

    // find distinct point before highest point
    int iPrev = hiIndex;
    do {
      iPrev = iPrev - 1;
      if (iPrev < 0) {
        iPrev = nPts;
      }
    } while (ring[iPrev].equals(2, hiPt) && iPrev != hiIndex);

    // find distinct point after highest point
    int iNext = hiIndex;
    do {
      iNext = (iNext + 1) % nPts;
    } while (ring[iNext].equals(2, hiPt) && iNext != hiIndex);

    final Point prev = ring[iPrev];
    final Point next = ring[iNext];

    /**
     * This check catches cases where the ring contains an A-B-A configuration
     * of points. This can happen if the ring does not contain 3 distinct points
     * (including the case where the input array has fewer than 4 elements), or
     * it contains coincident line segments.
     */
    if (prev.equals(2, hiPt) || next.equals(2, hiPt) || prev.equals(2, next)) {
      return false;
    }

    final int disc = CGAlgorithmsDD.orientationIndex(prev, hiPt, next);

    /**
     * If disc is exactly 0, lines are collinear. There are two possible cases:
     * (1) the lines lie along the x axis in opposite directions (2) the lines
     * lie on top of one another
     *
     * (1) is handled by checking if next is left of prev ==> CCW (2) will never
     * happen if the ring is valid, so don't check for it (Might want to assert
     * this)
     */
    boolean counterClockwise = false;
    if (disc == 0) {
      // poly is CCW if prev x is right of next x
      counterClockwise = prev.getX() > next.getX();
    } else {
      // if area is positive, points are ordered CCW
      counterClockwise = disc > 0;
    }
    return counterClockwise;
  }

  /**
   * Tests whether a point lies on the line segments defined by a list of
   * coordinates.
   *
   * @return true if the point is a vertex of the line or lies in the interior
   *         of a line segment in the linestring
   */
  public static boolean isOnLine(final Point p, final LineString line) {
    final LineIntersector lineIntersector = new RobustLineIntersector();
    for (final Segment segment : line.segments()) {
      final Point p0 = segment.getPoint(0);
      final Point p1 = segment.getPoint(1);
      lineIntersector.computeIntersection(p, p0, p1);
      if (lineIntersector.hasIntersection()) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPointInRing(final Point point, final LineString ring) {
    return RayCrossingCounter.locatePointInRing(point, ring) != Location.EXTERIOR;
  }

  public CGAlgorithms() {
  }

}
