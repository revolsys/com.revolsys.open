package com.revolsys.jts.geom;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.RobustLineIntersector;

public interface LineSegment extends Comparable<LineSegment>, CoordinatesList {

  /**
   * Computes the angle that the vector defined by this segment
   * makes with the X-axis.
   * The angle will be in the range [ -PI, PI ] radians.
   *
   * @return the angle this segment makes with the X-axis (in radians)
   */
  double angle();

  /**
   * Computes the closest point on this line segment to another point.
   * @param p the point to find the closest point to
   * @return a Point which is the closest point on the line segment to the point p
   */
  Point closestPoint(Point p);

  /**
   * Computes the closest points on two line segments.
   * 
   * @param line the segment to find the closest point to
   * @return a pair of Point which are the closest points on the line segments
   */
  Point[] closestPoints(LineSegment line);

  /**
   *  Compares this object with the specified object for order.
   *  Uses the standard lexicographic ordering for the points in the LineSegmentImpl.
   *
   *@param  o  the <code>LineSegmentImpl</code> with which this <code>LineSegmentImpl</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer as this <code>LineSegmentImpl</code>
   *      is less than, equal to, or greater than the specified <code>LineSegmentImpl</code>
   */
  @Override
  int compareTo(LineSegment o);

  LineSegment convert(GeometryFactory geometryFactory);

  /**
   * Computes the distance between this line segment and a given point.
   *
   * @return the distance from this segment to the given point
   */
  double distance(Point p);

  /**
   * Computes the distance between this line segment and another segment.
   *
   * @return the distance to the other segment
   */
  double distance(LineSegment ls);

  /**
   * Computes the perpendicular distance between the (infinite) line defined
   * by this line segment and a point.
   *
   * @return the perpendicular distance between the defined line and the given point
   */
  double distancePerpendicular(Point p);

  /**
   *  Returns <code>true</code> if <code>other</code> is
   *  topologically equal to this LineSegment (e.g. irrespective
   *  of orientation).
   *
   *@param  other  a <code>LineSegmentImpl</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>LineSegmentImpl</code>
   *      with the same values for the x and y ordinates.
   */
  boolean equalsTopo(LineSegment other);

  BoundingBox getBoundingBox();

  @Override
  Point getCoordinate(int i);

  double getElevation(Point point);

  GeometryFactory getGeometryFactory();

  LineSegment getIntersection(BoundingBox boundingBox);

  CoordinatesList getIntersection(final Point point1,
    final Point point2);

  CoordinatesList getIntersection(final GeometryFactory precisionModel,
    final LineSegment lineSegment2);

  CoordinatesList getIntersection(LineSegment lineSegment2);

  /**
   * Computes the length of the line segment.
   * @return the length of the line segment
   */
  double getLength();

  Point getP0();

  Point getP1();

  Point getPoint(int vertexIndex);

  /**
   * Computes an intersection point between two line segments, if there is one.
   * There may be 0, 1 or many intersection points between two segments.
   * If there are 0, null is returned. If there is 1 or more, 
   * exactly one of them is returned 
   * (chosen at the discretion of the algorithm).  
   * If more information is required about the details of the intersection,
   * the {@link RobustLineIntersector} class should be used.
   *
   * @param line a line segment
   * @return an intersection point, or <code>null</code> if there is none
   * 
   * @see RobustLineIntersector
   */
  Point intersection(LineSegment line);

  boolean intersects(BoundingBox boundingBox);

  boolean intersects(Point point, double maxDistance);

  boolean isEmpty();

  /**
   * Tests whether the segment is horizontal.
   *
   * @return <code>true</code> if the segment is horizontal
   */
  boolean isHorizontal();

  boolean isPointOnLineMiddle(Point point, final double maxDistance);

  /**
   * Tests whether the segment is vertical.
   *
   * @return <code>true</code> if the segment is vertical
   */
  boolean isVertical();

  /**
   * Computes the intersection point of the lines of infinite extent defined
   * by two line segments (if there is one).
   * There may be 0, 1 or an infinite number of intersection points 
   * between two lines.
   * If there is a unique intersection point, it is returned. 
   * Otherwise, <tt>null</tt> is returned.
   * If more information is required about the details of the intersection,
   * the {@link RobustLineIntersector} class should be used.
   *
   * @param line a line segment defining an straight line with infinite extent
   * @return an intersection point, 
   * or <code>null</code> if there is no point of intersection
   * or an infinite number of intersection points
   * 
   * @see RobustLineIntersector
   */
  Point lineIntersection(LineSegment line);

  /**
   * Computes the midpoint of the segment
   *
   * @return the midpoint of the segment
   */
  Point midPoint();

  /**
   * Puts the line segment into a normalized form.
   * This is useful for using line segments in maps and indexes when
   * topological equality rather than exact equality is desired.
   * A segment in normalized form has the first point smaller
   * than the second (according to the standard ordering on {@link Coordinates}).
   */

  LineSegment normalize();

  /**
   * Determines the orientation index of a {@link Coordinates} relative to this segment.
   * The orientation index is as defined in {@link CGAlgorithms#computeOrientation}.
   *
   * @param p the coordinate to compare
   *
   * @return 1 (LEFT) if <code>p</code> is to the left of this segment
   * @return -1 (RIGHT) if <code>p</code> is to the right of this segment
   * @return 0 (COLLINEAR) if <code>p</code> is collinear with this segment
   * 
   * @see CGAlgorithms#computeOrientation(Coordinate, Coordinate, Coordinate)
   */
  int orientationIndex(Point p);

  /**
   * Determines the orientation of a LineSegment relative to this segment.
   * The concept of orientation is specified as follows:
   * Given two line segments A and L,
   * <ul
   * <li>A is to the left of a segment L if A lies wholly in the
   * closed half-plane lying to the left of L
   * <li>A is to the right of a segment L if A lies wholly in the
   * closed half-plane lying to the right of L
   * <li>otherwise, A has indeterminate orientation relative to L. This
   * happens if A is collinear with L or if A crosses the line determined by L.
   * </ul>
   *
   * @param seg the LineSegment to compare
   *
   * @return 1 if <code>seg</code> is to the left of this segment
   * @return -1 if <code>seg</code> is to the right of this segment
   * @return 0 if <code>seg</code> has indeterminate orientation relative to this segment
   */
  int orientationIndex(LineSegment seg);

  /**
   * Computes the {@link Coordinates} that lies a given
   * fraction along the line defined by this segment.
   * A fraction of <code>0.0</code> returns the start point of the segment;
   * a fraction of <code>1.0</code> returns the end point of the segment.
   * If the fraction is < 0.0 or > 1.0 the point returned 
   * will lie before the start or beyond the end of the segment. 
   *
   * @param segmentLengthFraction the fraction of the segment length along the line
   * @return the point at that distance
   */
  Point pointAlong(double segmentLengthFraction);

  /**
   * Computes the {@link Coordinates} that lies a given
   * fraction along the line defined by this segment and offset from 
   * the segment by a given distance.
   * A fraction of <code>0.0</code> offsets from the start point of the segment;
   * a fraction of <code>1.0</code> offsets from the end point of the segment.
   * The computed point is offset to the left of the line if the offset distance is
   * positive, to the right if negative.
   *
   * @param segmentLengthFraction the fraction of the segment length along the line
   * @param offsetDistance the distance the point is offset from the segment
   *    (positive is to the left, negative is to the right)
   * @return the point at that distance and offset
   * 
   * @throws IllegalStateException if the segment has zero length
   */
  Point pointAlongOffset(double segmentLengthFraction,
    double offsetDistance);

  /**
   * Compute the projection of a point onto the line determined
   * by this line segment.
   * <p>
   * Note that the projected point
   * may lie outside the line segment.  If this is the case,
   * the projection factor will lie outside the range [0.0, 1.0].
   */
  Point project(Point p);

  /**
   * Project a line segment onto this line segment and return the resulting
   * line segment.  The returned line segment will be a subset of
   * the target line line segment.  This subset may be null, if
   * the segments are oriented in such a way that there is no projection.
   * <p>
   * Note that the returned line may have zero length (i.e. the same endpoints).
   * This can happen for instance if the lines are perpendicular to one another.
   *
   * @param seg the line segment to project
   * @return the projected line segment, or <code>null</code> if there is no overlap
   */
  LineSegment project(LineSegment seg);

  /**
   * Computes the Projection Factor for the projection of the point p
   * onto this LineSegmentImpl.  The Projection Factor is the constant r
   * by which the vector for this segment must be multiplied to
   * equal the vector for the projection of <tt>p<//t> on the line
   * defined by this segment.
   * <p>
   * The projection factor will lie in the range <tt>(-inf, +inf)</tt>,
   * or be <code>NaN</code> if the line segment has zero length..
   * 
   * @param p the point to compute the factor for
   * @return the projection factor for the point
   */
  double projectionFactor(Point p);

  /**
   * Reverses the direction of the line segment.
   */
  @Override
  LineSegment reverse();

  /**
   * Computes the fraction of distance (in <tt>[0.0, 1.0]</tt>) 
   * that the projection of a point occurs along this line segment.
   * If the point is beyond either ends of the line segment,
   * the closest fractional value (<tt>0.0</tt> or <tt>1.0</tt>) is returned.
   * <p>
   * Essentially, this is the {@link #projectionFactor} clamped to 
   * the range <tt>[0.0, 1.0]</tt>.
   * If the segment has zero length, 1.0 is returned.
   *  
   * @param point the point
   * @return the fraction along the line segment the projection of the point occurs
   */
  double segmentFraction(Point point);

  /**
   * Creates a LineString with the same coordinates as this segment
   * 
   * @param geomFactory the geometery factory to use
   * @return a LineString with the same geometry as this segment
   */
  LineString toLineString();

  boolean touchesEnd(LineSegment closestSegment);

}
