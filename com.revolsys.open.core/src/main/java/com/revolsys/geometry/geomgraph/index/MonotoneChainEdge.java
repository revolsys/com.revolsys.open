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

import com.revolsys.geometry.geomgraph.Edge;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;

/**
 * MonotoneChains are a way of partitioning the segments of an edge to
 * allow for fast searching of intersections.
 * They have the following properties:
 * <ol>
 * <li>the segments within a monotone chain will never intersect each other
 * <li>the envelope of any contiguous subset of the segments in a monotone chain
 * is simply the envelope of the endpoints of the subset.
 * </ol>
 * Property 1 means that there is no need to test pairs of segments from within
 * the same monotone chain for intersection.
 * Property 2 allows
 * binary search to be used to find the intersection points of two monotone chains.
 * For many types of real-world data, these properties eliminate a large number of
 * segment comparisons, producing substantial speed gains.
 * @version 1.7
 */
public class MonotoneChainEdge {

  private final Edge edge;

  // the lists of start/end indexes of the monotone chains.
  // Includes the end point of the edge as a sentinel
  private final int[] startIndex;

  public MonotoneChainEdge(final Edge edge) {
    this.edge = edge;
    final MonotoneChainIndexer mcb = new MonotoneChainIndexer();
    this.startIndex = mcb.getChainStartIndices(edge);
  }

  public void computeIntersects(final MonotoneChainEdge mce, final SegmentIntersector si) {
    for (int i = 0; i < this.startIndex.length - 1; i++) {
      for (int j = 0; j < mce.startIndex.length - 1; j++) {
        computeIntersectsForChain(i, mce, j, si);
      }
    }
  }

  private void computeIntersectsForChain(final int start0, final int end0,
    final MonotoneChainEdge mce, final int start1, final int end1, final SegmentIntersector ei) {
    final Point p00 = this.edge.getPoint(start0);
    final Point p01 = this.edge.getPoint(end0);
    final Point p10 = mce.edge.getPoint(start1);
    final Point p11 = mce.edge.getPoint(end1);
    // Debug.println("computeIntersectsForChain:" + p00 + p01 + p10 + p11);
    // terminating condition for the recursion
    if (end0 - start0 == 1 && end1 - start1 == 1) {
      ei.addIntersections(this.edge, start0, mce.edge, start1);
      return;
    }
    // nothing to do if the envelopes of these chains don't overlap
    final BoundingBox env1 = BoundingBoxDoubleXY.newBoundingBox(p00, p01);
    final BoundingBox env2 = BoundingBoxDoubleXY.newBoundingBox(p10, p11);
    if (!env1.intersects(env2)) {
      return;
    }

    // the chains overlap, so split each in half and iterate (binary search)
    final int mid0 = (start0 + end0) / 2;
    final int mid1 = (start1 + end1) / 2;

    // Assert: mid != start or end (since we checked above for end - start <= 1)
    // check terminating conditions before recursing
    if (start0 < mid0) {
      if (start1 < mid1) {
        computeIntersectsForChain(start0, mid0, mce, start1, mid1, ei);
      }
      if (mid1 < end1) {
        computeIntersectsForChain(start0, mid0, mce, mid1, end1, ei);
      }
    }
    if (mid0 < end0) {
      if (start1 < mid1) {
        computeIntersectsForChain(mid0, end0, mce, start1, mid1, ei);
      }
      if (mid1 < end1) {
        computeIntersectsForChain(mid0, end0, mce, mid1, end1, ei);
      }
    }
  }

  public void computeIntersectsForChain(final int chainIndex0, final MonotoneChainEdge mce,
    final int chainIndex1, final SegmentIntersector si) {
    computeIntersectsForChain(this.startIndex[chainIndex0], this.startIndex[chainIndex0 + 1], mce,
      mce.startIndex[chainIndex1], mce.startIndex[chainIndex1 + 1], si);
  }

  public double getMaxX(final int chainIndex) {
    final double x1 = this.edge.getPoint(this.startIndex[chainIndex]).getX();
    final double x2 = this.edge.getPoint(this.startIndex[chainIndex + 1]).getX();
    return x1 > x2 ? x1 : x2;
  }

  public double getMinX(final int chainIndex) {
    final double x1 = this.edge.getPoint(this.startIndex[chainIndex]).getX();
    final double x2 = this.edge.getPoint(this.startIndex[chainIndex + 1]).getX();
    return x1 < x2 ? x1 : x2;
  }

  public int[] getStartIndexes() {
    return this.startIndex;
  }
}
