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
package com.revolsys.jts.noding;

import java.util.Collection;
import java.util.Iterator;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.util.Debug;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Throws an appropriate exception if an noding error is found.
 *
 * @version 1.7
 */
public class NodingValidator {

  private final LineIntersector li = new RobustLineIntersector();

  private final Collection segStrings;

  public NodingValidator(final Collection segStrings) {
    this.segStrings = segStrings;
  }

  private void checkCollapse(final Coordinates p0, final Coordinates p1,
    final Coordinates p2) {
    if (p0.equals(p2)) {
      throw new RuntimeException("found non-noded collapse at "
        + Debug.toLine(p0, p1, p2));
    }
  }

  /**
   * Checks if a segment string contains a segment pattern a-b-a (which implies a self-intersection)
   */
  private void checkCollapses() {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      checkCollapses(ss);
    }
  }

  private void checkCollapses(final SegmentString ss) {
    final Coordinates[] pts = ss.getCoordinates();
    for (int i = 0; i < pts.length - 2; i++) {
      checkCollapse(pts[i], pts[i + 1], pts[i + 2]);
    }
  }

  /**
   * Checks for intersections between an endpoint of a segment string
   * and an interior vertex of another segment string
   */
  private void checkEndPtVertexIntersections() {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      final Coordinates[] pts = ss.getCoordinates();
      checkEndPtVertexIntersections(pts[0], segStrings);
      checkEndPtVertexIntersections(pts[pts.length - 1], segStrings);
    }
  }

  private void checkEndPtVertexIntersections(final Coordinates testPt,
    final Collection segStrings) {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss = (SegmentString)i.next();
      final Coordinates[] pts = ss.getCoordinates();
      for (int j = 1; j < pts.length - 1; j++) {
        if (pts[j].equals(testPt)) {
          throw new RuntimeException(
            "found endpt/interior pt intersection at index " + j + " :pt "
              + testPt);
        }
      }
    }
  }

  /**
   * Checks all pairs of segments for intersections at an interior point of a segment
   */
  private void checkInteriorIntersections() {
    for (final Iterator i = segStrings.iterator(); i.hasNext();) {
      final SegmentString ss0 = (SegmentString)i.next();
      for (final Iterator j = segStrings.iterator(); j.hasNext();) {
        final SegmentString ss1 = (SegmentString)j.next();

        checkInteriorIntersections(ss0, ss1);
      }
    }
  }

  private void checkInteriorIntersections(final SegmentString e0,
    final int segIndex0, final SegmentString e1, final int segIndex1) {
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }
    // numTests++;
    final Coordinates p00 = e0.getCoordinates()[segIndex0];
    final Coordinates p01 = e0.getCoordinates()[segIndex0 + 1];
    final Coordinates p10 = e1.getCoordinates()[segIndex1];
    final Coordinates p11 = e1.getCoordinates()[segIndex1 + 1];

    li.computeIntersection(p00, p01, p10, p11);
    if (li.hasIntersection()) {

      if (li.isProper() || hasInteriorIntersection(li, p00, p01)
        || hasInteriorIntersection(li, p10, p11)) {
        throw new RuntimeException("found non-noded intersection at " + p00
          + "-" + p01 + " and " + p10 + "-" + p11);
      }
    }
  }

  private void checkInteriorIntersections(final SegmentString ss0,
    final SegmentString ss1) {
    final Coordinates[] pts0 = ss0.getCoordinates();
    final Coordinates[] pts1 = ss1.getCoordinates();
    for (int i0 = 0; i0 < pts0.length - 1; i0++) {
      for (int i1 = 0; i1 < pts1.length - 1; i1++) {
        checkInteriorIntersections(ss0, i0, ss1, i1);
      }
    }
  }

  public void checkValid() {
    // MD - is this call required? Or could it be done in the Interior
    // Intersection code?
    checkEndPtVertexIntersections();
    checkInteriorIntersections();
    checkCollapses();
  }

  /**
   *@return true if there is an intersection point which is not an endpoint of the segment p0-p1
   */
  private boolean hasInteriorIntersection(final LineIntersector li,
    final Coordinates p0, final Coordinates p1) {
    for (int i = 0; i < li.getIntersectionNum(); i++) {
      final Coordinates intPt = li.getIntersection(i);
      if (!(intPt.equals(p0) || intPt.equals(p1))) {
        return true;
      }
    }
    return false;
  }

}
