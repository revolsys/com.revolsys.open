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
import com.revolsys.jts.geom.Coordinates;

/**
 * Finds <b>interior</b> intersections between line segments in {@link NodedSegmentString}s,
 * and adds them as nodes
 * using {@link NodedSegmentString#addIntersection(LineIntersector, int, int, int)}.
 * <p>
 * This class is used primarily for Snap-Rounding.  
 * For general-purpose noding, use {@link IntersectionAdder}.
 *
 * @version 1.7
 * @see IntersectionAdder
 * @deprecated see InteriorIntersectionFinderAdder
 */
@Deprecated
public class IntersectionFinderAdder implements SegmentIntersector {
  private final LineIntersector li;

  private final List interiorIntersections;

  /**
   * Creates an intersection finder which finds all proper intersections
   *
   * @param li the LineIntersector to use
   */
  public IntersectionFinderAdder(final LineIntersector li) {
    this.li = li;
    interiorIntersections = new ArrayList();
  }

  public List getInteriorIntersections() {
    return interiorIntersections;
  }

  /**
   * Always process all intersections
   * 
   * @return false always
   */
  @Override
  public boolean isDone() {
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
    // don't bother intersecting a segment with itself
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }

    final Coordinates p00 = e0.getCoordinates()[segIndex0];
    final Coordinates p01 = e0.getCoordinates()[segIndex0 + 1];
    final Coordinates p10 = e1.getCoordinates()[segIndex1];
    final Coordinates p11 = e1.getCoordinates()[segIndex1 + 1];

    li.computeIntersection(p00, p01, p10, p11);
    // if (li.hasIntersection() && li.isProper()) Debug.println(li);

    if (li.hasIntersection()) {
      if (li.isInteriorIntersection()) {
        for (int intIndex = 0; intIndex < li.getIntersectionNum(); intIndex++) {
          interiorIntersections.add(li.getIntersection(intIndex));
        }
        ((NodedSegmentString)e0).addIntersections(li, segIndex0, 0);
        ((NodedSegmentString)e1).addIntersections(li, segIndex1, 1);
      }
    }
  }

}
