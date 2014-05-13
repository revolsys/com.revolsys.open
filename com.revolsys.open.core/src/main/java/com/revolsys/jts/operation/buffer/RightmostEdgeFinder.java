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
package com.revolsys.jts.operation.buffer;

/**
 * @version 1.7
 */
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geomgraph.DirectedEdge;
import com.revolsys.jts.geomgraph.DirectedEdgeStar;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.Node;
import com.revolsys.jts.geomgraph.Position;
import com.revolsys.jts.util.Assert;

/**
 * A RightmostEdgeFinder find the DirectedEdge in a list which has the highest coordinate,
 * and which is oriented L to R at that point. (I.e. the right side is on the RHS of the edge.)
 *
 * @version 1.7
 */
class RightmostEdgeFinder {

  // private Point extremeCoord;
  private int minIndex = -1;

  private Point minCoord = null;

  private DirectedEdge minDe = null;

  private DirectedEdge orientedDe = null;

  /**
   * A RightmostEdgeFinder finds the DirectedEdge with the rightmost coordinate.
   * The DirectedEdge returned is guaranteed to have the R of the world on its RHS.
   */
  public RightmostEdgeFinder() {
  }

  private void checkForRightmostCoordinate(final DirectedEdge de) {
    final Edge edge = de.getEdge();
    for (int i = 0; i < edge.getNumPoints() - 1; i++) {
      // only check vertices which are the start or end point of a
      // non-horizontal segment
      // <FIX> MD 19 Sep 03 - NO! we can test all vertices, since the rightmost
      // must have a non-horiz segment adjacent to it
      final Point point = edge.getCoordinate(i);
      if (minCoord == null || point.getX() > minCoord.getX()) {
        minDe = de;
        minIndex = i;
        minCoord = point;
      }
      // }
    }
  }

  public void findEdge(final List<DirectedEdge> dirEdgeList) {
    /**
     * Check all forward DirectedEdges only.  This is still general,
     * because each edge has a forward DirectedEdge.
     */
    for (final DirectedEdge de : dirEdgeList) {
      if (de.isForward()) {
        checkForRightmostCoordinate(de);
      }
    }

    /**
     * If the rightmost point is a node, we need to identify which of
     * the incident edges is rightmost.
     */
    Assert.isTrue(minIndex != 0 || minCoord.equals(minDe.getCoordinate()),
      "inconsistency in rightmost processing");
    if (minIndex == 0) {
      findRightmostEdgeAtNode();
    } else {
      findRightmostEdgeAtVertex();
    }
    /**
     * now check that the extreme side is the R side.
     * If not, use the sym instead.
     */
    orientedDe = minDe;
    final int rightmostSide = getRightmostSide(minDe, minIndex);
    if (rightmostSide == Position.LEFT) {
      orientedDe = minDe.getSym();
    }
  }

  private void findRightmostEdgeAtNode() {
    final Node node = minDe.getNode();
    final DirectedEdgeStar star = (DirectedEdgeStar)node.getEdges();
    minDe = star.getRightmostEdge();
    // the DirectedEdge returned by the previous call is not
    // necessarily in the forward direction. Use the sym edge if it isn't.
    if (!minDe.isForward()) {
      minDe = minDe.getSym();
      minIndex = minDe.getEdge().getNumPoints() - 1;
    }
  }

  private void findRightmostEdgeAtVertex() {
    /**
     * The rightmost point is an interior vertex, so it has a segment on either side of it.
     * If these segments are both above or below the rightmost point, we need to
     * determine their relative orientation to decide which is rightmost.
     */
    final Edge edge = minDe.getEdge();
    Assert.isTrue(minIndex > 0 && minIndex < edge.getNumPoints(),
      "rightmost point expected to be interior vertex of edge");
    final Point pPrev = edge.getCoordinate(minIndex - 1);
    final Point pNext = edge.getCoordinate(minIndex + 1);
    final int orientation = CGAlgorithms.computeOrientation(minCoord, pNext,
      pPrev);
    boolean usePrev = false;
    // both segments are below min point
    if (pPrev.getY() < minCoord.getY() && pNext.getY() < minCoord.getY()
      && orientation == CGAlgorithms.COUNTERCLOCKWISE) {
      usePrev = true;
    } else if (pPrev.getY() > minCoord.getY() && pNext.getY() > minCoord.getY()
      && orientation == CGAlgorithms.CLOCKWISE) {
      usePrev = true;
    }
    // if both segments are on the same side, do nothing - either is safe
    // to select as a rightmost segment
    if (usePrev) {
      minIndex = minIndex - 1;
    }
  }

  public Point getCoordinate() {
    return minCoord;
  }

  public DirectedEdge getEdge() {
    return orientedDe;
  }

  private int getRightmostSide(final DirectedEdge de, final int index) {
    int side = getRightmostSideOfSegment(de, index);
    if (side < 0) {
      side = getRightmostSideOfSegment(de, index - 1);
    }
    if (side < 0) {
      // reaching here can indicate that segment is horizontal
      // Assert.shouldNeverReachHere("problem with finding rightmost side of segment at "
      // + de.getCoordinate());
      // testing only
      minCoord = null;
      checkForRightmostCoordinate(de);
    }
    return side;
  }

  private int getRightmostSideOfSegment(final DirectedEdge de, final int i) {
    final Edge edge = de.getEdge();
    if (i < 0 || i + 1 >= edge.getNumPoints()) {
      return -1;
    }
    final Point p1 = edge.getCoordinate(i);
    final Point p2 = edge.getCoordinate(i + 1);
    if (p1.getY() == p2.getY()) {
      return -1; // indicates edge is parallel to x-axis
    }

    int pos = Position.LEFT;
    if (p1.getY() < p2.getY()) {
      pos = Position.RIGHT;
    }
    return pos;
  }
}
