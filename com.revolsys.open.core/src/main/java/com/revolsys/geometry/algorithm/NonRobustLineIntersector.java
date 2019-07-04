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

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.impl.PointDouble;

/**
 * A non-robust version of {@link LineIntersector}.
 *
 * @version 1.7
 */
public class NonRobustLineIntersector extends LineIntersector {
  /**
   * @return true if both numbers are positive or if both numbers are negative.
   * Returns false if both numbers are zero.
   */
  public static boolean isSameSignAndNonZero(final double a, final double b) {
    if (a == 0 || b == 0) {
      return false;
    }
    return a < 0 && b < 0 || a > 0 && b > 0;
  }

  public NonRobustLineIntersector() {
  }

  /*
   * p1-p2 and p3-p4 are assumed to be collinear (although not necessarily
   * intersecting). Returns: DONT_INTERSECT : the two segments do not intersect
   * COLLINEAR : the segments intersect, in the line segment pa-pb. pa-pb is in
   * the same direction as p1-p2 DO_INTERSECT : the inputLines intersect in a
   * single point only, pa
   */
  private int computeCollinearIntersection(final Point p1, final Point p2, final Point p3,
    final Point p4) {
    double r1;
    double r2;
    double r3;
    double r4;
    Point q3;
    Point q4;
    double t3;
    double t4;
    r1 = 0;
    r2 = 1;
    r3 = rParameter(p1, p2, p3);
    r4 = rParameter(p1, p2, p4);
    // make sure p3-p4 is in same direction as p1-p2
    if (r3 < r4) {
      q3 = p3;
      t3 = r3;
      q4 = p4;
      t4 = r4;
    } else {
      q3 = p4;
      t3 = r4;
      q4 = p3;
      t4 = r3;
    }
    // check for no intersection
    if (t3 > r2 || t4 < r1) {
      return NO_INTERSECTION;
    }

    // check for single point intersection
    if (q4 == p1) {
      this.pa = p1;
      return POINT_INTERSECTION;
    }
    if (q3 == p2) {
      this.pa = p2;
      return POINT_INTERSECTION;
    }

    // intersection MUST be a segment - compute endpoints
    this.pa = p1;
    if (t3 > r1) {
      this.pa = q3;
    }
    this.pb = p2;
    if (t4 < r2) {
      this.pb = q4;
    }
    return COLLINEAR_INTERSECTION;
  }

  @Override
  protected int computeIntersect(final Point p1, final Point p2, final Point p3, final Point p4) {
    double a1;
    double b1;
    double c1;
    /*
     * Coefficients of line eqns.
     */
    double a2;
    /*
     * Coefficients of line eqns.
     */
    double b2;
    /*
     * Coefficients of line eqns.
     */
    double c2;
    double r1;
    double r2;
    double r3;
    double r4;
    /*
     * 'Sign' values
     */
    // double denom, offset, num; /* Intermediate values */

    this.isProper = false;

    /*
     * Compute a1, b1, c1, where line joining points 1 and 2 is
     * "a1 x  +  b1 y  +  c1  =  0".
     */
    a1 = p2.getY() - p1.getY();
    b1 = p1.getX() - p2.getX();
    c1 = p2.getX() * p1.getY() - p1.getX() * p2.getY();

    /*
     * Compute r3 and r4.
     */
    r3 = a1 * p3.getX() + b1 * p3.getY() + c1;
    r4 = a1 * p4.getX() + b1 * p4.getY() + c1;

    /*
     * Check signs of r3 and r4. If both point 3 and point 4 lie on same side of
     * line 1, the line segments do not intersect.
     */
    if (r3 != 0 && r4 != 0 && isSameSignAndNonZero(r3, r4)) {
      return NO_INTERSECTION;
    }

    /*
     * Compute a2, b2, c2
     */
    a2 = p4.getY() - p3.getY();
    b2 = p3.getX() - p4.getX();
    c2 = p4.getX() * p3.getY() - p3.getX() * p4.getY();

    /*
     * Compute r1 and r2
     */
    r1 = a2 * p1.getX() + b2 * p1.getY() + c2;
    r2 = a2 * p2.getX() + b2 * p2.getY() + c2;

    /*
     * Check signs of r1 and r2. If both point 1 and point 2 lie on same side of
     * second line segment, the line segments do not intersect.
     */
    if (r1 != 0 && r2 != 0 && isSameSignAndNonZero(r1, r2)) {
      return NO_INTERSECTION;
    }

    /**
     *  Line segments intersect: compute intersection point.
     */
    final double denom = a1 * b2 - a2 * b1;
    if (denom == 0) {
      return computeCollinearIntersection(p1, p2, p3, p4);
    }
    final double numX = b1 * c2 - b2 * c1;
    /*
     * TESTING ONLY double valX = (( num < 0 ? num - offset : num + offset ) /
     * denom); double valXInt = (int) (( num < 0 ? num - offset : num + offset )
     * / denom); if (valXInt != pa.x) // TESTING ONLY System.out.println(val +
     * " - int: " + valInt + ", floor: " + pa.x);
     */
    final double numY = a2 * c1 - a1 * c2;
    this.pa = new PointDouble(numX / denom, numY / denom);

    // check if this is a proper intersection BEFORE truncating values,
    // to avoid spurious equality comparisons with endpoints
    this.isProper = true;
    if (this.pa.equals(p1) || this.pa.equals(p2) || this.pa.equals(p3) || this.pa.equals(p4)) {
      this.isProper = false;
    }

    this.pa = CoordinatesUtil.getPrecise(getScale(), this.pa);
    return POINT_INTERSECTION;
  }

  @Override
  public boolean computeIntersection(final double x, final double y, final double x1,
    final double y1, final double x2, final double y2) {
    this.isProper = false;

    /*
     * Compute a1, b1, c1, where line joining points 1 and 2 is
     * "a1 x  +  b1 y  +  c1  =  0".
     */
    final double a1 = y2 - y1;
    final double b1 = x1 - x2;
    final double c1 = x2 * y1 - x1 * y2;

    /*
     * Compute r3 and r4.
     */
    final double r = a1 * x + b1 * y + c1;

    // if r != 0 the point does not lie on the line
    if (r != 0) {
      this.intersectionCount = NO_INTERSECTION;
      return false;
    }

    // Point lies on line - check to see whether it lies in line segment.

    final double dist = rParameter(x, y, x1, y1, x2, y2);
    if (dist < 0.0 || dist > 1.0) {
      this.intersectionCount = NO_INTERSECTION;
      return false;
    }

    this.isProper = true;
    if (x == x1 && y == y1 || x == x2 && y == y2) {
      this.isProper = false;
    }
    this.intersectionCount = POINT_INTERSECTION;
    return true;
  }

  @Override
  public void computeIntersectionPoints(final Point p, final Point p1, final Point p2) {
    double a1;
    double b1;
    double c1;
    /*
     * Coefficients of line eqns.
     */
    double r;
    /*
     * 'Sign' values
     */
    this.isProper = false;

    /*
     * Compute a1, b1, c1, where line joining points 1 and 2 is
     * "a1 x  +  b1 y  +  c1  =  0".
     */
    a1 = p2.getY() - p1.getY();
    b1 = p1.getX() - p2.getX();
    c1 = p2.getX() * p1.getY() - p1.getX() * p2.getY();

    /*
     * Compute r3 and r4.
     */
    r = a1 * p.getX() + b1 * p.getY() + c1;

    // if r != 0 the point does not lie on the line
    if (r != 0) {
      this.intersectionCount = NO_INTERSECTION;
      return;
    }

    // Point lies on line - check to see whether it lies in line segment.

    final double dist = rParameter(p1, p2, p);
    if (dist < 0.0 || dist > 1.0) {
      this.intersectionCount = NO_INTERSECTION;
      return;
    }

    this.isProper = true;
    if (p.equals(p1) || p.equals(p2)) {
      this.isProper = false;
    }
    this.intersectionCount = POINT_INTERSECTION;
  }

  /**
   *  RParameter computes the parameter for the point p
   *  in the parameterized equation
   *  of the line from p1 to p2.
   *  This is equal to the 'distance' of p along p1-p2
   */
  private double rParameter(final double x, final double y, final double x1, final double y1,
    final double x2, final double y2) {
    final double dx = Math.abs(x2 - x1);
    final double dy = Math.abs(y2 - y1);
    // compute maximum delta, for numerical stability
    // also handle case of p1-p2 being vertical or horizontal
    double r;
    if (dx > dy) {
      r = (x - x1) / (x2 - x1);
    } else {
      r = (y - y1) / (y2 - y1);
    }
    return r;
  }

  /**
   *  RParameter computes the parameter for the point p
   *  in the parameterized equation
   *  of the line from p1 to p2.
   *  This is equal to the 'distance' of p along p1-p2
   */
  private double rParameter(final Point p1, final Point p2, final Point p) {
    double r;
    // compute maximum delta, for numerical stability
    // also handle case of p1-p2 being vertical or horizontal
    final double dx = Math.abs(p2.getX() - p1.getX());
    final double dy = Math.abs(p2.getY() - p1.getY());
    if (dx > dy) {
      r = (p.getX() - p1.getX()) / (p2.getX() - p1.getX());
    } else {
      r = (p.getY() - p1.getY()) / (p2.getY() - p1.getY());
    }
    return r;
  }

}
