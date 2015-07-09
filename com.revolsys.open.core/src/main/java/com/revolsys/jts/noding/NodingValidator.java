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

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;

/**
 * Validates that a collection of {@link SegmentString}s is correctly noded.
 * Throws an appropriate exception if an noding error is found.
 *
 * @version 1.7
 */
public class NodingValidator {

  private final LineIntersector li = new RobustLineIntersector();

  private final Collection<NodedSegmentString> segStrings;

  public NodingValidator(final Collection<NodedSegmentString> segStrings) {
    this.segStrings = segStrings;
  }

  private void checkCollapse(final Point p0, final Point p1, final Point p2) {
    if (p0.equals(p2)) {
      throw new RuntimeException(
        "found non-noded collapse at " + GeometryFactory.floating3().lineString(p0, p1, p2));
    }
  }

  /**
   * Checks if a segment string contains a segment pattern a-b-a (which implies a self-intersection)
   */
  private void checkCollapses() {
    for (final NodedSegmentString segment : this.segStrings) {
      checkCollapses(segment);
    }
  }

  private void checkCollapses(final NodedSegmentString ss) {
    final LineString points = ss.getPoints();
    for (int i = 0; i < points.getVertexCount() - 2; i++) {
      checkCollapse(points.getPoint(i), points.getPoint(i + 1), points.getPoint(i + 2));
    }
  }

  /**
   * Checks for intersections between an endpoint of a segment string
   * and an interior vertex of another segment string
   */
  private void checkEndPtVertexIntersections() {
    for (final NodedSegmentString ss : this.segStrings) {
      checkEndPtVertexIntersections(ss.getCoordinate(0), this.segStrings);
      checkEndPtVertexIntersections(ss.getCoordinate(ss.size() - 1), this.segStrings);
    }
  }

  private void checkEndPtVertexIntersections(final Point testPt,
    final Collection<NodedSegmentString> segStrings) {
    for (final NodedSegmentString ss : segStrings) {
      final LineString pts = ss.getPoints();
      for (int j = 1; j < pts.getVertexCount() - 1; j++) {
        if (pts.getPoint(j).equals(testPt)) {
          throw new RuntimeException(
            "found endpt/interior pt intersection at index " + j + " :pt " + testPt);
        }
      }
    }
  }

  /**
   * Checks all pairs of segments for intersections at an interior point of a segment
   */
  private void checkInteriorIntersections() {
    for (final NodedSegmentString ss0 : this.segStrings) {
      for (final NodedSegmentString ss1 : this.segStrings) {
        checkInteriorIntersections(ss0, ss1);
      }
    }
  }

  private void checkInteriorIntersections(final NodedSegmentString e0, final int segIndex0,
    final SegmentString e1, final int segIndex1) {
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }
    // numTests++;
    final Point p00 = e0.getCoordinate(segIndex0);
    final Point p01 = e0.getCoordinate(segIndex0 + 1);
    final Point p10 = e1.getCoordinate(segIndex1);
    final Point p11 = e1.getCoordinate(segIndex1 + 1);

    this.li.computeIntersection(p00, p01, p10, p11);
    if (this.li.hasIntersection()) {

      if (this.li.isProper() || hasInteriorIntersection(this.li, p00, p01)
        || hasInteriorIntersection(this.li, p10, p11)) {
        throw new RuntimeException(
          "found non-noded intersection at " + p00 + "-" + p01 + " and " + p10 + "-" + p11);
      }
    }
  }

  private void checkInteriorIntersections(final NodedSegmentString ss0, final SegmentString ss1) {
    for (int i0 = 0; i0 < ss0.size() - 1; i0++) {
      for (int i1 = 0; i1 < ss1.size() - 1; i1++) {
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
  private boolean hasInteriorIntersection(final LineIntersector li, final Point p0,
    final Point p1) {
    for (int i = 0; i < li.getIntersectionNum(); i++) {
      final Point intPt = li.getIntersection(i);
      if (!(intPt.equals(p0) || intPt.equals(p1))) {
        return true;
      }
    }
    return false;
  }

}
