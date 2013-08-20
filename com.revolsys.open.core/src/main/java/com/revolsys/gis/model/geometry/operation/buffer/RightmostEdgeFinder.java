package com.revolsys.gis.model.geometry.operation.buffer;

import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdge;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdgeStar;
import com.revolsys.gis.model.geometry.operation.geomgraph.Edge;
import com.revolsys.gis.model.geometry.operation.geomgraph.Node;
import com.revolsys.gis.model.geometry.operation.geomgraph.Position;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.util.Assert;

/**
 * @version 1.7
 */

/**
 * A RightmostEdgeFinder find the DirectedEdge in a list which has the highest
 * coordinate, and which is oriented L to R at that point. (I.e. the right side
 * is on the RHS of the edge.)
 * 
 * @version 1.7
 */
public class RightmostEdgeFinder {

  // private Coordinate extremeCoord;
  private int minIndex = -1;

  private Coordinates minCoord = null;

  private DirectedEdge minDe = null;

  private DirectedEdge orientedDe = null;

  /**
   * A RightmostEdgeFinder finds the DirectedEdge with the rightmost coordinate.
   * The DirectedEdge returned is guaranteed to have the R of the world on its
   * RHS.
   */
  public RightmostEdgeFinder() {
  }

  private void checkForRightmostCoordinate(final DirectedEdge de) {
    final CoordinatesList coord = de.getEdge().getCoordinates();
    for (int i = 0; i < coord.size() - 1; i++) {
      // only check vertices which are the start or end point of a
      // non-horizontal segment
      // <FIX> MD 19 Sep 03 - NO! we can test all vertices, since the rightmost
      // must have a non-horiz segment adjacent to it
      if (minCoord == null || coord.getX(i) > minCoord.getX()) {
        minDe = de;
        minIndex = i;
        minCoord = coord.get(i);
      }
      // }
    }
  }

  public void findEdge(final List dirEdgeList) {
    /**
     * Check all forward DirectedEdges only. This is still general, because each
     * edge has a forward DirectedEdge.
     */
    for (final Iterator i = dirEdgeList.iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      if (!de.isForward()) {
        continue;
      }
      checkForRightmostCoordinate(de);
    }

    /**
     * If the rightmost point is a node, we need to identify which of the
     * incident edges is rightmost.
     */
    Assert.isTrue(minIndex != 0 || minCoord.equals(minDe.getCoordinate()),
      "inconsistency in rightmost processing");
    if (minIndex == 0) {
      findRightmostEdgeAtNode();
    } else {
      findRightmostEdgeAtVertex();
    }
    /**
     * now check that the extreme side is the R side. If not, use the sym
     * instead.
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
      minIndex = minDe.getEdge().getCoordinates().size() - 1;
    }
  }

  private void findRightmostEdgeAtVertex() {
    /**
     * The rightmost point is an interior vertex, so it has a segment on either
     * side of it. If these segments are both above or below the rightmost
     * point, we need to determine their relative orientation to decide which is
     * rightmost.
     */
    final CoordinatesList pts = minDe.getEdge().getCoordinates();
    Assert.isTrue(minIndex > 0 && minIndex < pts.size(),
      "rightmost point expected to be interior vertex of edge");
    final Coordinates pPrev = pts.get(minIndex - 1);
    final Coordinates pNext = pts.get(minIndex + 1);
    final int orientation = CoordinatesUtil.orientationIndex(minCoord, pNext,
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

  public Coordinates getCoordinate() {
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
    final Edge e = de.getEdge();
    final CoordinatesList coord = e.getCoordinates();

    if (i < 0 || i + 1 >= coord.size()) {
      return -1;
    }
    if (coord.getY(i) == coord.getY(i + 1)) {
      return -1; // indicates edge is parallel to x-axis
    }

    int pos = Position.LEFT;
    if (coord.getY(i) < coord.getY(i + 1)) {
      pos = Position.RIGHT;
    }
    return pos;
  }
}
