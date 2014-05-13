package com.revolsys.jts.geom;

import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.jts.LineSegmentImpl;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.AbstractCoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.HCoordinate;
import com.revolsys.jts.algorithm.NotRepresentableException;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.util.MathUtil;

public abstract class AbstractLineSegment extends AbstractCoordinatesList
  implements LineSegment {

  public AbstractLineSegment() {
    super();
  }

  /**
   * Computes the angle that the vector defined by this segment
   * makes with the X-axis.
   * The angle will be in the range [ -PI, PI ] radians.
   *
   * @return the angle this segment makes with the X-axis (in radians)
   */
  @Override
  public double angle() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    return CoordinatesUtil.angle(x1, y1, x2, y2);
  }

  @Override
  public AbstractLineSegment clone() {
    return (AbstractLineSegment)super.clone();
  }

  /**
   * Computes the closest point on this line segment to another point.
   * @param point the point to find the closest point to
   * @return a Point which is the closest point on the line segment to the point p
   */
  @Override
  public Point closestPoint(final Point point) {
    final Point p0 = getP0();
    final Point p1 = getP1();
    return LineSegmentUtil.closestPoint(p0, p1, point);
  }

  /**
   * Computes the closest points on two line segments.
   * 
   * @param line the segment to find the closest point to
   * @return a pair of Point which are the closest points on the line segments
   */
  @Override
  public Point[] closestPoints(final LineSegment line) {
    // test for intersection
    final Point intPt = intersection(line);
    if (intPt != null) {
      return new Point[] {
        intPt, intPt
      };
    }

    /**
     * if no intersection closest pair contains at least one endpoint. Test each
     * endpoint in turn.
     */
    final Point[] closestPt = new Point[2];
    double minDistance = Double.MAX_VALUE;
    double dist;

    final Point lineStart = line.get(0);
    final Point lineEnd = line.get(1);
    final Point close00 = closestPoint(lineStart);
    minDistance = close00.distance(lineStart);
    closestPt[0] = close00;
    closestPt[1] = lineStart;

    final Point close01 = closestPoint(lineEnd);
    dist = close01.distance(lineEnd);
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = close01;
      closestPt[1] = lineEnd;
    }

    final Point start = get(0);
    final Point close10 = line.closestPoint(start);
    dist = close10.distance(start);
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = start;
      closestPt[1] = close10;
    }

    final Point close11 = line.closestPoint(start);
    dist = close11.distance(start);
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = start;
      closestPt[1] = close11;
    }

    return closestPt;
  }

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
  public int compareTo(final LineSegment other) {
    int compare = get(0).compareTo(other.get(0));
    if (compare == 0) {
      compare = get(1).compareTo(other.get(1));
    }
    return compare;
  }

  @Override
  public boolean contains(final Point coordinate) {
    if (get(0).equals(coordinate)) {
      return true;
    } else if (get(1).equals(coordinate)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public LineSegment convert(final GeometryFactory geometryFactory) {
    final GeometryFactory factory = getGeometryFactory();
    if (geometryFactory == factory) {
      return this;
    } else {
      final Point point1 = ProjectionFactory.convert(get(0), factory,
        geometryFactory);
      final Point point2 = ProjectionFactory.convert(get(1), factory,
        geometryFactory);
      return new com.revolsys.gis.jts.LineSegmentImpl(geometryFactory, point1,
        point2);
    }
  }

  /**
   * Computes the distance between this line segment and a given point.
   *
   * @return the distance from this segment to the given point
   */
  @Override
  public double distance(final Point point) {
    final Point start = get(0);
    final Point end = get(1);
    return LineSegmentUtil.distance(start, end, point);
  }

  @Override
  public double distance(final LineSegment line) {
    final Point start = get(0);
    final Point end = get(1);
    final Point lineStart = line.get(0);
    final Point lineEnd = line.get(1);
    return CGAlgorithms.distanceLineLine(start, end, lineStart, lineEnd);
  }

  /**
   * Computes the perpendicular distance between the (infinite) line defined
   * by this line segment and a point.
   *
   * @return the perpendicular distance between the defined line and the given point
   */
  @Override
  public double distancePerpendicular(final Point p) {
    return CGAlgorithms.distancePointLinePerpendicular(p, getP0(), getP1());
  }

  /**
   *  Returns <code>true</code> if <code>other</code> has the same values for
   *  its points.
   *
   *@param  o  a <code>LineSegmentImpl</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>LineSegmentImpl</code>
   *      with the same values for the x and y ordinates.
   */
  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof LineSegmentImpl)) {
      return false;
    }
    final LineSegment other = (LineSegment)o;
    return getP0().equals(other.getP0()) && getP1().equals(other.getP1());
  }

  /**
   *  Returns <code>true</code> if <code>other</code> is
   *  topologically equal to this LineSegment (e.g. irrespective
   *  of orientation).
   *
   *@param  other  a <code>LineSegmentImpl</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>LineSegmentImpl</code>
   *      with the same values for the x and y ordinates.
   */
  @Override
  public boolean equalsTopo(final LineSegment other) {
    return getP0().equals(other.getP0()) && getP1().equals(other.getP1())
      || getP0().equals(other.getP1()) && getP1().equals(other.getP0());
  }

  public LineSegment extend(final double startDistance, final double endDistance) {
    final double angle = angle();
    final Point c1 = CoordinatesUtil.offset(get(0), angle, -startDistance);
    final Point c2 = CoordinatesUtil.offset(get(1), angle, endDistance);
    return new LineSegmentImpl(getGeometryFactory(), c1, c2);

  }

  @Override
  public int getAxisCount() {
    return 3;
  }

  @Override
  public BoundingBox getBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new Envelope(geometryFactory, this);
  }

  @Override
  public Point getCoordinate(final int i) {
    if (i == 0) {
      return getP0();
    }
    return getP1();
  }

  private Point getCrossing(final Point coordinates1,
    final Point coordinates2, final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    Point intersection = null;
    final Polygon polygon = boundingBox.toPolygon(1);
    final LineString ring = polygon.getExteriorRing();
    final CoordinatesList points = CoordinatesListUtil.get(ring);
    for (int i = 0; i < 4; i++) {
      final Point ringC1 = points.get(i);
      final Point ringC2 = points.get(i);
      final CoordinatesList currentIntersections = LineSegmentUtil.getIntersection(
        geometryFactory, coordinates1, coordinates2, ringC1, ringC2);
      if (currentIntersections.size() == 1) {
        final Point currentIntersection = currentIntersections.get(0);
        if (intersection == null) {
          intersection = currentIntersection;
        } else if (coordinates1.distance(currentIntersection) < coordinates1.distance(intersection)) {
          intersection = currentIntersection;
        }
      }
    }
    return intersection;
  }

  @Override
  public double getElevation(final Point point) {
    return CoordinatesUtil.getElevation(point, get(0), get(1));
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.getFactory();
  }

  @Override
  public LineSegment getIntersection(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.convert(geometryFactory);
    final Point lineStart = get(0);
    final Point lineEnd = get(1);
    final boolean contains1 = boundingBox.covers(lineStart);
    final boolean contains2 = boundingBox.covers(lineEnd);
    if (contains1) {
      if (contains2) {
        return this;
      } else {
        final Point c1 = lineStart;
        final Point c2 = getCrossing(lineEnd, lineStart, boundingBox);
        return new com.revolsys.gis.jts.LineSegmentImpl(geometryFactory, c1, c2);
      }
    } else {
      if (contains2) {
        final Point c1 = getCrossing(lineStart, lineEnd, boundingBox);
        final Point c2 = lineEnd;
        return new com.revolsys.gis.jts.LineSegmentImpl(geometryFactory, c1, c2);
      } else {
        final Point c1 = getCrossing(lineStart, lineEnd, boundingBox);
        final Point c2 = getCrossing(lineEnd, lineStart, boundingBox);
        return new com.revolsys.gis.jts.LineSegmentImpl(geometryFactory, c1, c2);
      }
    }
  }

  @Override
  public CoordinatesList getIntersection(final Point point1,
    final Point point2) {
    final CoordinatesList intersection = LineSegmentUtil.getIntersection(
      getGeometryFactory(), get(0), get(1), point1, point2);
    return intersection;
  }

  @Override
  public CoordinatesList getIntersection(final GeometryFactory precisionModel,
    final LineSegment lineSegment2) {
    return LineSegmentUtil.getIntersection(getGeometryFactory(), get(0),
      get(1), lineSegment2.get(0), lineSegment2.get(1));
  }

  @Override
  public CoordinatesList getIntersection(final LineSegment lineSegment2) {
    final CoordinatesList intersection = LineSegmentUtil.getIntersection(
      getGeometryFactory(), get(0), get(1), lineSegment2.get(0),
      lineSegment2.get(1));
    return intersection;
  }

  /**
   * Computes the length of the line segment.
   * @return the length of the line segment
   */
  @Override
  public double getLength() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    return MathUtil.distance(x1, y1, x2, y2);
  }

  @Override
  public Point getP0() {
    return get(0);
  }

  @Override
  public Point getP1() {
    return get(1);
  }

  @Override
  public Point getPoint(final int i) {
    final Point coordinates = get(i);
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(coordinates);
  }

  public int getSrid() {
    return 0;
  }

  /**
   * Gets a hashcode for this object.
   * 
   * @return a hashcode for this object
   */
  @Override
  public int hashCode() {
    long bits0 = java.lang.Double.doubleToLongBits(getP0().getX());
    bits0 ^= java.lang.Double.doubleToLongBits(getP0().getY()) * 31;
    final int hash0 = (((int)bits0) ^ ((int)(bits0 >> 32)));

    long bits1 = java.lang.Double.doubleToLongBits(getP1().getX());
    bits1 ^= java.lang.Double.doubleToLongBits(getP1().getY()) * 31;
    final int hash1 = (((int)bits1) ^ ((int)(bits1 >> 32)));

    // XOR is supposed to be a good way to combine hashcodes
    return hash0 ^ hash1;
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
  @Override
  public Point intersection(final LineSegment line) {
    final CoordinatesList intersection = getIntersection(line);
    if (intersection.size() == 0) {
      return null;
    } else {
      return intersection.get(0);
    }
  }

  @Override
  public boolean intersects(final BoundingBox boundingBox) {
    final Point p1 = get(0);
    final Point p2 = get(1);
    if (boundingBox.intersects(p1)) {
      return true;
    } else if (boundingBox.intersects(p2)) {
      return true;
    } else {
      final CoordinatesList cornerPoints = boundingBox.getCornerPoints();
      for (int i = 0; i < 4; i++) {
        final Point bp1 = cornerPoints.get(i);
        final Point bp2 = cornerPoints.get((i + 1) % 4);
        if (LineSegmentUtil.intersects(p1, p2, bp1, bp2)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean intersects(final Point point, final double maxDistance) {
    return LineSegmentUtil.isPointOnLine(get(0), get(1), point, maxDistance);
  }

  @Override
  public boolean isEmpty() {
    return get(0) == null || get(1) == null;
  }

  /**
   * Tests whether the segment is horizontal.
   *
   * @return <code>true</code> if the segment is horizontal
   */
  @Override
  public boolean isHorizontal() {
    return getY(0) == getY(1);
  }

  @Override
  public boolean isPointOnLineMiddle(final Point point,
    final double maxDistance) {
    return LineSegmentUtil.isPointOnLineMiddle(get(0), get(1), point,
      maxDistance);
  }

  /**
   * Tests whether the segment is vertical.
   *
   * @return <code>true</code> if the segment is vertical
   */
  @Override
  public boolean isVertical() {
    return getX(0) == getX(1);
  }

  public boolean isWithinDistance(final Point point, final double distance) {
    return distance(point) <= distance;
  }

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
  @Override
  public Point lineIntersection(final LineSegment line) {
    try {
      final Point intPt = HCoordinate.intersection(getP0(), getP1(),
        line.getP0(), line.getP1());
      return intPt;
    } catch (final NotRepresentableException ex) {
      // eat this exception, and return null;
    }
    return null;
  }

  /**
   * Computes the midpoint of the segment
   *
   * @return the midpoint of the segment
   */
  @Override
  public Point midPoint() {
    return LineSegmentUtil.midPoint(getGeometryFactory(), get(0), get(1));
  }

  /**
   * Puts the line segment into a normalized form.
   * This is useful for using line segments in maps and indexes when
   * topological equality rather than exact equality is desired.
   * A segment in normalized form has the first point smaller
   * than the second (according to the standard ordering on {@link Coordinates}).
   */
  @Override
  public LineSegment normalize() {
    if (getP1().compareTo(getP0()) < 0) {
      return new LineSegmentImpl(getP1(), getP0());
    } else {
      return this;
    }
  }

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
  @Override
  public int orientationIndex(final Point p) {
    return CGAlgorithms.orientationIndex(getP0(), getP1(), p);
  }

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
  @Override
  public int orientationIndex(final LineSegment seg) {
    final int orient0 = CoordinatesUtil.orientationIndex(get(0), get(1),
      seg.get(0));
    final int orient1 = CoordinatesUtil.orientationIndex(get(0), get(1),
      seg.get(1));
    // this handles the case where the points are L or collinear
    if (orient0 >= 0 && orient1 >= 0) {
      return Math.max(orient0, orient1);
    }
    // this handles the case where the points are R or collinear
    if (orient0 <= 0 && orient1 <= 0) {
      return Math.max(orient0, orient1);
    }
    // points lie on opposite sides ==> indeterminate orientation
    return 0;
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
  @Override
  public Point pointAlong(final double segmentLengthFraction) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      final double value1 = getValue(0, i);
      final double value2 = getValue(1, i);
      final double delta = value2 - value1;
      double newValue = value1 + delta * segmentLengthFraction;
      newValue = geometryFactory.makePrecise(i, newValue);
      coordinates[i] = newValue;
    }
    return new DoubleCoordinates(coordinates);
  }

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
  @Override
  public Point pointAlongOffset(final double segmentLengthFraction,
    final double offsetDistance) {
    final double x1 = getX(0);
    final double x2 = getX(1);
    final double dx = x2 - x1;

    final double y1 = getY(0);
    final double y2 = getY(1);
    final double dy = y2 - y1;

    // the point on the segment line
    double x = x1 + segmentLengthFraction * (dx);
    double y = y1 + segmentLengthFraction * (dy);

    final double len = Math.sqrt(dx * dx + dy * dy);
    if (offsetDistance != 0.0) {
      if (len <= 0.0) {
        throw new IllegalStateException(
          "Cannot compute offset from zero-length line segment");
      }
      double ux = 0.0;
      double uy = 0.0;

      // u is the vector that is the length of the offset, in the direction of
      // the segment
      ux = offsetDistance * dx / len;
      uy = offsetDistance * dy / len;
      // the offset point is the seg point plus the offset vector rotated 90
      // degrees CCW
      x = x - uy;
      y = y + ux;
    }

    final DoubleCoordinates coord = new DoubleCoordinates(x, y);
    return coord;
  }

  /**
   * Compute the projection of a point onto the line determined
   * by this line segment.
   * <p>
   * Note that the projected point
   * may lie outside the line segment.  If this is the case,
   * the projection factor will lie outside the range [0.0, 1.0].
   */
  @Override
  public Point project(final Point point) {
    final Point lineStart = get(0);
    final Point lineEnd = get(1);
    final Point newPoint = LineSegmentUtil.project(getGeometryFactory(),
      lineStart, lineEnd, point);
    return newPoint;
  }

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
  @Override
  public LineSegment project(final LineSegment seg) {
    final double pf0 = projectionFactor(seg.getP0());
    final double pf1 = projectionFactor(seg.getP1());
    // check if segment projects at all
    if (pf0 >= 1.0 && pf1 >= 1.0) {
      return null;
    }
    if (pf0 <= 0.0 && pf1 <= 0.0) {
      return null;
    }

    Point newp0 = project(seg.getP0());
    if (pf0 < 0.0) {
      newp0 = getP0();
    }
    if (pf0 > 1.0) {
      newp0 = getP1();
    }

    Point newp1 = project(seg.getP1());
    if (pf1 < 0.0) {
      newp1 = getP0();
    }
    if (pf1 > 1.0) {
      newp1 = getP1();
    }

    return new LineSegmentImpl(newp0, newp1);
  }

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
  @Override
  public double projectionFactor(final Point point) {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double x = point.getX();
    final double y = point.getY();
    return LineSegmentUtil.projectionFactor(x1, y1, x2, y2, x, y);
  }

  /**
   * Reverses the direction of the line segment.
   */
  @Override
  public LineSegment reverse() {
    return new LineSegmentImpl(getP1(), getP0());
  }

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
   * @param inputPt the point
   * @return the fraction along the line segment the projection of the point occurs
   */
  @Override
  public double segmentFraction(final Point inputPt) {
    double segFrac = projectionFactor(inputPt);
    if (segFrac < 0.0) {
      segFrac = 0.0;
    } else if (segFrac > 1.0 || Double.isNaN(segFrac)) {
      segFrac = 1.0;
    }
    return segFrac;
  }

  @Override
  public int size() {
    return 2;
  }

  /**
   * Creates a LineString with the same coordinates as this segment
   * 
   * @param geomFactory the geometery factory to use
   * @return a LineString with the same geometry as this segment
   */
  @Override
  public LineString toLineString() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.lineString(this);
  }

  @Override
  public String toString() {
    final int srid = getSrid();
    if (srid == 0) {
      return super.toString();
    } else {
      return "SRID=" + srid + ";" + super.toString();
    }
  }

  @Override
  public boolean touchesEnd(final LineSegment closestSegment) {
    if (contains(closestSegment.get(0))) {
      return true;
    } else if (contains(closestSegment.get(1))) {
      return true;
    }
    return false;
  }

}
