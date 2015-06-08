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

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.geom.Point;

/**
 * Finds an interior intersection in a set of {@link SegmentString}s,
 * if one exists.  Only the first intersection found is reported.
 *
 * @version 1.7
 */
public class InteriorIntersectionFinder implements SegmentIntersector {
  private boolean findAllIntersections = false;

  private boolean isCheckEndSegmentsOnly = false;

  private final LineIntersector li;

  private Point interiorIntersection = null;

  private Point[] intSegments = null;

  private final List intersections = new ArrayList();

  /**
   * Creates an intersection finder which finds an interior intersection
   * if one exists
   *
   * @param li the LineIntersector to use
   */
  public InteriorIntersectionFinder(final LineIntersector li) {
    this.li = li;
    this.interiorIntersection = null;
  }

  /**
   * Gets the computed location of the intersection.
   * Due to round-off, the location may not be exact.
   *
   * @return the coordinate for the intersection location
   */
  public Point getInteriorIntersection() {
    return this.interiorIntersection;
  }

  public List getIntersections() {
    return this.intersections;
  }

  /**
   * Gets the endpoints of the intersecting segments.
   *
   * @return an array of the segment endpoints (p00, p01, p10, p11)
   */
  public Point[] getIntersectionSegments() {
    return this.intSegments;
  }

  /**
   * Tests whether an intersection was found.
   *
   * @return true if an intersection was found
   */
  public boolean hasIntersection() {
    return this.interiorIntersection != null;
  }

  @Override
  public boolean isDone() {
    if (this.findAllIntersections) {
      return false;
    }
    return this.interiorIntersection != null;
  }

  /**
   * Tests whether a segment in a {@link SegmentString} is an end segment.
   * (either the first or last).
   *
   * @param segStr a segment string
   * @param index the index of a segment in the segment string
   * @return true if the segment is an end segment
   */
  private boolean isEndSegment(final SegmentString segStr, final int index) {
    if (index == 0) {
      return true;
    }
    if (index >= segStr.size() - 2) {
      return true;
    }
    return false;
  }

  /**
   * This method is called by clients
   * of the {@link SegmentIntersector} class to process
   * intersections for two segments of the {@link SegmentString}s being intersected.
   * Note that some clients (such as <code>MonotoneChain</code>s) may optimize away
   * this call for segment pairs which they have determined do not intersect
   * (e.g. by an disjoint envelope test).
   */
  @Override
  public void processIntersections(final SegmentString e0, final int segIndex0,
    final SegmentString e1, final int segIndex1) {
    // short-circuit if intersection already found
    if (hasIntersection()) {
      return;
    }

    // don't bother intersecting a segment with itself
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }

    /**
     * If enabled, only test end segments (on either segString).
     *
     */
    if (this.isCheckEndSegmentsOnly) {
      final boolean isEndSegPresent = isEndSegment(e0, segIndex0) || isEndSegment(e1, segIndex1);
      if (!isEndSegPresent) {
        return;
      }
    }

    final Point p00 = e0.getCoordinate(segIndex0);
    final Point p01 = e0.getCoordinate(segIndex0 + 1);
    final Point p10 = e1.getCoordinate(segIndex1);
    final Point p11 = e1.getCoordinate(segIndex1 + 1);

    this.li.computeIntersection(p00, p01, p10, p11);

    if (this.li.hasIntersection()) {
      if (this.li.isInteriorIntersection()) {
        this.intSegments = new Point[4];
        this.intSegments[0] = p00;
        this.intSegments[1] = p01;
        this.intSegments[2] = p10;
        this.intSegments[3] = p11;

        this.interiorIntersection = this.li.getIntersection(0);
        this.intersections.add(this.interiorIntersection);
      }
    }
  }

  /**
   * Sets whether only end segments should be tested for interior intersection.
   * This is a performance optimization that may be used if
   * the segments have been previously noded by an appropriate algorithm.
   * It may be known that any potential noding failures will occur only in
   * end segments.
   *
   * @param isCheckEndSegmentsOnly whether to test only end segments
   */
  public void setCheckEndSegmentsOnly(final boolean isCheckEndSegmentsOnly) {
    this.isCheckEndSegmentsOnly = isCheckEndSegmentsOnly;
  }

  public void setFindAllIntersections(final boolean findAllIntersections) {
    this.findAllIntersections = findAllIntersections;
  }
}
