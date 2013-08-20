package com.revolsys.gis.model.geometry.operation.buffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdge;
import com.revolsys.gis.model.geometry.operation.geomgraph.Position;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Locates a subgraph inside a set of subgraphs,
 * in order to determine the outside depth of the subgraph.
 * The input subgraphs are assumed to have had depths
 * already calculated for their edges.
 *
 * @version 1.7
 */
public class SubgraphDepthLocater {
  /**
   * A segment from a directed edge which has been assigned a depth value
   * for its sides.
   */
  private class DepthSegment implements Comparable {
    private final LineSegment upwardSeg;

    private final int leftDepth;

    public DepthSegment(final LineSegment seg, final int depth) {
      // input seg is assumed to be normalized
      upwardSeg = new LineSegment(seg);
      // upwardSeg.normalize();
      this.leftDepth = depth;
    }

    /**
     * Defines a comparision operation on DepthSegments
     * which orders them left to right
     *
     * <pre>
     * DS1 < DS2   if   DS1.seg is left of DS2.seg
     * DS1 > DS2   if   DS1.seg is right of DS2.seg
     * </pre>
     *
     * @param obj
     * @return
     */
    @Override
    public int compareTo(final Object obj) {
      final DepthSegment other = (DepthSegment)obj;
      /**
       * try and compute a determinate orientation for the segments.
       * Test returns 1 if other is left of this (i.e. this > other)
       */
      int orientIndex = upwardSeg.orientationIndex(other.upwardSeg);

      /**
       * If comparison between this and other is indeterminate,
       * try the opposite call order.
       * orientationIndex value is 1 if this is left of other,
       * so have to flip sign to get proper comparison value of
       * -1 if this is leftmost
       */
      if (orientIndex == 0) {
        orientIndex = -1 * other.upwardSeg.orientationIndex(upwardSeg);
      }

      // if orientation is determinate, return it
      if (orientIndex != 0) {
        return orientIndex;
      }

      // otherwise, segs must be collinear - sort based on minimum X value
      return compareX(this.upwardSeg, other.upwardSeg);
    }

    /**
     * Compare two collinear segments for left-most ordering.
     * If segs are vertical, use vertical ordering for comparison.
     * If segs are equal, return 0.
     * Segments are assumed to be directed so that the second coordinate is >= to the first
     * (e.g. up and to the right).
     *
     * @param seg0 a segment to compare
     * @param seg1 a segment to compare
     * @return
     */
    private int compareX(final LineSegment seg0, final LineSegment seg1) {
      final int compare0 = seg0.getCoordinate(0).compareTo(
        seg1.getCoordinate(0));
      if (compare0 != 0) {
        return compare0;
      }
      return seg0.getCoordinate(1).compareTo(seg1.getCoordinate(1));

    }

  }

  private final Collection subgraphs;

  private final LineSegment seg = new LineSegment();

  private final CGAlgorithms cga = new CGAlgorithms();

  public SubgraphDepthLocater(final List subgraphs) {
    this.subgraphs = subgraphs;
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @return a List of {@link DepthSegments} intersecting the stabbing line
   */
  private List findStabbedSegments(final Coordinates stabbingRayLeftPt) {
    final List stabbedSegments = new ArrayList();
    for (final Iterator i = subgraphs.iterator(); i.hasNext();) {
      final BufferSubgraph bsg = (BufferSubgraph)i.next();

      // optimization - don't bother checking subgraphs which the ray does not
      // intersect
      final Envelope env = bsg.getEnvelope();
      if (stabbingRayLeftPt.getY() < env.getMinY()
        || stabbingRayLeftPt.getY() > env.getMaxY()) {
        continue;
      }

      findStabbedSegments(stabbingRayLeftPt, bsg.getDirectedEdges(),
        stabbedSegments);
    }
    return stabbedSegments;
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line
   * in the input dirEdge.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @param stabbedSegments the current list of {@link DepthSegments} intersecting the stabbing line
   */
  private void findStabbedSegments(final Coordinates stabbingRayLeftPt,
    final DirectedEdge dirEdge, final List stabbedSegments) {
    final CoordinatesList pts = dirEdge.getEdge().getCoordinates();
    for (int i = 0; i < pts.size() - 1; i++) {
      seg.setPoint(0, pts.get(i));
      seg.setPoint(1, pts.get(i + 1));
      // ensure segment always points upwards
      if (seg.get(0).getY() > seg.get(1).getY()) {
        seg.reverse();
      }

      // skip segment if it is left of the stabbing line
      final double maxx = Math.max(seg.get(0).getX(), seg.get(1).getX());
      if (maxx < stabbingRayLeftPt.getX()) {
        continue;
      }

      // skip horizontal segments (there will be a non-horizontal one carrying
      // the same depth info
      if (seg.isHorizontal()) {
        continue;
      }

      // skip if segment is above or below stabbing line
      if (stabbingRayLeftPt.getY() < seg.get(0).getY()
        || stabbingRayLeftPt.getY() > seg.get(1).getY()) {
        continue;
      }

      // skip if stabbing ray is right of the segment
      if (CoordinatesUtil.orientationIndex(seg.get(0), seg.get(1),
        stabbingRayLeftPt) == CGAlgorithms.RIGHT) {
        continue;
      }

      // stabbing line cuts this segment, so record it
      int depth = dirEdge.getDepth(Position.LEFT);
      // if segment direction was flipped, use RHS depth instead
      if (!seg.get(0).equals(pts.get(i))) {
        depth = dirEdge.getDepth(Position.RIGHT);
      }
      final DepthSegment ds = new DepthSegment(seg, depth);
      stabbedSegments.add(ds);
    }
  }

  /**
   * Finds all non-horizontal segments intersecting the stabbing line
   * in the list of dirEdges.
   * The stabbing line is the ray to the right of stabbingRayLeftPt.
   *
   * @param stabbingRayLeftPt the left-hand origin of the stabbing line
   * @param stabbedSegments the current list of {@link DepthSegments} intersecting the stabbing line
   */
  private void findStabbedSegments(final Coordinates stabbingRayLeftPt,
    final List dirEdges, final List stabbedSegments) {
    /**
     * Check all forward DirectedEdges only.  This is still general,
     * because each Edge has a forward DirectedEdge.
     */
    for (final Iterator i = dirEdges.iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      if (!de.isForward()) {
        continue;
      }
      findStabbedSegments(stabbingRayLeftPt, de, stabbedSegments);
    }
  }

  public int getDepth(final Coordinates p) {
    final List stabbedSegments = findStabbedSegments(p);
    // if no segments on stabbing line subgraph must be outside all others.
    if (stabbedSegments.size() == 0) {
      return 0;
    }
    Collections.sort(stabbedSegments);
    final DepthSegment ds = (DepthSegment)stabbedSegments.get(0);
    return ds.leftDepth;
  }
}
