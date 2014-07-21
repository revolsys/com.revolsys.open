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

import com.revolsys.gis.util.Debug;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.CGAlgorithmsDD;
import com.revolsys.jts.algorithm.HCoordinate;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.NotRepresentableException;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDouble;
import com.revolsys.jts.geomgraph.Position;
import com.revolsys.math.Angle;

/**
 * Generates segments which form an offset curve.
 * Supports all end cap and join options 
 * provided for buffering.
 * This algorithm implements various heuristics to 
 * produce smoother, simpler curves which are
 * still within a reasonable tolerance of the 
 * true curve.
 * 
 * @author Martin Davis
 *
 */
class OffsetSegmentGenerator {

  /**
   * Factor which controls how close offset segments can be to
   * skip adding a filler or mitre.
   */
  private static final double OFFSET_SEGMENT_SEPARATION_FACTOR = 1.0E-3;

  /**
   * Factor which controls how close curve vertices on inside turns can be to be snapped 
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
   * The angle quantum with which to approximate a fillet curve
   * (based on the input # of quadrant segments)
   */
  private final double filletAngleQuantum;

  /**
   * The Closing Segment Length Factor controls how long
   * "closing segments" are.  Closing segments are added
   * at the middle of inside corners to ensure a smoother
   * boundary for the buffer offset curve. 
   * In some cases (particularly for round joins with default-or-better
   * quantization) the closing segments can be made quite short.
   * This substantially improves performance (due to fewer intersections being created).
   * 
   * A closingSegFactor of 0 results in lines to the corner vertex
   * A closingSegFactor of 1 results in lines halfway to the corner vertex
   * A closingSegFactor of 80 results in lines 1/81 of the way to the corner vertex
   * (this option is reasonable for the very common default situation of round joins
   * and quadrantSegs >= 8)
   */
  private int closingSegLengthFactor = 1;

  private OffsetSegmentString segList;

  private double distance = 0.0;

  private final GeometryFactory precisionModel;

  private final BufferParameters bufParams;

  private final LineIntersector li;

  private Point s0;

  private Point s1;

  private Point s2;

  private LineSegment offset0;

  private LineSegment offset1;

  private int side = 0;

  private boolean hasNarrowConcaveAngle = false;

  public OffsetSegmentGenerator(final GeometryFactory precisionModel,
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
   * Adds a bevel join connecting the two offset segments
   * around a reflex corner.
   * 
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   */
  private void addBevelJoin(final LineSegment offset0, final LineSegment offset1) {
    segList.addPt(offset0.getP1());
    segList.addPt(offset1.getP0());
  }

  private void addCollinear(final boolean addStartPoint) {
    /**
     * This test could probably be done more efficiently,
     * but the situation of exact collinearity should be fairly rare.
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
       * segments are collinear but reversing. 
       * Add an "end-cap" fillet
       * all the way around to other direction This case should ONLY happen
       * for LineStrings, so the orientation is always CW. (Polygons can never
       * have two consecutive segments which are parallel but reversed,
       * because that would be a self intersection.
       * 
       */
      if (bufParams.getJoinStyle() == BufferParameters.JOIN_BEVEL
        || bufParams.getJoinStyle() == BufferParameters.JOIN_MITRE) {
        if (addStartPoint) {
          segList.addPt(offset0.getP1());
        }
        segList.addPt(offset1.getP0());
      } else {
        addFillet(s1, offset0.getP1(), offset1.getP0(), CGAlgorithms.CLOCKWISE,
          distance);
      }
    }
  }

  /**
   * Adds points for a circular fillet arc
   * between two specified angles.  
   * The start and end point for the fillet are not added -
   * the caller must add them if required.
   *
   * @param direction is -1 for a CW angle, 1 for a CCW angle
   * @param radius the radius of the fillet
   */
  private void addFillet(final Point p, final double startAngle,
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
    while (currAngle < totalAngle) {
      final double angle = startAngle + directionFactor * currAngle;
      final double x = p.getX() + radius * Math.cos(angle);
      final double y = p.getY() + radius * Math.sin(angle);
      segList.addPt(x, y);
      currAngle += currAngleInc;
    }
  }

  /**
   * Add points for a circular fillet around a reflex corner.
   * Adds the start and end points
   * 
   * @param p base point of curve
   * @param p0 start point of fillet curve
   * @param p1 endpoint of fillet curve
   * @param direction the orientation of the fillet
   * @param radius the radius of the fillet
   */
  private void addFillet(final Point p, final Point p0, final Point p1,
    final int direction, final double radius) {
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

  public void addFirstSegment() {
    segList.addPt(offset1.getP0());
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
    li.computeIntersection(offset0.getP0(), offset0.getP1(), offset1.getP0(),
      offset1.getP1());
    if (li.hasIntersection()) {
      segList.addPt(li.getIntersection(0));
    } else {
      /**
       * If no intersection is detected, 
       * it means the angle is so small and/or the offset so
       * large that the offsets segments don't intersect. 
       * In this case we must
       * add a "closing segment" to make sure the buffer curve is continuous,
       * fairly smooth (e.g. no sharp reversals in direction)
       * and tracks the buffer correctly around the corner. The curve connects
       * the endpoints of the segment offsets to points
       * which lie toward the centre point of the corner.
       * The joining curve will not appear in the final buffer outline, since it
       * is completely internal to the buffer polygon.
       * 
       * In complex buffer cases the closing segment may cut across many other
       * segments in the generated offset curve.  In order to improve the 
       * performance of the noding, the closing segment should be kept as short as possible.
       * (But not too short, since that would defeat its purpose).
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
      if (offset0.getP1().distance(offset1.getP0()) < distance
        * INSIDE_TURN_VERTEX_SNAP_DISTANCE_FACTOR) {
        segList.addPt(offset0.getP1());
      } else {
        // add endpoint of this segment offset
        segList.addPt(offset0.getP1());

        /**
         * Add "closing segment" of required length.
         */
        if (closingSegLengthFactor > 0) {
          final Point mid0 = new PointDouble((closingSegLengthFactor
            * offset0.getP1().getX() + s1.getX())
            / (closingSegLengthFactor + 1), (closingSegLengthFactor
            * offset0.getP1().getY() + s1.getY())
            / (closingSegLengthFactor + 1), Point.NULL_ORDINATE);
          segList.addPt(mid0);
          final Point mid1 = new PointDouble((closingSegLengthFactor
            * offset1.getP0().getX() + s1.getX())
            / (closingSegLengthFactor + 1), (closingSegLengthFactor
            * offset1.getP0().getY() + s1.getY())
            / (closingSegLengthFactor + 1), Point.NULL_ORDINATE);
          segList.addPt(mid1);
        } else {
          /**
           * This branch is not expected to be used except for testing purposes.
           * It is equivalent to the JTS 1.9 logic for closing segments
           * (which results in very poor performance for large buffer distances)
           */
          segList.addPt(s1);
        }

        // */
        // add start point of next segment offset
        segList.addPt(offset1.getP0());
      }
    }
  }

  /**
   * Add last offset point
   */
  public void addLastSegment() {
    segList.addPt(offset1.getP1());
  }

  /**
   * Adds a limited mitre join connecting the two reflex offset segments.
   * A limited mitre is a mitre which is beveled at the distance
   * determined by the mitre ratio limit.
   * 
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   * @param distance the offset distance
   * @param mitreLimit the mitre limit ratio
   */
  private void addLimitedMitreJoin(final LineSegment offset0,
    final LineSegment offset1, final double distance, final double mitreLimit) {
    final Point basePt = s1;

    final double ang0 = basePt.angle2d(s0);

    // oriented angle between segments
    final double angDiff = Angle.angleBetweenOriented(s0, basePt, s2);
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
    final Point bevelMidPt = new PointDouble(bevelMidX, bevelMidY,
      Point.NULL_ORDINATE);

    // compute the mitre midline segment from the corner point to the bevel
    // segment midpoint
    final LineSegment mitreMidLine = new LineSegmentDouble(basePt, bevelMidPt);

    // finally the bevel segment endpoints are computed as offsets from
    // the mitre midline
    final Point bevelEndLeft = mitreMidLine.pointAlongOffset(1.0, bevelHalfLen);
    final Point bevelEndRight = mitreMidLine.pointAlongOffset(1.0,
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
  public void addLineEndCap(final Point p0, final Point p1) {
    final LineSegment seg = new LineSegmentDouble(p0, p1);

    final LineSegment offsetL = createOffsetSegment(seg, Position.LEFT,
      distance);
    final LineSegment offsetR = createOffsetSegment(seg, Position.RIGHT,
      distance);

    final double dx = p1.getX() - p0.getX();
    final double dy = p1.getY() - p0.getY();
    final double angle = Math.atan2(dy, dx);

    final Point lp1 = offsetL.getP1();
    final Point rp1 = offsetR.getP1();
    switch (bufParams.getEndCapStyle()) {
      case BufferParameters.CAP_ROUND:
        // add offset seg points with a fillet between them
        segList.addPt(lp1);
        addFillet(p1, angle + Math.PI / 2, angle - Math.PI / 2,
          CGAlgorithms.CLOCKWISE, distance);
        segList.addPt(rp1);
      break;
      case BufferParameters.CAP_FLAT:
        // only offset segment points are added
        segList.addPt(lp1);
        segList.addPt(rp1);
      break;
      case BufferParameters.CAP_SQUARE:
        // add a square defined by extensions of the offset segment endpoints
        final Point squareCapSideOffset = new PointDouble(Math.abs(distance)
          * Math.cos(angle), Math.abs(distance) * Math.sin(angle));

        final double lx = lp1.getX() + squareCapSideOffset.getX();
        final double ly = lp1.getY() + squareCapSideOffset.getY();
        segList.addPt(lx, ly);

        final double rx = rp1.getX() + squareCapSideOffset.getX();
        final double ry = rp1.getY() + squareCapSideOffset.getY();
        segList.addPt(rx, ry);
      break;

    }
  }

  /**
   * Adds a mitre join connecting the two reflex offset segments.
   * The mitre will be beveled if it exceeds the mitre ratio limit.
   * 
   * @param offset0 the first offset segment
   * @param offset1 the second offset segment
   * @param distance the offset distance
   */
  private void addMitreJoin(final Point p, final LineSegment offset0,
    final LineSegment offset1, final double distance) {
    boolean isMitreWithinLimit = true;
    Point intPt = null;

    /**
     * This computation is unstable if the offset segments are nearly collinear.
     * Howver, this situation should have been eliminated earlier by the check for 
     * whether the offset segment endpoints are almost coincident
     */
    try {
      intPt = HCoordinate.intersection(offset0.getP0(), offset0.getP1(),
        offset1.getP0(), offset1.getP1());

      final double mitreRatio = distance <= 0.0 ? 1.0 : intPt.distance(p)
        / Math.abs(distance);

      if (mitreRatio > bufParams.getMitreLimit()) {
        isMitreWithinLimit = false;
      }
    } catch (final NotRepresentableException ex) {
      intPt = new PointDouble(0.0, 0.0, Point.NULL_ORDINATE);
      isMitreWithinLimit = false;
    }

    if (isMitreWithinLimit) {
      segList.addPt(intPt);
    } else {
      addLimitedMitreJoin(offset0, offset1, distance, bufParams.getMitreLimit());
      // addBevelJoin(offset0, offset1);
    }
  }

  public void addNextSegment(final Point p, final boolean addStartPoint) {
    // s0-s1-s2 are the coordinates of the previous segment and the current one
    this.s0 = this.s1;
    this.s1 = this.s2;
    this.s2 = p;
    offset0 = createOffsetSegment(s0, s1, side, distance);
    offset1 = createOffsetSegment(s1, s2, side, distance);

    // do nothing if points are equal
    if (s1.equals(s2)) {
      return;
    }

    final int orientation = CGAlgorithmsDD.orientationIndex(s0, s1, s2);
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
     * Heuristic: If offset endpoints are very close together, 
     * just use one of them as the corner vertex.
     * This avoids problems with computing mitre corners in the case
     * where the two segments are almost parallel 
     * (which is hard to compute a robust intersection for).
     */
    if (offset0.getP1().distance(offset1.getP0()) < distance
      * OFFSET_SEGMENT_SEPARATION_FACTOR) {
      segList.addPt(offset0.getP1());
      return;
    }

    if (bufParams.getJoinStyle() == BufferParameters.JOIN_MITRE) {
      addMitreJoin(s1, offset0, offset1, distance);
    } else if (bufParams.getJoinStyle() == BufferParameters.JOIN_BEVEL) {
      addBevelJoin(offset0, offset1);
    } else {
      // add a circular fillet connecting the endpoints of the offset segments
      if (addStartPoint) {
        segList.addPt(offset0.getP1());
      }
      // TESTING - comment out to produce beveled joins
      addFillet(s1, offset0.getP1(), offset1.getP0(), orientation, distance);
      segList.addPt(offset1.getP0());
    }
  }

  public void addSegments(final LineString points, final boolean isForward) {
    segList.addPts(points, isForward);
  }

  public void closeRing() {
    segList.closeRing();
  }

  /**
   * Creates a CW circle around a point
   */
  public void createCircle(final Point point) {
    if (point == null) {
      Debug.noOp();
    } else {
      // add start point
      final double x = point.getX() + distance;
      final double y = point.getY();
      segList.addPt(x, y);
      addFillet(point, 0.0, 2.0 * Math.PI, -1, distance);
      segList.closeRing();
    }
  }

  /**
    * Compute an offset segment for an input segment on a given side and at a given distance.
    * The offset points are computed in full double precision, for accuracy.
    *
    * @param seg the segment to offset
    * @param side the side of the segment ({@link Position}) the offset lies on
    * @param distance the offset distance
    * @param offset the points computed for the offset segment
    */
  private LineSegment createOffsetSegment(final LineSegment seg,
    final int side, final double distance) {
    final int sideSign = side == Position.LEFT ? 1 : -1;
    final double dx = seg.getX(1) - seg.getX(0);
    final double dy = seg.getY(1) - seg.getY(0);
    final double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    final double ux = sideSign * distance * dx / len;
    final double uy = sideSign * distance * dy / len;
    final double x1 = precisionModel.makePrecise(0, seg.getX(0) - uy);
    final double y1 = precisionModel.makePrecise(1, seg.getY(0) + ux);
    final double x2 = precisionModel.makePrecise(0, seg.getX(1) - uy);
    final double y2 = precisionModel.makePrecise(1, seg.getY(1) + ux);
    return new LineSegmentDouble(2, x1, y1, x2, y2);
  }

  private LineSegment createOffsetSegment(final Point p1, final Point p2,
    final int side, final double distance) {
    final int sideSign = side == Position.LEFT ? 1 : -1;
    final double p1x = p1.getX();
    final double p1y = p1.getY();
    final double p2x = p2.getX();
    final double p2Y = p2.getY();
    final double dx = p2x - p1x;
    final double dy = p2Y - p1y;
    final double len = Math.sqrt(dx * dx + dy * dy);
    // u is the vector that is the length of the offset, in the direction of the
    // segment
    final double ux = sideSign * distance * dx / len;
    final double uy = sideSign * distance * dy / len;
    final double x1 = precisionModel.makePrecise(0, p1x - uy);
    final double y1 = precisionModel.makePrecise(1, p1y + ux);
    final double x2 = precisionModel.makePrecise(0, p2x - uy);
    final double y2 = precisionModel.makePrecise(1, p2Y + ux);
    final LineSegmentDouble line = new LineSegmentDouble(2, x1, y1, x2, y2);
    return line;
  }

  /**
   * Creates a CW square around a point
   */
  public void createSquare(final Point p) {
    segList.addPt(p.getX() + distance, p.getY() + distance);
    segList.addPt(p.getX() + distance, p.getY() - distance);
    segList.addPt(p.getX() - distance, p.getY() - distance);
    segList.addPt(p.getX() - distance, p.getY() + distance);
    segList.closeRing();
  }

  public LineString getPoints() {
    return segList.getPoints();
  }

  /**
   * Tests whether the input has a narrow concave angle
   * (relative to the offset distance).
   * In this case the generated offset curve will contain self-intersections
   * and heuristic closing segments.
   * This is expected behaviour in the case of Buffer curves. 
   * For pure Offset Curves,
   * the output needs to be further treated 
   * before it can be used. 
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
     * Choose the min vertex separation as a small fraction of the offset distance.
     */
    segList.setMinimumVertexDistance(distance
      * CURVE_VERTEX_SNAP_DISTANCE_FACTOR);
  }

  public void initSideSegments(final Point s1, final Point s2, final int side) {
    this.s1 = s1;
    this.s2 = s2;
    this.side = side;
    offset1 = createOffsetSegment(s1, s2, side, distance);
  }

  @Override
  public String toString() {
    return segList.toString();
  }
}
