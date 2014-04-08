package com.revolsys.gis.model.geometry.operation.geomgraph.index;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.util.Assert;

/**
 * @version 1.7
 */

/**
 * A LineIntersector is an algorithm that can both test whether two line
 * segments intersect and compute the intersection point if they do. The
 * intersection point may be computed in a precise or non-precise manner.
 * Computing it precisely involves rounding it to an integer. (This assumes that
 * the input coordinates have been made precise by scaling them to an integer
 * grid.)
 * 
 * @version 1.7
 */
public abstract class LineIntersector {
  /**
   * These are deprecated, due to ambiguous naming
   */
  public final static int DONT_INTERSECT = 0;

  public final static int DO_INTERSECT = 1;

  public final static int COLLINEAR = 2;

  /**
   * Indicates that line segments do not intersect
   */
  public final static int NO_INTERSECTION = 0;

  /**
   * Indicates that line segments intersect in a single point
   */
  public final static int POINT_INTERSECTION = 1;

  /**
   * Indicates that line segments intersect in a line segment
   */
  public final static int COLLINEAR_INTERSECTION = 2;

  /**
   * Computes the "edge distance" of an intersection point p along a segment.
   * The edge distance is a metric of the point along the edge. The metric used
   * is a robust and easy to compute metric function. It is <b>not</b>
   * equivalent to the usual Euclidean metric. It relies on the fact that either
   * the x or the y ordinates of the points in the edge are unique, depending on
   * whether the edge is longer in the horizontal or vertical direction.
   * <p>
   * NOTE: This function may produce incorrect distances for inputs where p is
   * not precisely on p1-p2 (E.g. p = (139,9) p1 = (139,10), p2 = (280,1)
   * produces distanct 0.0, which is incorrect.
   * <p>
   * My hypothesis is that the function is safe to use for points which are the
   * result of <b>rounding</b> points which lie on the line, but not safe to use
   * for <b>truncated</b> points.
   */
  public static double computeEdgeDistance(final Coordinates p,
    final Coordinates p0, final Coordinates p1) {
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
   * This function is non-robust, since it may compute the square of large
   * numbers. Currently not sure how to improve this.
   */
  public static double nonRobustComputeEdgeDistance(final Coordinates p,
    final Coordinates p1, final Coordinates p2) {
    final double dx = p.getX() - p1.getX();
    final double dy = p.getY() - p1.getY();
    final double dist = Math.sqrt(dx * dx + dy * dy); // dummy value
    Assert.isTrue(!(dist == 0.0 && !p.equals(p1)),
      "Invalid distance calculation");
    return dist;
  }

  protected int result;

  protected Coordinates[][] inputLines = new Coordinates[2][2];

  protected Coordinates[] intPt = new Coordinates[2];

  /**
   * The indexes of the endpoints of the intersection lines, in order along the
   * corresponding line
   */
  protected int[][] intLineIndex;

  protected boolean isProper;

  protected Coordinates pa;

  protected Coordinates pb;

  /**
   * If makePrecise is true, computed intersection coordinates will be made
   * precise using Coordinates#makePrecise
   */
  protected CoordinatesPrecisionModel precisionModel = null;

  // public int numIntersects = 0;

  public LineIntersector() {
    intPt[0] = new DoubleCoordinates(2);
    intPt[1] = new DoubleCoordinates(2);
    // alias the intersection points for ease of reference
    pa = intPt[0];
    pb = intPt[1];
    result = 0;
  }

  protected abstract int computeIntersect(Coordinates p1, Coordinates p2,
    Coordinates q1, Coordinates q2);

  /**
   * Compute the intersection of a point p and the line p1-p2. This function
   * computes the boolean value of the hasIntersection test. The actual value of
   * the intersection (if there is one) is equal to the value of <code>p</code>.
   */
  public abstract void computeIntersection(Coordinates p, Coordinates p1,
    Coordinates p2);

  /**
   * Computes the intersection of the lines p1-p2 and p3-p4. This function
   * computes both the boolean value of the hasIntersection test and the
   * (approximate) value of the intersection point itself (if there is one).
   */
  public void computeIntersection(final Coordinates p1, final Coordinates p2,
    final Coordinates p3, final Coordinates p4) {
    inputLines[0][0] = p1;
    inputLines[0][1] = p2;
    inputLines[1][0] = p3;
    inputLines[1][1] = p4;
    result = computeIntersect(p1, p2, p3, p4);
    // numIntersects++;
  }

  protected void computeIntLineIndex() {
    if (intLineIndex == null) {
      intLineIndex = new int[2][2];
      computeIntLineIndex(0);
      computeIntLineIndex(1);
    }
  }

  protected void computeIntLineIndex(final int segmentIndex) {
    final double dist0 = getEdgeDistance(segmentIndex, 0);
    final double dist1 = getEdgeDistance(segmentIndex, 1);
    if (dist0 > dist1) {
      intLineIndex[segmentIndex][0] = 0;
      intLineIndex[segmentIndex][1] = 1;
    } else {
      intLineIndex[segmentIndex][0] = 1;
      intLineIndex[segmentIndex][1] = 0;
    }
  }

  /**
   * Computes the "edge distance" of an intersection point along the specified
   * input line segment.
   * 
   * @param segmentIndex is 0 or 1
   * @param intIndex is 0 or 1
   * @return the edge distance of the intersection point
   */
  public double getEdgeDistance(final int segmentIndex, final int intIndex) {
    final double dist = computeEdgeDistance(intPt[intIndex],
      inputLines[segmentIndex][0], inputLines[segmentIndex][1]);
    return dist;
  }

  /*
   * public String toString() { String str = inputLines[0][0] + "-" +
   * inputLines[0][1] + " " + inputLines[1][0] + "-" + inputLines[1][1] + " : "
   * + getTopologySummary(); return str; }
   */

  /**
   * Gets an endpoint of an input segment.
   * 
   * @param segmentIndex the index of the input segment (0 or 1)
   * @param ptIndex the index of the endpoint (0 or 1)
   * @return the specified endpoint
   */
  public Coordinates getEndpoint(final int segmentIndex, final int ptIndex) {
    return inputLines[segmentIndex][ptIndex];
  }

  /**
   * Computes the index (order) of the intIndex'th intersection point in the
   * direction of a specified input line segment
   * 
   * @param segmentIndex is 0 or 1
   * @param intIndex is 0 or 1
   * @return the index of the intersection point along the input segment (0 or
   *         1)
   */
  public int getIndexAlongSegment(final int segmentIndex, final int intIndex) {
    computeIntLineIndex();
    return intLineIndex[segmentIndex][intIndex];
  }

  /**
   * Returns the intIndex'th intersection point
   * 
   * @param intIndex is 0 or 1
   * @return the intIndex'th intersection point
   */
  public Coordinates getIntersection(final int intIndex) {
    return intPt[intIndex];
  }

  /**
   * Computes the intIndex'th intersection point in the direction of a specified
   * input line segment
   * 
   * @param segmentIndex is 0 or 1
   * @param intIndex is 0 or 1
   * @return the intIndex'th intersection point in the direction of the
   *         specified input line segment
   */
  public Coordinates getIntersectionAlongSegment(final int segmentIndex,
    final int intIndex) {
    // lazily compute int line array
    computeIntLineIndex();
    return intPt[intLineIndex[segmentIndex][intIndex]];
  }

  /**
   * Returns the number of intersection points found. This will be either 0, 1
   * or 2.
   */
  public int getIntersectionNum() {
    return result;
  }

  private String getTopologySummary() {
    final StringBuffer catBuf = new StringBuffer();
    if (isEndPoint()) {
      catBuf.append(" endpoint");
    }
    if (isProper) {
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
    return result != NO_INTERSECTION;
  }

  protected boolean isCollinear() {
    return result == COLLINEAR_INTERSECTION;
  }

  protected boolean isEndPoint() {
    return hasIntersection() && !isProper;
  }

  /**
   * Tests whether either intersection point is an interior point of one of the
   * input segments.
   * 
   * @return <code>true</code> if either intersection point is in the interior
   *         of one of the input segments
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
   * Tests whether either intersection point is an interior point of the
   * specified input segment.
   * 
   * @return <code>true</code> if either intersection point is in the interior
   *         of the input segment
   */
  public boolean isInteriorIntersection(final int inputLineIndex) {
    for (int i = 0; i < result; i++) {
      if (!(intPt[i].equals2d(inputLines[inputLineIndex][0]) || intPt[i].equals2d(inputLines[inputLineIndex][1]))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Test whether a point is a intersection point of two line segments. Note
   * that if the intersection is a line segment, this method only tests for
   * equality with the endpoints of the intersection segment. It does <b>not</b>
   * return true if the input point is internal to the intersection segment.
   * 
   * @return true if the input point is one of the intersection points.
   */
  public boolean isIntersection(final Coordinates pt) {
    for (int i = 0; i < result; i++) {
      if (intPt[i].equals2d(pt)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests whether an intersection is proper. <br>
   * The intersection between two line segments is considered proper if they
   * intersect in a single point in the interior of both segments (e.g. the
   * intersection is a single point and is not equal to any of the endpoints).
   * <p>
   * The intersection between a point and a line segment is considered proper if
   * the point lies in the interior of the segment (e.g. is not equal to either
   * of the endpoints).
   * 
   * @return true if the intersection is proper
   */
  public boolean isProper() {
    return hasIntersection() && isProper;
  }

  /**
   * Force computed intersection to be rounded to a given precision model. No
   * getter is provided, because the precision model is not required to be
   * specified.
   * 
   * @param precisionModel
   */
  public void setPrecisionModel(final CoordinatesPrecisionModel precisionModel) {
    this.precisionModel = precisionModel;
  }

  @Override
  public String toString() {
    return inputLines[0][0] + "," + inputLines[0][1] + " - " + inputLines[1][0]
      + "," + inputLines[1][1] + " " + getTopologySummary();
  }

}
