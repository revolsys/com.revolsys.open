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
package com.revolsys.gis.model.geometry.operation.buffer;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.HCoordinate;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.NotRepresentableException;
import com.vividsolutions.jts.geomgraph.Position;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * Generates segments which form an offset curve. Supports all end cap and join
 * options provided for buffering. Implements various heuristics to produce
 * smoother, simpler curves which are still within a reasonable tolerance of the
 * true curve.
 * 
 * @author Martin Davis
 */
class OffsetSegmentGenerator {

  /**
   * Factor which controls how close offset segments can be to skip adding a
   * filler or mitre.
   */
  private static final double OFFSET_SEGMENT_SEPARATION_FACTOR = 1.0E-3;

  /**
   * Factor which controls how close curve vertices on inside turns can be to be
   * snapped
   */
  private static final double INSIDE_TURN_VERTEX_SNAP_DISTANCE_FACTOR = 1.0E-3;

  /**
   * Factor which controls how close curve vertices can be to be snapped
   */
  private static final double CURVE_VERTEX_SNAP_DISTANCE_FACTOR = 1.0E-6;

  /**
   * Factor which determines how short closing segs can be for round buffers
   */
  private static final int MAX_CLOSING_SEG_LEN_FACTOR = 80;

  /**
   * The angle quantum with which to approximate a fillet curve (based on the
   * input # of quadrant segments)
   */
  private final double filletAngleQuantum;

  /**
   * The Closing Segment Length Factor controls how long "closing segments" are.
   * Closing segments are added at the middle of inside corners to ensure a
   * smoother boundary for the buffer offset curve. In some cases (particularly
   * for round joins with default-or-better quantization) the closing segments
   * can be made quite short. This substantially improves performance (due to
   * fewer intersections being created). A closingSegFactor of 0 results in
   * lines to the corner vertex A closingSegFactor of 1 results in lines halfway
   * to the corner vertex A closingSegFactor of 80 results in lines 1/81 of the
   * way to the corner vertex (this option is reasonable for the very common
   * default situation of round joins and quadrantSegs >= 8)
   */
  private int closingSegLengthFactor = 1;

  private OffsetSegmentString segList;

  private double distance = 0.0;

  private final CoordinatesPrecisionModel precisionModel;

  private final BufferParameters bufParams;

  private final LineIntersector li;

  private Coordinates s0, s1, s2;

  private final LineSegment seg0 = new LineSegment();

  private final LineSegment seg1 = new LineSegment();

  private final LineSegment offset0 = new LineSegment();

  private final LineSegment offset1 = new LineSegment();

  private int side = 0;

  private boolean hasNarrowConcaveAngle = false;

  public OffsetSegmentGenerator(final CoordinatesPrecisionModel precisionModel,
    final BufferParameters bufParams, final double distance) {
    this.precisionModel = precisionModel;
    this.bufParams = bufParams;

    // compute intersections in full precision, to provide accuracy
    // the points are rounded as they are inserted into the curve line
    li = new RobustLineIntersector();
    filletAngleQuantum = Math.PI / 2.0 / bufParams.getQuadrantSegments();

    /**
     * Non-round joins cause issues with short closing segments, so don't use
     * them. In any case, non-round joins only really make sense for relatively
     * small buffer distances.
     */
    if (bufParams.getQuadrantSegments() >= 8
      && bufParams.getJoinStyle() == BufferParameters.JOIN_ROUND) {
      closingSegLengthFactor = MAX_CLOSING_SEG_LEN_FACTOR;
    }
    init(distance);
  }

  /**
   * Adds a bevel join connecting the two offset segments around a reflex
   * corner.
   * 
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   */
  private void addBevelJoin(final LineSegment offset0, final LineSegment offset1) {
    segList.addPt(offset0.get(1));
    segList.addPt(offset1.get(0));
  }

  private void addCollinear(final boolean addStartPoint) {
    /**
     * This test could probably be done more efficiently, but the situation of
     * exact collinearity should be fairly rare.
     */
    li.computeIntersection(s0, s1, s1, s2);
    final int numInt = li.getIntersectionNum();
    /**
     * if numInt is < 2, the lines are parallel and in the same direction. In
     * this case the point can be ignored, since the offset lines will also be
     * parallel.
     */
    if (numInt >= 2) {
      /**
       * segments are collinear but reversing. Add an "end-cap" fillet all the
       * way around to other direction This case should ONLY happen for
       * LineStrings, so the orientation is always CW. (Polygons can never have
       * two consecutive segments which are parallel but reversed, because that
       * would be a self intersection.
       */
      if (bufParams.getJoinStyle() == BufferParameters.JOIN_BEVEL
        || bufParams.getJoinStyle() == BufferParameters.JOIN_MITRE) {
        if (addStartPoint) {
          segList.addPt(offset0.get(1));
        }
        segList.addPt(offset1.get(0));
      } else {
        addFillet(s1, offset0.get(1), offset1.get(0), CGAlgorithms.CLOCKWISE,
          distance);
      }
    }
  }

  /**
   * Add points for a circular fillet around a reflex corner. Adds the start and
   * end points
   * 
   * @param p base point of curve
   * @param p0 start point of fillet curve
   * @param p1 endpoint of fillet curve
   * @param direction the orientation of the fillet
   * @param radius the radius of the fillet
   */
  private void addFillet(final Coordinates p, final Coordinates p0,
    final Coordinates p1, final int direction, final double radius) {
    final double dx0 = p0.getX() - p.getX();
    final double dy0 = p0.getY() - p.getY();
    double startAngle = Math.atan2(dy0, dx0);
    final double dx1 = p1.getX() - p.getX();
    final double dy1 = p1.getY() - p.getY();
    final double endAngle = Math.atan2(dy1, dx1);

    if (direction == CGAlgorithms.CLOCKWISE) {
      if (startAngle <= endAngle) {
        startAngle += 2.0 * Math.PI;
      }
    } else { // direction == COUNTERCLOCKWISE
      if (startAngle >= endAngle) {
        startAngle -= 2.0 * Math.PI;
      }
    }
    segList.addPt(p0);
    addFillet(p, startAngle, endAngle, direction, radius);
    segList.addPt(p1);
  }

  /**
   * Adds points for a circular fillet arc between two specified angles. The
   * start and end point for the fillet are not added - the caller must add them
   * if required.
   * 
   * @param direction is -1 for a CW angle, 1 for a CCW angle
   * @param radius the radius of the fillet
   */
  private void addFillet(final Coordinates p, final double startAngle,
    final double endAngle, final int direction, final double radius) {
    final int directionFactor = direction == CGAlgorithms.CLOCKWISE ? -1 : 1;

    final double totalAngle = Math.abs(startAngle - endAngle);
    final int nSegs = (int)(totalAngle / filletAngleQuantum + 0.5);

    if (nSegs < 1) {
      return; // no segments because angle is less than increment - nothing to
              // do!
    }

    double initAngle, currAngleInc;

    // choose angle increment so that each segment has equal length
    initAngle = 0.0;
    currAngleInc = totalAngle / nSegs;

    double currAngle = initAngle;
    final Coordinates pt = new DoubleCoordinates();
    while (currAngle < totalAngle) {
      final double angle = startAngle + directionFactor * currAngle;
      pt.setX(p.getX() + radius * Math.cos(angle));
      pt.setY(p.getY() + radius * Math.sin(angle));
      segList.addPt(pt);
      currAngle += currAngleInc;
    }
  }

  public void addFirstSegment() {
    segList.addPt(offset1.get(0));
  }

  /**
   * Adds the offset points for an inside (concave) turn.
   * 
   * @param orientation
   * @param addStartPoint
   */
  private void addInsideTurn(final int orientation, final boolean addStartPoint) {
    /**
     * add intersection point of offset segments (if any)
     */
    li.computeIntersection(offset0.get(0), offset0.get(1), offset1.get(0),
      offset1.get(1));
    if (li.hasIntersection()) {
      segList.addPt(li.getIntersection(0));
    } else {
      /**
       * If no intersection is detected, it means the angle is so small and/or
       * the offset so large that the offsets segments don't intersect. In this
       * case we must add a "closing segment" to make sure the buffer curve is
       * continuous, fairly smooth (e.g. no sharp reversals in direction) and
       * tracks the buffer correctly around the corner. The curve connects the
       * endpoints of the segment offsets to points which lie toward the centre
       * point of the corner. The joining curve will not appear in the final
       * buffer outline, since it is completely internal to the buffer polygon.
       * In complex buffer cases the closing segment may cut across many other
       * segments in the generated offset curve. In order to improve the
       * performance of the noding, the closing segment should be kept as short
       * as possible. (But not too short, since that would defeat its purpose).
       * This is the purpose of the closingSegFactor heuristic value.
       */

      /**
       * The intersection test above is vulnerable to robustness errors; i.e. it
       * may be that the offsets should intersect very close to their endpoints,
       * but aren't reported as such due to rounding. To handle this situation
       * appropriately, we use the following test: If the offset points are very
       * close, don't add closing segments but simply use one of the offset
       * points
       */
      hasNarrowConcaveAngle = true;
      // System.out.println("NARROW ANGLE - distance = " + distance);
      if (offset0.get(1).distance(offset1.get(0)) < distance
        * INSIDE_TURN_VERTEX_SNAP_DISTANCE_FACTOR) {
        segList.addPt(offset0.get(1));
      } else {
        // add endpoint of this segment offset
        segList.addPt(offset0.get(1));

        /**
         * Add "closing segment" of required length.
         */
        if (closingSegLengthFactor > 0) {
          final Coordinates mid0 = new DoubleCoordinates(
            (closingSegLengthFactor * offset0.get(1).getX() + s1.getX())
              / (closingSegLengthFactor + 1), (closingSegLengthFactor
              * offset0.get(1).getY() + s1.getY())
              / (closingSegLengthFactor + 1));
          segList.addPt(mid0);
          final Coordinates mid1 = new DoubleCoordinates(
            (closingSegLengthFactor * offset1.get(0).getX() + s1.getX())
              / (closingSegLengthFactor + 1), (closingSegLengthFactor
              * offset1.get(0).getY() + s1.getY())
              / (closingSegLengthFactor + 1));
          segList.addPt(mid1);
        } else {
          /**
           * This branch is not expected to be used except for testing purposes.
           * It is equivalent to the JTS 1.9 logic for closing segments (which
           * results in very poor performance for large buffer distances)
           */
          segList.addPt(s1);
        }

        // */
        // add start point of next segment offset
        segList.addPt(offset1.get(0));
      }
    }
  }

  /**
   * Add last offset point
   */
  public void addLastSegment() {
    segList.addPt(offset1.get(1));
  }

  /**
   * Adds a limited mitre join connecting the two reflex offset segments. A
   * limited mitre is a mitre which is beveled at the distance determined by the
   * mitre ratio limit.
   * 
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   * @param distance the offset distance
   * @param mitreLimit the mitre limit ratio
   */
  private void addLimitedMitreJoin(final LineSegment offset0,
    final LineSegment offset1, final double distance, final double mitreLimit) {
    final Coordinates basePt = seg0.get(1);

    final double ang0 = basePt.angle2d(seg0.get(0));

    // oriented angle between segments
    final double angDiff = CoordinatesUtil.angleBetweenOriented(seg0.get(0),
      basePt, seg1.get(1));
    // half of the interior angle
    final double angDiffHalf = angDiff / 2;

    // angle for bisector of the interior angle between the segments
    final double midAng = Angle.normalize(ang0 + angDiffHalf);
    // rotating this by PI gives the bisector of the reflex angle
    final double mitreMidAng = Angle.normalize(midAng + Math.PI);

    // the miterLimit determines the distance to the mitre bevel
    final double mitreDist = mitreLimit * distance;
    // the bevel delta is the difference between the buffer distance
    // and half of the length of the bevel segment
    final double bevelDelta = mitreDist * Math.abs(Math.sin(angDiffHalf));
    final double bevelHalfLen = distance - bevelDelta;

    // compute the midpoint of the bevel segment
    final double bevelMidX = basePt.getX() + mitreDist * Math.cos(mitreMidAng);
    final double bevelMidY = basePt.getY() + mitreDist * Math.sin(mitreMidAng);
    final Coordinates bevelMidPt = new DoubleCoordinates(bevelMidX, bevelMidY);

    // compute the mitre midline segment from the corner point to the bevel
    // segment midpoint
    final LineSegment mitreMidLine = new LineSegment(basePt, bevelMidPt);

    // finally the bevel segment endpoints are computed as offsets from
    // the mitre midline
    final Coordinates bevelEndLeft = mitreMidLine.pointAlongOffset(1.0,
      bevelHalfLen);
    final Coordinates bevelEndRight = mitreMidLine.pointAlongOffset(1.0,
      -bevelHalfLen);

    if (side == Position.LEFT) {
      segList.addPt(bevelEndLeft);
      segList.addPt(bevelEndRight);
    } else {
      segList.addPt(bevelEndRight);
      segList.addPt(bevelEndLeft);
    }
  }

  // private static double MAX_CLOSING_SEG_LEN = 3.0;

  /**
   * Add an end cap around point p1, terminating a line segment coming from p0
   */
  public void addLineEndCap(final Coordinates p0, final Coordinates p1) {
    final LineSegment seg = new LineSegment(p0, p1);

    final LineSegment offsetL = new LineSegment();
    computeOffsetSegment(seg, Position.LEFT, distance, offsetL);
    final LineSegment offsetR = new LineSegment();
    computeOffsetSegment(seg, Position.RIGHT, distance, offsetR);

    final double dx = p1.getX() - p0.getX();
    final double dy = p1.getY() - p0.getY();
    final double angle = Math.atan2(dy, dx);

    switch (bufParams.getEndCapStyle()) {
      case BufferParameters.CAP_ROUND:
        // add offset seg points with a fillet between them
        segList.addPt(offsetL.get(1));
        addFillet(p1, angle + Math.PI / 2, angle - Math.PI / 2,
          CGAlgorithms.CLOCKWISE, distance);
        segList.addPt(offsetR.get(1));
      break;
      case BufferParameters.CAP_FLAT:
        // only offset segment points are added
        segList.addPt(offsetL.get(1));
        segList.addPt(offsetR.get(1));
      break;
      case BufferParameters.CAP_SQUARE:
        // add a square defined by extensions of the offset segment endpoints
        final Coordinates squareCapSideOffset = new DoubleCoordinates();
        squareCapSideOffset.setX(Math.abs(distance) * Math.cos(angle));
        squareCapSideOffset.setY(Math.abs(distance) * Math.sin(angle));

        final Coordinates squareCapLOffset = new DoubleCoordinates(offsetL.get(
          1).getX()
          + squareCapSideOffset.getX(), offsetL.get(1).getY()
          + squareCapSideOffset.getY());
        final Coordinates squareCapROffset = new DoubleCoordinates(offsetR.get(
          1).getX()
          + squareCapSideOffset.getX(), offsetR.get(1).getY()
          + squareCapSideOffset.getY());
        segList.addPt(squareCapLOffset);
        segList.addPt(squareCapROffset);
      break;

    }
  }

  /**
   * Adds a mitre join connecting the two reflex offset segments. The mitre will
   * be beveled if it exceeds the mitre ratio limit.
   * 
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   * @param distance the offset distance
   */
  private void addMitreJoin(final Coordinates p, final LineSegment offset0,
    final LineSegment offset1, final double distance) {
    boolean isMitreWithinLimit = true;
    Coordinates intPt = null;

    /**
     * This computation is unstable if the offset segments are nearly collinear.
     * Howver, this situation should have been eliminated earlier by the check
     * for whether the offset segment endpoints are almost coincident
     */
    try {
      intPt = HCoordinate.intersection(offset0.get(0), offset0.get(1),
        offset1.get(0), offset1.get(1));

      final double mitreRatio = distance <= 0.0 ? 1.0 : intPt.distance(p)
        / Math.abs(distance);

      if (mitreRatio > bufParams.getMitreLimit()) {
        isMitreWithinLimit = false;
      }
    } catch (final NotRepresentableException ex) {
      intPt = new DoubleCoordinates(0.0, 0.0);
      isMitreWithinLimit = false;
    }

    if (isMitreWithinLimit) {
      segList.addPt(intPt);
    } else {
      addLimitedMitreJoin(offset0, offset1, distance, bufParams.getMitreLimit());
      // addBevelJoin(offset0, offset1);
    }
  }

  public void addNextSegment(final Coordinates p, final boolean addStartPoint) {
    // s0-s1-s2 are the coordinates of the previous segment and the current one
    s0 = s1;
    s1 = s2;
    s2 = p;
    seg0.setCoordinates(s0, s1);
    computeOffsetSegment(seg0, side, distance, offset0);
    seg1.setCoordinates(s1, s2);
    computeOffsetSegment(seg1, side, distance, offset1);

    // do nothing if points are equal
    if (s1.equals(s2)) {
      return;
    }

    final int orientation = CoordinatesUtil.orientationIndex(s0, s1, s2);
    final boolean outsideTurn = (orientation == CGAlgorithms.CLOCKWISE && side == Position.LEFT)
      || (orientation == CGAlgorithms.COUNTERCLOCKWISE && side == Position.RIGHT);

    if (orientation == 0) { // lines are collinear
      addCollinear(addStartPoint);
    } else if (outsideTurn) {
      addOutsideTurn(orientation, addStartPoint);
    } else { // inside turn
      addInsideTurn(orientation, addStartPoint);
    }
  }

  /**
   * Adds the offset points for an outside (convex) turn
   * 
   * @param orientation
   * @param addStartPoint
   */
  private void addOutsideTurn(final int orientation, final boolean addStartPoint) {
    /**
     * Heuristic: If offset endpoints are very close together, just use one of
     * them as the corner vertex. This avoids problems with computing mitre
     * corners in the case where the two segments are almost parallel (which is
     * hard to compute a robust intersection for).
     */
    if (offset0.get(1).distance(offset1.get(0)) < distance
      * OFFSET_SEGMENT_SEPARATION_FACTOR) {
      segList.addPt(offset0.get(1));
      return;
    }

    if (bufParams.getJoinStyle() == BufferParameters.JOIN_MITRE) {
      addMitreJoin(s1, offset0, offset1, distance);
    } else if (bufParams.getJoinStyle() == BufferParameters.JOIN_BEVEL) {
      addBevelJoin(offset0, offset1);
    } else {
      // add a circular fillet connecting the endpoints of the offset segments
      if (addStartPoint) {
        segList.addPt(offset0.get(1));
      }
      // TESTING - comment out to produce beveled joins
      addFillet(s1, offset0.get(1), offset1.get(0), orientation, distance);
      segList.addPt(offset1.get(0));
    }
  }

  public void addSegments(final CoordinatesList pt, final boolean isForward) {
    segList.addPts(pt, isForward);
  }

  public void closeRing() {
    segList.closeRing();
  }

  /**
   * Compute an offset segment for an input segment on a given side and at a
   * given distance. The offset points are computed in full double precision,
   * for accuracy.
   * 
   * @param seg the segment to offset
   * @param side the side of the segment ({@link Position}) the offset lies on
   * @param distance the offset distance
   * @param offset the points computed for the offset segment
   */
  private void computeOffsetSegment(final LineSegment seg, final int side,
    final double distance, final LineSegment offset) {
    final int sideSign = side == Position.LEFT ? 1 : -1;
    final double dx = seg.get(1).getX() - seg.get(0).getX();
    final double dy = seg.get(1).getY() - seg.get(0).getY();
    final double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    final double ux = sideSign * distance * dx / len;
    final double uy = sideSign * distance * dy / len;
    offset.setX(0, seg.get(0).getX() - uy);
    offset.setY(0, seg.get(0).getY() + ux);
    offset.setX(1, seg.get(1).getX() - uy);
    offset.setY(1, seg.get(1).getY() + ux);
  }

  /**
   * Creates a CW circle around a point
   */
  public void createCircle(final Coordinates p) {
    // add start point
    final Coordinates pt = new DoubleCoordinates(p.getX() + distance, p.getY());
    segList.addPt(pt);
    addFillet(p, 0.0, 2.0 * Math.PI, -1, distance);
    segList.closeRing();
  }

  /**
   * Creates a CW square around a point
   */
  public void createSquare(final Coordinates p) {
    segList.addPt(new DoubleCoordinates(p.getX() + distance, p.getY()
      + distance));
    segList.addPt(new DoubleCoordinates(p.getX() + distance, p.getY()
      - distance));
    segList.addPt(new DoubleCoordinates(p.getX() - distance, p.getY()
      - distance));
    segList.addPt(new DoubleCoordinates(p.getX() - distance, p.getY()
      + distance));
    segList.closeRing();
  }

  public CoordinatesList getCoordinates() {
    return segList.getCoordinates();
  }

  /**
   * Tests whether the input has a narrow concave angle (relative to the offset
   * distance). In this case the generated offset curve will contain
   * self-intersections and heuristic closing segments. This is expected
   * behaviour in the case of buffer curves. For pure offset curves, the output
   * needs to be further treated before it can be used.
   * 
   * @return true if the input has a narrow concave angle
   */
  public boolean hasNarrowConcaveAngle() {
    return hasNarrowConcaveAngle;
  }

  private void init(final double distance) {
    this.distance = distance;
    segList = new OffsetSegmentString();
    segList.setPrecisionModel(precisionModel);
    /**
     * Choose the min vertex separation as a small fraction of the offset
     * distance.
     */
    segList.setMinimumVertexDistance(distance
      * CURVE_VERTEX_SNAP_DISTANCE_FACTOR);
  }

  public void initSideSegments(final Coordinates s1, final Coordinates s2,
    final int side) {
    this.s1 = s1;
    this.s2 = s2;
    this.side = side;
    seg1.setCoordinates(s1, s2);
    computeOffsetSegment(seg1, side, distance, offset1);
  }
}
