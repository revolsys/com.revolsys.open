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
package com.revolsys.jts.algorithm;

/**
 *@version 1.7
 */

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.util.EnvelopeUtil;

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
  private static Coordinates nearestEndpoint(final Coordinates p1,
    final Coordinates p2, final Coordinates q1, final Coordinates q2) {
    Coordinates nearestPt = p1;
    double minDist = CGAlgorithms.distancePointLine(p1, q1, q2);

    double dist = CGAlgorithms.distancePointLine(p2, q1, q2);
    if (dist < minDist) {
      minDist = dist;
      nearestPt = p2;
    }
    dist = CGAlgorithms.distancePointLine(q1, p1, p2);
    if (dist < minDist) {
      minDist = dist;
      nearestPt = q1;
    }
    dist = CGAlgorithms.distancePointLine(q2, p1, p2);
    if (dist < minDist) {
      minDist = dist;
      nearestPt = q2;
    }
    return nearestPt;
  }

  public RobustLineIntersector() {
  }

  private void checkDD(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2, final Coordinates intPt) {
    final Coordinates intPtDD = CGAlgorithmsDD.intersection(p1, p2, q1, q2);
    final boolean isIn = isInSegmentEnvelopes(intPtDD);
    System.out.println("DD in env = " + isIn + "  --------------------- "
      + intPtDD);
    if (intPt.distance(intPtDD) > 0.0001) {
      System.out.println("Distance = " + intPt.distance(intPtDD));
    }
  }

  private int computeCollinearIntersection(final Coordinates p1,
    final Coordinates p2, final Coordinates q1, final Coordinates q2) {
    final boolean p1q1p2 = EnvelopeUtil.intersects(p1, p2, q1);
    final boolean p1q2p2 = EnvelopeUtil.intersects(p1, p2, q2);
    final boolean q1p1q2 = EnvelopeUtil.intersects(q1, q2, p1);
    final boolean q1p2q2 = EnvelopeUtil.intersects(q1, q2, p2);

    if (p1q1p2 && p1q2p2) {
      intPt[0] = q1;
      intPt[1] = q2;
      return COLLINEAR_INTERSECTION;
    }
    if (q1p1q2 && q1p2q2) {
      intPt[0] = p1;
      intPt[1] = p2;
      return COLLINEAR_INTERSECTION;
    }
    if (p1q1p2 && q1p1q2) {
      intPt[0] = q1;
      intPt[1] = p1;
      return q1.equals(p1) && !p1q2p2 && !q1p2q2 ? POINT_INTERSECTION
        : COLLINEAR_INTERSECTION;
    }
    if (p1q1p2 && q1p2q2) {
      intPt[0] = q1;
      intPt[1] = p2;
      return q1.equals(p2) && !p1q2p2 && !q1p1q2 ? POINT_INTERSECTION
        : COLLINEAR_INTERSECTION;
    }
    if (p1q2p2 && q1p1q2) {
      intPt[0] = q2;
      intPt[1] = p1;
      return q2.equals(p1) && !p1q1p2 && !q1p2q2 ? POINT_INTERSECTION
        : COLLINEAR_INTERSECTION;
    }
    if (p1q2p2 && q1p2q2) {
      intPt[0] = q2;
      intPt[1] = p2;
      return q2.equals(p2) && !p1q1p2 && !q1p1q2 ? POINT_INTERSECTION
        : COLLINEAR_INTERSECTION;
    }
    return NO_INTERSECTION;
  }

  @Override
  protected int computeIntersect(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    isProper = false;

    // first try a fast test to see if the envelopes of the lines intersect
    if (!EnvelopeUtil.intersects(p1, p2, q1, q2)) {
      return NO_INTERSECTION;
    }

    // for each endpoint, compute which side of the other segment it lies
    // if both endpoints lie on the same side of the other segment,
    // the segments do not intersect
    final int Pq1 = CGAlgorithms.orientationIndex(p1, p2, q1);
    final int Pq2 = CGAlgorithms.orientationIndex(p1, p2, q2);

    if ((Pq1 > 0 && Pq2 > 0) || (Pq1 < 0 && Pq2 < 0)) {
      return NO_INTERSECTION;
    }

    final int Qp1 = CGAlgorithms.orientationIndex(q1, q2, p1);
    final int Qp2 = CGAlgorithms.orientationIndex(q1, q2, p2);

    if ((Qp1 > 0 && Qp2 > 0) || (Qp1 < 0 && Qp2 < 0)) {
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
      isProper = false;

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
      if (p1.equals2d(q1) || p1.equals2d(q2)) {
        intPt[0] = p1;
      } else if (p2.equals2d(q1) || p2.equals2d(q2)) {
        intPt[0] = p2;
      }

      /**
       * Now check to see if any endpoint lies on the interior of the other segment.
       */
      else if (Pq1 == 0) {
        intPt[0] = new Coordinate(q1);
      } else if (Pq2 == 0) {
        intPt[0] = new Coordinate(q2);
      } else if (Qp1 == 0) {
        intPt[0] = new Coordinate(p1);
      } else if (Qp2 == 0) {
        intPt[0] = new Coordinate(p2);
      }
    } else {
      isProper = true;
      intPt[0] = intersection(p1, p2, q1, q2);
    }
    return POINT_INTERSECTION;
  }

  @Override
  public void computeIntersection(final Coordinates p, final Coordinates p1,
    final Coordinates p2) {
    isProper = false;
    // do between check first, since it is faster than the orientation test
    if (EnvelopeUtil.intersects(p1, p2, p)) {
      if ((CGAlgorithms.orientationIndex(p1, p2, p) == 0)
        && (CGAlgorithms.orientationIndex(p2, p1, p) == 0)) {
        isProper = true;
        if (p.equals(p1) || p.equals(p2)) {
          isProper = false;
        }
        result = POINT_INTERSECTION;
        return;
      }
    }
    result = NO_INTERSECTION;
  }

  /**
   * This method computes the actual value of the intersection point.
   * To obtain the maximum precision from the intersection calculation,
   * the coordinates are normalized by subtracting the minimum
   * ordinate values (in absolute value).  This has the effect of
   * removing common significant digits from the calculation to
   * maintain more bits of precision.
   */
  private Coordinates intersection(final Coordinates p1, final Coordinates p2,
    final Coordinates q1, final Coordinates q2) {
    Coordinates intPt = intersectionWithNormalization(p1, p2, q1, q2);

    /*
     * // TESTING ONLY Coordinates intPtDD = CGAlgorithmsDD.intersection(p1, p2,
     * q1, q2); double dist = intPt.distance(intPtDD); System.out.println(intPt
     * + " - " + intPtDD + " dist = " + dist); //intPt = intPtDD;
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
      intPt = new Coordinate(nearestEndpoint(p1, p2, q1, q2));
      // intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);

      // System.out.println("Segments: " + this);
      // System.out.println("Snapped to " + intPt);
      // checkDD(p1, p2, q1, q2, intPt);
    }
    if (precisionModel != null) {
      precisionModel.makePrecise(intPt);
    }
    return intPt;
  }

  private Coordinates intersectionWithNormalization(final Coordinates p1,
    final Coordinates p2, final Coordinates q1, final Coordinates q2) {
    final Coordinates n1 = new Coordinate(p1);
    final Coordinates n2 = new Coordinate(p2);
    final Coordinates n3 = new Coordinate(q1);
    final Coordinates n4 = new Coordinate(q2);
    final Coordinates normPt = new Coordinate();
    normalizeToEnvCentre(n1, n2, n3, n4, normPt);

    final Coordinates intPt = safeHCoordinateIntersection(n1, n2, n3, n4);

    intPt.setX(intPt.getX() + normPt.getX());
    intPt.setY(intPt.getY() + normPt.getY());

    return intPt;
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
  private boolean isInSegmentEnvelopes(final Coordinates intPt) {
    final BoundingBox env0 = new Envelope(inputLines[0][0], inputLines[0][1]);
    final BoundingBox env1 = new Envelope(inputLines[1][0], inputLines[1][1]);
    return env0.contains(intPt) && env1.contains(intPt);
  }

  /**
   * Normalize the supplied coordinates to
   * so that the midpoint of their intersection envelope
   * lies at the origin.
   *
   * @param n00
   * @param n01
   * @param n10
   * @param n11
   * @param normPt
   */
  private void normalizeToEnvCentre(final Coordinates n00,
    final Coordinates n01, final Coordinates n10, final Coordinates n11,
    final Coordinates normPt) {
    final double minX0 = n00.getX() < n01.getX() ? n00.getX() : n01.getX();
    final double minY0 = n00.getY() < n01.getY() ? n00.getY() : n01.getY();
    final double maxX0 = n00.getX() > n01.getX() ? n00.getX() : n01.getX();
    final double maxY0 = n00.getY() > n01.getY() ? n00.getY() : n01.getY();

    final double minX1 = n10.getX() < n11.getX() ? n10.getX() : n11.getX();
    final double minY1 = n10.getY() < n11.getY() ? n10.getY() : n11.getY();
    final double maxX1 = n10.getX() > n11.getX() ? n10.getX() : n11.getX();
    final double maxY1 = n10.getY() > n11.getY() ? n10.getY() : n11.getY();

    final double intMinX = minX0 > minX1 ? minX0 : minX1;
    final double intMaxX = maxX0 < maxX1 ? maxX0 : maxX1;
    final double intMinY = minY0 > minY1 ? minY0 : minY1;
    final double intMaxY = maxY0 < maxY1 ? maxY0 : maxY1;

    final double intMidX = (intMinX + intMaxX) / 2.0;
    final double intMidY = (intMinY + intMaxY) / 2.0;
    normPt.setX(intMidX);
    normPt.setY(intMidY);

    /*
     * // equilavalent code using more modular but slower method BoundingBox
     * env0 = new Envelope(n00, n01); BoundingBox env1 = new Envelope(n10, n11);
     * Envelope intEnv = env0.intersection(env1); Coordinates intMidPt =
     * intEnv.centre(); normPt.x = intMidPt.x; normPt.y = intMidPt.y;
     */

    n00.setX(n00.getX() - normPt.getX());
    n00.setY(n00.getY() - normPt.getY());
    n01.setX(n01.getX() - normPt.getX());
    n01.setY(n01.getY() - normPt.getY());
    n10.setX(n10.getX() - normPt.getX());
    n10.setY(n10.getY() - normPt.getY());
    n11.setX(n11.getX() - normPt.getX());
    n11.setY(n11.getY() - normPt.getY());
  }

  /**
   * Normalize the supplied coordinates so that
   * their minimum ordinate values lie at the origin.
   * NOTE: this normalization technique appears to cause
   * large errors in the position of the intersection point for some cases.
   *
   * @param n1
   * @param n2
   * @param n3
   * @param n4
   * @param normPt
   */
  private void normalizeToMinimum(final Coordinates n1, final Coordinates n2,
    final Coordinates n3, final Coordinates n4, final Coordinates normPt) {
    normPt.setX(smallestInAbsValue(n1.getX(), n2.getX(), n3.getX(), n4.getX()));
    normPt.setY(smallestInAbsValue(n1.getY(), n2.getY(), n3.getY(), n4.getY()));
    n1.setX(n1.getX() - normPt.getX());
    n1.setY(n1.getY() - normPt.getY());
    n2.setX(n2.getX() - normPt.getX());
    n2.setY(n2.getY() - normPt.getY());
    n3.setX(n3.getX() - normPt.getX());
    n3.setY(n3.getY() - normPt.getY());
    n4.setX(n4.getX() - normPt.getX());
    n4.setY(n4.getY() - normPt.getY());
  }

  /**
   * Computes a segment intersection using homogeneous coordinates.
   * Round-off error can cause the raw computation to fail, 
   * (usually due to the segments being approximately parallel).
   * If this happens, a reasonable approximation is computed instead.
   * 
   * @param p1 a segment endpoint
   * @param p2 a segment endpoint
   * @param q1 a segment endpoint
   * @param q2 a segment endpoint
   * @return the computed intersection point
   */
  private Coordinates safeHCoordinateIntersection(final Coordinates p1,
    final Coordinates p2, final Coordinates q1, final Coordinates q2) {
    Coordinates intPt = null;
    try {
      intPt = HCoordinate.intersection(p1, p2, q1, q2);
    } catch (final NotRepresentableException e) {
      // System.out.println("Not calculable: " + this);
      // compute an approximate result
      // intPt = CentralEndpointIntersector.getIntersection(p1, p2, q1, q2);
      intPt = nearestEndpoint(p1, p2, q1, q2);
      // System.out.println("Snapped to " + intPt);
    }
    return intPt;
  }

  private double smallestInAbsValue(final double x1, final double x2,
    final double x3, final double x4) {
    double x = x1;
    double xabs = Math.abs(x);
    if (Math.abs(x2) < xabs) {
      x = x2;
      xabs = Math.abs(x2);
    }
    if (Math.abs(x3) < xabs) {
      x = x3;
      xabs = Math.abs(x3);
    }
    if (Math.abs(x4) < xabs) {
      x = x4;
    }
    return x;
  }

}
