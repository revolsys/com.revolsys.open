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
package com.revolsys.geometry.geomgraph.index;

import java.util.Collection;
import java.util.Iterator;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.geomgraph.Node;
import com.revolsys.geometry.model.Point;

/**
 * Computes the intersection of line segments,
 * and adds the intersection to the edges containing the segments.
 *
 * @version 1.7
 */
public class SegmentIntersector {

  public static boolean isAdjacentSegments(final int i1, final int i2) {
    return Math.abs(i1 - i2) == 1;
  }

  /**
   * These variables keep track of what types of intersections were
   * found during ALL edges that have been intersected.
   */
  private boolean hasIntersection = false;

  private boolean hasProper = false;

  private boolean hasProperInterior = false;

  // the proper intersection point found
  private Point properIntersectionPoint = null;

  private final LineIntersector li;

  private final boolean includeProper;

  private final boolean recordIsolated;

  // testing only
  public int numTests = 0;

  private Collection[] bdyNodes;

  /*
   * public SegmentIntersector() { }
   */
  public SegmentIntersector(final LineIntersector li, final boolean includeProper,
    final boolean recordIsolated) {
    this.li = li;
    this.includeProper = includeProper;
    this.recordIsolated = recordIsolated;
  }

  /**
   * This method is called by clients of the EdgeIntersector class to test for and add
   * intersections for two segments of the edges being intersected.
   * Note that clients (such as MonotoneChainEdges) may choose not to intersect
   * certain pairs of segments for efficiency reasons.
   */
  public void addIntersections(final Edge e0, final int segIndex0, final Edge e1,
    final int segIndex1) {
    if (e0 == e1 && segIndex0 == segIndex1) {
      return;
    }
    this.numTests++;
    final Point p00 = e0.getCoordinate(segIndex0);
    final Point p01 = e0.getCoordinate(segIndex0 + 1);
    final Point p10 = e1.getCoordinate(segIndex1);
    final Point p11 = e1.getCoordinate(segIndex1 + 1);

    this.li.computeIntersection(p00, p01, p10, p11);
    // if (li.hasIntersection() && li.isProper()) Debug.println(li);
    /**
     *  Always record any non-proper intersections.
     *  If includeProper is true, record any proper intersections as well.
     */
    if (this.li.hasIntersection()) {
      if (this.recordIsolated) {
        e0.setIsolated(false);
        e1.setIsolated(false);
      }
      // if the segments are adjacent they have at least one trivial
      // intersection,
      // the shared endpoint. Don't bother adding it if it is the
      // only intersection.
      if (!isTrivialIntersection(e0, segIndex0, e1, segIndex1)) {
        this.hasIntersection = true;
        if (this.includeProper || !this.li.isProper()) {
          // Debug.println(li);
          e0.addIntersections(this.li, segIndex0, 0);
          e1.addIntersections(this.li, segIndex1, 1);
        }
        if (this.li.isProper()) {
          this.properIntersectionPoint = this.li.getIntersection(0).clone();
          this.hasProper = true;
          if (!isBoundaryPoint(this.li, this.bdyNodes)) {
            this.hasProperInterior = true;
          }
        }
        // if (li.isCollinear())
        // hasCollinear = true;
      }
    }
  }

  /**
   * @return the proper intersection point, or <code>null</code> if none was found
   */
  public Point getProperIntersectionPoint() {
    return this.properIntersectionPoint;
  }

  public boolean hasIntersection() {
    return this.hasIntersection;
  }

  /**
   * A proper interior intersection is a proper intersection which is <b>not</b>
   * contained in the set of boundary nodes set for this SegmentIntersector.
   */
  public boolean hasProperInteriorIntersection() {
    return this.hasProperInterior;
  }

  /**
   * A proper intersection is an intersection which is interior to at least two
   * line segments.  Note that a proper intersection is not necessarily
   * in the interior of the entire Geometry, since another edge may have
   * an endpoint equal to the intersection, which according to SFS semantics
   * can result in the point being on the Boundary of the Geometry.
   */
  public boolean hasProperIntersection() {
    return this.hasProper;
  }

  private boolean isBoundaryPoint(final LineIntersector li, final Collection bdyNodes) {
    for (final Iterator i = bdyNodes.iterator(); i.hasNext();) {
      final Node node = (Node)i.next();
      final Point pt = node.getCoordinate();
      if (li.isIntersection(pt)) {
        return true;
      }
    }
    return false;
  }

  private boolean isBoundaryPoint(final LineIntersector li, final Collection[] bdyNodes) {
    if (bdyNodes == null) {
      return false;
    }
    if (isBoundaryPoint(li, bdyNodes[0])) {
      return true;
    }
    if (isBoundaryPoint(li, bdyNodes[1])) {
      return true;
    }
    return false;
  }

  /**
   * A trivial intersection is an apparent self-intersection which in fact
   * is simply the point shared by adjacent line segments.
   * Note that closed edges require a special check for the point shared by the beginning
   * and end segments.
   */
  private boolean isTrivialIntersection(final Edge e0, final int segIndex0, final Edge e1,
    final int segIndex1) {
    if (e0 == e1) {
      if (this.li.getIntersectionNum() == 1) {
        if (isAdjacentSegments(segIndex0, segIndex1)) {
          return true;
        }
        if (e0.isClosed()) {
          final int maxSegIndex = e0.getNumPoints() - 1;
          if (segIndex0 == 0 && segIndex1 == maxSegIndex
            || segIndex1 == 0 && segIndex0 == maxSegIndex) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public void setBoundaryNodes(final Collection bdyNodes0, final Collection bdyNodes1) {
    this.bdyNodes = new Collection[2];
    this.bdyNodes[0] = bdyNodes0;
    this.bdyNodes[1] = bdyNodes1;
  }

}
