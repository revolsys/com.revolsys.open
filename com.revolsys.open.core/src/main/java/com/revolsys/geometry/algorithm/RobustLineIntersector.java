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
package com.revolsys.geometry.algorithm;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.CentralEndpointIntersector;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.util.BoundingBoxUtil;

/**
 * A robust version of {@link LineIntersector}.
 *
 * @version 1.7
 * @see RobustDeterminant
 */
public class RobustLineIntersector extends LineIntersector {

  /**
   * Finds the endpoint of the segments P and Q which
   * is closest to the other segment.
   * This is a reasonable surrogate for the true
   * intersection points in ill-conditioned cases
   * (e.g. where two segments are nearly coincident,
   * or where the endpoint of one segment lies almost on the other segment).
   * <p>
   * This replaces the older CentralEndpoint heuristic,
   * which chose the wrong endpoint in some cases
   * where the segments had very distinct slopes
   * and one endpoint lay almost on the other segment.
   *
   * @param p1 an endpoint of segment P
   * @param p2 an endpoint of segment P
   * @param q1 an endpoint of segment Q
   * @param q2 an endpoint of segment Q
   * @return the nearest endpoint to the other segment
   */
  private static Point nearestEndpoint(final Point p1, final Point p2, final Point q1,
    final Point q2) {
    Point nearestPt = p1;
    double minDist = LineSegmentUtil.distanceLinePoint(q1, q2, p1);

    double dist = LineSegmentUtil.distanceLinePoint(q1, q2, p2);
    if (dist < minDist) {
      minDist = dist;
      nearestPt = p2;
    }
    dist = LineSegmentUtil.distanceLinePoint(p1, p2, q1);
    if (dist < minDist) {
      minDist = dist;
      nearestPt = q1;
    }
    dist = LineSegmentUtil.distanceLinePoint(p1, p2, q2);
    if (dist < minDist) {
      minDist = dist;
      nearestPt = q2;
    }
    return nearestPt;
  }

  public RobustLineIntersector() {
  }

  public RobustLineIntersector(final double scale) {
    super(scale);
  }

  private int computeCollinearIntersection(final Point p1, final Point p2, final Point q1,
    final Point q2) {
    final boolean p1q1p2 = BoundingBoxUtil.intersects(p1, p2, q1);
    final boolean p1q2p2 = BoundingBoxUtil.intersects(p1, p2, q2);
    final boolean q1p1q2 = BoundingBoxUtil.intersects(q1, q2, p1);
    final boolean q1p2q2 = BoundingBoxUtil.intersects(q1, q2, p2);

    if (p1q1p2 && p1q2p2) {
      this.intPt[0] = q1;
      this.intPt[1] = q2;
      return COLLINEAR_INTERSECTION;
    }
    if (q1p1q2 && q1p2q2) {
      this.intPt[0] = p1;
      this.intPt[1] = p2;
      return COLLINEAR_INTERSECTION;
    }
    if (p1q1p2 && q1p1q2) {
      this.intPt[0] = q1;
      this.intPt[1] = p1;
      return q1.equals(p1) && !p1q2p2 && !q1p2q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    if (p1q1p2 && q1p2q2) {
      this.intPt[0] = q1;
      this.intPt[1] = p2;
      return q1.equals(p2) && !p1q2p2 && !q1p1q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    if (p1q2p2 && q1p1q2) {
      this.intPt[0] = q2;
      this.intPt[1] = p1;
      return q2.equals(p1) && !p1q1p2 && !q1p2q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    if (p1q2p2 && q1p2q2) {
      this.intPt[0] = q2;
      this.intPt[1] = p2;
      return q2.equals(p2) && !p1q1p2 && !q1p1q2 ? POINT_INTERSECTION : COLLINEAR_INTERSECTION;
    }
    return NO_INTERSECTION;
  }

  @Override
  protected int computeIntersect(final Point p1, final Point p2, final Point q1, final Point q2) {
    this.isProper = false;

    // first try a fast test to see if the envelopes of the lines intersect
    if (!BoundingBoxUtil.intersects(p1, p2, q1, q2)) {
      return NO_INTERSECTION;
    }

    // for each endpoint, compute which side of the other segment it lies
    // if both endpoints lie on the same side of the other segment,
    // the segments do not intersect
    final int Pq1 = CGAlgorithmsDD.orientationIndex(p1, p2, q1);
    final int Pq2 = CGAlgorithmsDD.orientationIndex(p1, p2, q2);

    if (Pq1 > 0 && Pq2 > 0 || Pq1 < 0 && Pq2 < 0) {
      return NO_INTERSECTION;
    }

    final int Qp1 = CGAlgorithmsDD.orientationIndex(q1, q2, p1);
    final int Qp2 = CGAlgorithmsDD.orientationIndex(q1, q2, p2);

    if (Qp1 > 0 && Qp2 > 0 || Qp1 < 0 && Qp2 < 0) {
      return NO_INTERSECTION;
    }

    final boolean collinear = Pq1 == 0 && Pq2 == 0 && Qp1 == 0 && Qp2 == 0;
    if (collinear) {
      return computeCollinearIntersection(p1, p2, q1, q2);
    }

    /**
     * At this point we know that there is a single intersection point
     * (since the lines are not collinear).
     */

    /**
     *  Check if the intersection is an endpoint. If it is, copy the endpoint as
     *  the intersection point. Copying the point rather than computing it
     *  ensures the point has the exact value, which is important for
     *  robustness. It is sufficient to simply check for an endpoint which is on
     *  the other line, since at this point we know that the inputLines must
     *  intersect.
     */
    if (Pq1 == 0 || Pq2 == 0 || Qp1 == 0 || Qp2 == 0) {
      this.isProper = false;

      /**
       * Check for two equal endpoints.
       * This is done explicitly rather than by the orientation tests
       * below in order to improve robustness.
       *
       * [An example where the orientation tests fail to be consistent is
       * the following (where the true intersection is at the shared endpoint
       * POINT (19.850257749638203 46.29709338043669)
       *
       * LINESTRING ( 19.850257749638203 46.29709338043669, 20.31970698357233 46.76654261437082 )
       * and
       * LINESTRING ( -48.51001596420236 -22.063180333403878, 19.850257749638203 46.29709338043669 )
       *
       * which used to produce the INCORRECT result: (20.31970698357233, 46.76654261437082, NaN)
       *
       */
      if (p1.equals(2, q1) || p1.equals(2, q2)) {
        this.intPt[0] = p1;
      } else if (p2.equals(2, q1) || p2.equals(2, q2)) {
        this.intPt[0] = p2;
      }

      /**
       * Now check to see if any endpoint lies on the interior of the other segment.
       */
      else if (Pq1 == 0) {
        this.intPt[0] = new PointDouble(q1);
      } else if (Pq2 == 0) {
        this.intPt[0] = new PointDouble(q2);
      } else if (Qp1 == 0) {
        this.intPt[0] = new PointDouble(p1);
      } else if (Qp2 == 0) {
        this.intPt[0] = new PointDouble(p2);
      }
    } else {
      this.isProper = true;
      this.intPt[0] = intersection(p1, p2, q1, q2);
    }
    return POINT_INTERSECTION;
  }

  @Override
  public void computeIntersection(final Point p, final Point p1, final Point p2) {
    this.isProper = false;
    // do between check first, since it is faster than the orientation test
    if (BoundingBoxUtil.intersects(p1, p2, p)) {
      if (CGAlgorithmsDD.orientationIndex(p1, p2, p) == 0
        && CGAlgorithmsDD.orientationIndex(p2, p1, p) == 0) {
        this.isProper = true;
        if (p.equals(p1) || p.equals(p2)) {
          this.isProper = false;
        }
        this.result = POINT_INTERSECTION;
        return;
      }
    }
    this.result = NO_INTERSECTION;
  }

  /**
   * This method computes the actual value of the intersection point.
   * To obtain the maximum precision from the intersection calculation,
   * the coordinates are normalized by subtracting the minimum
   * ordinate values (in absolute value).  This has the effect of
   * removing common significant digits from the calculation to
   * maintain more bits of precision.
   */
  private Point intersection(final Point p1, final Point p2, final Point q1, final Point q2) {
    Point intPt = intersectionWithNormalization(p1, p2, q1, q2);

    /*
     * // TESTING ONLY Point intPtDD = CGAlgorithmsDD.intersection(p1, p2, q1, q2); double dist =
     * intPt.distance(intPtDD); System.out.println(intPt + " - " + intPtDD + " dist = " + dist);
     * //intPt = intPtDD;
     */

    /**
     * Due to rounding it can happen that the computed intersection is
     * outside the envelopes of the input segments.  Clearly this
     * is inconsistent.
     * This code checks this condition and forces a more reasonable answer
     *
     * MD - May 4 2005 - This is still a problem.  Here is a failure case:
     *
     * LINESTRING (2089426.5233462777 1180182.3877339689, 2085646.6891757075 1195618.7333999649)
     * LINESTRING (1889281.8148903656 1997547.0560044837, 2259977.3672235999 483675.17050843034)
     * int point = (2097408.2633752143,1144595.8008114607)
     *
     * MD - Dec 14 2006 - This does not seem to be a failure case any longer
     */
    if (!isInSegmentEnvelopes(intPt)) {
      // System.out.println("Intersection outside segment envelopes: " + intPt);

      // compute a safer result
      // copy the coordinate, since it may be rounded later
      intPt = new PointDouble(nearestEndpoint(p1, p2, q1, q2));
      // intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);

      // System.out.println("Segments: " + this);
      // System.out.println("Snapped to " + intPt);
      // checkDD(p1, p2, q1, q2, intPt);
    }
    return CoordinatesUtil.getPrecise(getScale(), intPt);
  }

  private Point intersectionWithNormalization(final Point p1, final Point p2, final Point q1,
    final Point q2) {

    final double minX0 = p1.getX() < p2.getX() ? p1.getX() : p2.getX();
    final double minY0 = p1.getY() < p2.getY() ? p1.getY() : p2.getY();
    final double maxX0 = p1.getX() > p2.getX() ? p1.getX() : p2.getX();
    final double maxY0 = p1.getY() > p2.getY() ? p1.getY() : p2.getY();

    final double minX1 = q1.getX() < q2.getX() ? q1.getX() : q2.getX();
    final double minY1 = q1.getY() < q2.getY() ? q1.getY() : q2.getY();
    final double maxX1 = q1.getX() > q2.getX() ? q1.getX() : q2.getX();
    final double maxY1 = q1.getY() > q2.getY() ? q1.getY() : q2.getY();

    final double intMinX = minX0 > minX1 ? minX0 : minX1;
    final double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
    final double intMinY = minY0 > minY1 ? minY0 : minY1;
    final double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;

    final double normX = (intMinX + intMaxX) / 2.0;
    final double normY = (intMinY + intMaxY) / 2.0;

    final Point n1 = new PointDouble(p1.getX() - normX, p1.getY() - normY);
    final Point n2 = new PointDouble(p2.getX() - normX, p2.getY() - normY);
    final Point n3 = new PointDouble(q1.getX() - normX, q1.getY() - normY);
    final Point n4 = new PointDouble(q2.getX() - normX, q2.getY() - normY);

    final Point intPt = safeHCoordinatesIntersection(n1, n2, n3, n4);

    final double x = intPt.getX() + normX;
    final double y = intPt.getY() + normY;

    return new PointDouble(x, y);
  }

  /**
   * Tests whether a point lies in the envelopes of both input segments.
   * A correctly computed intersection point should return <code>true</code>
   * for this test.
   * Since this test is for debugging purposes only, no attempt is
   * made to optimize the envelope test.
   *
   * @return <code>true</code> if the input point lies within both input segment envelopes
   */
  private boolean isInSegmentEnvelopes(final Point intPt) {
    final BoundingBox env0 = BoundingBoxDoubleXY.newBoundingBox(this.inputLines[0][0],
      this.inputLines[0][1]);
    final BoundingBox env1 = BoundingBoxDoubleXY.newBoundingBox(this.inputLines[1][0],
      this.inputLines[1][1]);
    return env0.covers(intPt) && env1.covers(intPt);
  }

  /**
   * Computes a segment intersection using homogeneous coordinates. Round-off
   * error can cause the raw computation to fail, (usually due to the segments
   * being approximately parallel). If this happens, a reasonable approximation
   * is computed instead.
   *
   * @param p1 a segment endpoint
   * @param p2 a segment endpoint
   * @param q1 a segment endpoint
   * @param q2 a segment endpoint
   * @return the computed intersection point
   */
  private Point safeHCoordinatesIntersection(final Point p1, final Point p2, final Point q1,
    final Point q2) {
    Point intPt = null;
    try {
      intPt = HCoordinate.intersection(p1, p2, q1, q2);
    } catch (final NotRepresentableException e) {
      // System.out.println("Not calculable: " + this);
      // compute an approximate result
      intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);
      // System.out.println("Snapped to " + intPt);
    }
    return intPt;
  }
}
