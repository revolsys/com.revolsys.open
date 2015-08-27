package com.revolsys.geometry.model.segment;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;

public interface LineSegment extends LineString {

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

  double distance(double x, double y);

  /**
   * Computes the distance between this line segment and another segment.
   *
   * @return the distance to the other segment
   */
  double distance(LineSegment ls);

  /**
   * Computes the distance between this line segment and a given point.
   *
   * @return the distance from this segment to the given point
   */
  @Override
  double distance(Point p);

  double distanceAlong(final double x, final double y);

  @Override
  double distanceAlong(final Point point);

  /**
   * Computes the perpendicular distance between the (infinite) line defined
   * by this line segment and a point.
   *
   * @return the perpendicular distance between the defined line and the given point
   */
  double distancePerpendicular(Point p);

  boolean equals(LineSegment segment);

  /**
   *  Returns <code>true</code> if <code>other</code> is
   *  topologically equal to this LineSegment (e.g. irrespective
   *  of orientation).
   *
   *@param  other  a <code>LineSegmentDouble</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>LineSegmentDouble</code>
   *      with the same values for the x and y ordinates.
   */
  boolean equalsTopo(LineSegment other);

  @Override
  int getAxisCount();

  @Override
  BoundingBox getBoundingBox();

  @Override
  double getCoordinate(int vertexIndex, int axisIndex);

  double getElevation(Point point);

  @Override
  GeometryFactory getGeometryFactory();

  LineSegment getIntersection(BoundingBox boundingBox);

  Geometry getIntersection(LineSegment lineSegment2);

  Point getP0();

  Point getP1();

  @Override
  default Side getSide(final Point point) {
    final int orientationIndex = orientationIndex(point);
    switch (orientationIndex) {
      case 1:
        return Side.LEFT;
      case -1:
        return Side.RIGHT;

      default:
        return null;
    }
  }

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

  boolean intersects(Point point, double maxDistance);

  boolean isEndPoint(Point point);

  /**
   * Tests whether the segment is horizontal.
   *
   * @return <code>true</code> if the segment is horizontal
   */
  boolean isHorizontal();

  boolean isPerpendicularTo(Point point);

  boolean isPointOnLineMiddle(Point point, final double maxDistance);

  /**
   * Tests whether the segment is vertical.
   *
   * @return <code>true</code> if the segment is vertical
   */
  boolean isVertical();

  boolean isZeroLength();

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

  @Override
  LineSegment normalize();

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
  default int orientationIndex(final Point p) {
    /**
     * MD - 9 Aug 2010 It seems that the basic algorithm is slightly orientation
     * dependent, when computing the orientation of a point very close to a
     * line. This is possibly due to the arithmetic in the translation to the
     * origin.
     *
     * For instance, the following situation produces identical results in spite
     * of the inverse orientation of the line segment:
     *
     * Point p0 = new PointDouble((double)219.3649559090992, 140.84159161824724);
     * Point p1 = new PointDouble((double)168.9018919682399, -5.713787599646864);
     *
     * Point p = new PointDouble((double)186.80814046338352, 46.28973405831556); int
     * orient = orientationIndex(p0, p1, p); int orientInv =
     * orientationIndex(p1, p0, p);
     *
     * A way to force consistent results is to normalize the orientation of the
     * vector using the following code. However, this may make the results of
     * orientationIndex inconsistent through the triangle of points, so it's not
     * clear this is an appropriate patch.
     *
     */
    return CGAlgorithmsDD.orientationIndex(getP0(), getP1(), p);
  }

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
  Point pointAlongOffset(double segmentLengthFraction, double offsetDistance);

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
   * Compute the projection of a point onto the line determined
   * by this line segment.
   * <p>
   * Note that the projected point
   * may lie outside the line segment.  If this is the case,
   * the projection factor will lie outside the range [0.0, 1.0].
   */
  Point project(Point p);

  double projectCoordinate(int axisIndex, double projectionFactor);

  /**
   * Computes the Projection Factor for the projection of the point p
   * onto this LineSegmentDouble.  The Projection Factor is the constant r
   * by which the vector for this segment must be multiplied to
   * equal the vector for the projection of <tt>p<//t> on the line
   * defined by this segment.
   * <p>
   * The projection factor will lie in the range <tt>(-inf, +inf)</tt>,
   * or be <code>NaN</code> if the line segment has zero length..
   *
   * @param x the point x coordinate to compute the factor for
   * @param y the point y coordinate to compute the factor for
   * @return the projection factor for the point
   */
  default double projectionFactor(final double x, final double y) {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    return LineSegmentUtil.projectionFactor(x1, y1, x2, y2, x, y);
  }

  /**
   * Computes the Projection Factor for the projection of the point p
   * onto this LineSegmentDouble.  The Projection Factor is the constant r
   * by which the vector for this segment must be multiplied to
   * equal the vector for the projection of <tt>p<//t> on the line
   * defined by this segment.
   * <p>
   * The projection factor will lie in the range <tt>(-inf, +inf)</tt>,
   * or be <code>NaN</code> if the line segment has zero length..
   *
   * @param point the point to compute the factor for
   * @return the projection factor for the point
   */
  default double projectionFactor(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return projectionFactor(x, y);
  }

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

  boolean touchesEnd(LineSegment closestSegment);

}
