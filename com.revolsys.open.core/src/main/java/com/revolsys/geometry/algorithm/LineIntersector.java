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
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.util.Assert;
import com.revolsys.record.io.format.wkt.EWktWriter;

/**
 * A <code>LineIntersector</code> is an algorithm that can both test whether
 * two line segments intersect and compute the intersection point(s)
 * if they do.
 * <p>
 * There are three possible outcomes when determining whether two line segments intersect:
 * <ul>
 * <li>{@link #NO_INTERSECTION} - the segments do not intersect
 * <li>{@link #POINT_INTERSECTION - the segments intersect in a single point
 * <li>{@link #COLLINEAR_INTERSECTION - the segments are collinear and they intersect in a line segment
 * </ul>
 * For segments which intersect in a single point, the point may be either an endpoint
 * or in the interior of each segment.
 * If the point lies in the interior of both segments,
 * this is termed a <i>proper intersection</i>.
 * The method {@link #isProper()} test for this situation.
 * <p>
 * The intersection point(s) may be computed in a precise or non-precise manner.
 * Computing an intersection point precisely involves rounding it
 * via a supplied scale.
 * <p>
 * LineIntersectors do not perform an initial envelope intersection test
 * to determine if the segments are disjoint.
 * This is because this class is likely to be used in a context where
 * envelope overlap is already known to occur (or be likely).
 *
 * @version 1.7
 */
public abstract class LineIntersector {
  public final static int COLLINEAR = 2;

  /**
   * Indicates that line segments intersect in a line segment
   */
  public final static int COLLINEAR_INTERSECTION = 2;

  public final static int DO_INTERSECT = 1;

  /**
   * These are deprecated, due to ambiguous naming
   */
  public final static int DONT_INTERSECT = 0;

  /**
   * Indicates that line segments do not intersect
   */
  public final static int NO_INTERSECTION = 0;

  /**
   * Indicates that line segments intersect in a single point
   */
  public final static int POINT_INTERSECTION = 1;

  /**
   * Computes the "edge distance" of an intersection point p along a segment.
   * The edge distance is a metric of the point along the edge.
   * The metric used is a robust and easy to compute metric function.
   * It is <b>not</b> equivalent to the usual Euclidean metric.
   * It relies on the fact that either the x or the y ordinates of the
   * points in the edge are unique, depending on whether the edge is longer in
   * the horizontal or vertical direction.
   * <p>
   * NOTE: This function may produce incorrect distances
   *  for inputs where p is not precisely on p1-p2
   * (E.g. p = (139,9) p1 = (139,10), p2 = (280,1) produces distanct 0.0, which is incorrect.
   * <p>
   * My hypothesis is that the function is safe to use for points which are the
   * result of <b>rounding</b> points which lie on the line,
   * but not safe to use for <b>truncated</b> points.
   */
  public static double computeEdgeDistance(final Point p, final Point p0, final Point p1) {
    final double dx = Math.abs(p1.getX() - p0.getX());
    final double dy = Math.abs(p1.getY() - p0.getY());

    double dist = -1.0; // sentinel value
    if (p.equals(p0)) {
      dist = 0.0;
    } else if (p.equals(p1)) {
      if (dx > dy) {
        dist = dx;
      } else {
        dist = dy;
      }
    } else {
      final double pdx = Math.abs(p.getX() - p0.getX());
      final double pdy = Math.abs(p.getY() - p0.getY());
      if (dx > dy) {
        dist = pdx;
      } else {
        dist = pdy;
      }
      // <FIX>
      // hack to ensure that non-endpoints always have a non-zero distance
      if (dist == 0.0 && !p.equals(p0)) {
        dist = Math.max(pdx, pdy);
      }
    }
    Assert.isTrue(!(dist == 0.0 && !p.equals(p0)), "Bad distance calculation");
    return dist;
  }

  /**
   * This function is non-robust, since it may compute the square of large numbers.
   * Currently not sure how to improve this.
   */
  public static double nonRobustComputeEdgeDistance(final Point p, final Point p1, final Point p2) {
    final double dx = p.getX() - p1.getX();
    final double dy = p.getY() - p1.getY();
    final double dist = Math.sqrt(dx * dx + dy * dy); // dummy value
    Assert.isTrue(!(dist == 0.0 && !p.equals(p1)), "Invalid distance calculation");
    return dist;
  }

  protected Point[][] inputLines = new Point[2][2];

  /**
   * The indexes of the endpoints of the intersection lines, in order along
   * the corresponding line
   */
  protected int[][] intLineIndex;

  protected Point[] intPt = new Point[2];

  protected boolean isProper;

  protected Point pa;

  protected Point pb;

  protected int result;

  private double scale;

  // public int numIntersects = 0;

  public LineIntersector() {
    this.intPt[0] = new PointDouble();
    this.intPt[1] = new PointDouble();
    // alias the intersection points for ease of reference
    this.pa = this.intPt[0];
    this.pb = this.intPt[1];
    this.result = 0;
  }

  public LineIntersector(final double scale) {
    this.scale = scale;
  }

  protected abstract int computeIntersect(Point p1, Point p2, Point q1, Point q2);

  /**
   * Compute the intersection of a point p and the line p1-p2.
   * This function computes the boolean value of the hasIntersection test.
   * The actual value of the intersection (if there is one)
   * is equal to the value of <code>p</code>.
   */
  public abstract void computeIntersection(Point p, Point p1, Point p2);

  /**
   * Computes the intersection of the lines p1-p2 and p3-p4.
   * This function computes both the boolean value of the hasIntersection test
   * and the (approximate) value of the intersection point itself (if there is one).
   */
  public void computeIntersection(final Point p1, final Point p2, final Point p3, final Point p4) {
    this.inputLines[0][0] = p1;
    this.inputLines[0][1] = p2;
    this.inputLines[1][0] = p3;
    this.inputLines[1][1] = p4;
    this.result = computeIntersect(p1, p2, p3, p4);
    // numIntersects++;
  }

  protected void computeIntLineIndex() {
    if (this.intLineIndex == null) {
      this.intLineIndex = new int[2][2];
      computeIntLineIndex(0);
      computeIntLineIndex(1);
    }
  }

  protected void computeIntLineIndex(final int segmentIndex) {
    final double dist0 = getEdgeDistance(segmentIndex, 0);
    final double dist1 = getEdgeDistance(segmentIndex, 1);
    if (dist0 > dist1) {
      this.intLineIndex[segmentIndex][0] = 0;
      this.intLineIndex[segmentIndex][1] = 1;
    } else {
      this.intLineIndex[segmentIndex][0] = 1;
      this.intLineIndex[segmentIndex][1] = 0;
    }
  }

  /**
   * Computes the "edge distance" of an intersection point along the specified input line segment.
   *
   * @param segmentIndex is 0 or 1
   * @param intIndex is 0 or 1
   *
   * @return the edge distance of the intersection point
   */
  public double getEdgeDistance(final int segmentIndex, final int intIndex) {
    final double dist = computeEdgeDistance(this.intPt[intIndex], this.inputLines[segmentIndex][0],
      this.inputLines[segmentIndex][1]);
    return dist;
  }

  /**
   * Gets an endpoint of an input segment.
   *
   * @param segmentIndex the index of the input segment (0 or 1)
   * @param ptIndex the index of the endpoint (0 or 1)
   * @return the specified endpoint
   */
  public Point getEndpoint(final int segmentIndex, final int ptIndex) {
    return this.inputLines[segmentIndex][ptIndex];
  }

  /**
   * Computes the index (order) of the intIndex'th intersection point in the direction of
   * a specified input line segment
   *
   * @param segmentIndex is 0 or 1
   * @param intIndex is 0 or 1
   *
   * @return the index of the intersection point along the input segment (0 or 1)
   */
  public int getIndexAlongSegment(final int segmentIndex, final int intIndex) {
    computeIntLineIndex();
    return this.intLineIndex[segmentIndex][intIndex];
  }

  /**
   * Returns the intIndex'th intersection point
   *
   * @param intIndex is 0 or 1
   *
   * @return the intIndex'th intersection point
   */
  public Point getIntersection(final int intIndex) {
    return this.intPt[intIndex];
  }

  /*
   * public String toString() { String str = inputLines[0][0] + "-" + inputLines[0][1] + " " +
   * inputLines[1][0] + "-" + inputLines[1][1] + " : " + getTopologySummary(); return str; }
   */

  /**
   * Computes the intIndex'th intersection point in the direction of
   * a specified input line segment
   *
   * @param segmentIndex is 0 or 1
   * @param intIndex is 0 or 1
   *
   * @return the intIndex'th intersection point in the direction of the specified input line segment
   */
  public Point getIntersectionAlongSegment(final int segmentIndex, final int intIndex) {
    // lazily compute int line array
    computeIntLineIndex();
    return this.intPt[this.intLineIndex[segmentIndex][intIndex]];
  }

  /**
   * Returns the number of intersection points found.  This will be either 0, 1 or 2.
   *
   * @return the number of intersection points found (0, 1, or 2)
   */
  public int getIntersectionNum() {
    return this.result;
  }

  public double getScale() {
    return this.scale;
  }

  private String getTopologySummary() {
    final StringBuilder catBuf = new StringBuilder();
    if (isEndPoint()) {
      catBuf.append(" endpoint");
    }
    if (this.isProper) {
      catBuf.append(" proper");
    }
    if (isCollinear()) {
      catBuf.append(" collinear");
    }
    return catBuf.toString();
  }

  /**
   * Tests whether the input geometries intersect.
   *
   * @return true if the input geometries intersect
   */
  public boolean hasIntersection() {
    return this.result != NO_INTERSECTION;
  }

  protected boolean isCollinear() {
    return this.result == COLLINEAR_INTERSECTION;
  }

  protected boolean isEndPoint() {
    return hasIntersection() && !this.isProper;
  }

  /**
   * Tests whether either intersection point is an interior point of one of the input segments.
   *
   * @return <code>true</code> if either intersection point is in the interior of one of the input segments
   */
  public boolean isInteriorIntersection() {
    if (isInteriorIntersection(0)) {
      return true;
    }
    if (isInteriorIntersection(1)) {
      return true;
    }
    return false;
  }

  /**
   * Tests whether either intersection point is an interior point of the specified input segment.
   *
   * @return <code>true</code> if either intersection point is in the interior of the input segment
   */
  public boolean isInteriorIntersection(final int inputLineIndex) {
    for (int i = 0; i < this.result; i++) {
      if (!(this.intPt[i].equals(2, this.inputLines[inputLineIndex][0])
        || this.intPt[i].equals(2, this.inputLines[inputLineIndex][1]))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test whether a point is a intersection point of two line segments.
   * Note that if the intersection is a line segment, this method only tests for
   * equality with the endpoints of the intersection segment.
   * It does <b>not</b> return true if
   * the input point is internal to the intersection segment.
   *
   * @return true if the input point is one of the intersection points.
   */
  public boolean isIntersection(final Point pt) {
    for (int i = 0; i < this.result; i++) {
      if (this.intPt[i].equals(2, pt)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether an intersection is proper.
   * <br>
   * The intersection between two line segments is considered proper if
   * they intersect in a single point in the interior of both segments
   * (e.g. the intersection is a single point and is not equal to any of the
   * endpoints).
   * <p>
   * The intersection between a point and a line segment is considered proper
   * if the point lies in the interior of the segment (e.g. is not equal to
   * either of the endpoints).
   *
   * @return true if the intersection is proper
   */
  public boolean isProper() {
    return hasIntersection() && this.isProper;
  }

  public void setScale(final double scale) {
    this.scale = scale;
  }

  @Override
  public String toString() {
    return EWktWriter.lineString(this.inputLines[0][0], this.inputLines[0][1]) + " - "
      + EWktWriter.lineString(this.inputLines[1][0], this.inputLines[1][1]) + getTopologySummary();
  }
}
