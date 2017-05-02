package com.revolsys.geometry.model.segment;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.CGAlgorithmsDD;
import com.revolsys.geometry.algorithm.HCoordinate;
import com.revolsys.geometry.algorithm.NotRepresentableException;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.cs.projection.ProjectionFactory;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.util.BoundingBoxUtil;
import com.revolsys.math.Angle;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;
import com.revolsys.util.number.Doubles;

public interface LineSegment extends LineString {
  public static int addEndPointIntersection(final double[] coordinates, final int intersectionCount,
    final int axisCount, final LineSegment segment1, final int vertexIndex1,
    final LineSegment segment2, final int vertexIndex2) {
    final double x = segment1.getX(vertexIndex1);
    final double y = segment1.getY(vertexIndex1);
    if (!CoordinatesListUtil.containsXy(coordinates, intersectionCount, axisCount, x, y)) {
      coordinates[intersectionCount * axisCount] = x;
      coordinates[intersectionCount * axisCount + 1] = y;
      for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
        double value = segment1.getCoordinate(vertexIndex1, axisIndex);
        if (Double.isNaN(value)) {
          value = segment2.getCoordinate(vertexIndex2, axisIndex);
        }
        coordinates[intersectionCount * axisCount + axisIndex] = value;
      }
      return intersectionCount + 1;
    }
    return intersectionCount;
  }

  public static int addEndPointIntersectionProjected(final double[] coordinates,
    final int intersectionCount, final int axisCount, final LineSegment segment1,
    final int vertexIndex1, final LineSegment segment2, final double projectionFactor) {
    final double x = segment1.getX(vertexIndex1);
    final double y = segment1.getY(vertexIndex1);
    if (!CoordinatesListUtil.containsXy(coordinates, intersectionCount, axisCount, x, y)) {
      coordinates[intersectionCount * axisCount] = x;
      coordinates[intersectionCount * axisCount + 1] = y;
      for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
        double value = segment1.getCoordinate(vertexIndex1, axisIndex);
        if (Double.isNaN(value)) {
          value = segment2.projectCoordinate(axisIndex, projectionFactor);
        }
        coordinates[intersectionCount * axisCount + axisIndex] = value;
      }
    }
    return intersectionCount + 1;
  }

  public static int addPointIntersection(final double[] coordinates, final int intersectionCount,
    final int axisCount, final LineSegment segment1, final int vertexIndex,
    final LineSegment segment2) {
    final double x = segment1.getCoordinate(vertexIndex, 0);
    final double y = segment1.getCoordinate(vertexIndex, 1);
    if (segment2.equalsVertex(0, x, y)) {
      return addEndPointIntersection(coordinates, intersectionCount, axisCount, segment1,
        vertexIndex, segment2, 0);
    } else if (segment2.equalsVertex(1, x, y)) {
      return addEndPointIntersection(coordinates, intersectionCount, axisCount, segment1,
        vertexIndex, segment2, 1);
    } else {
      final double distance = segment2.distance(x, y);
      final double maxDistance = segment1.getGeometryFactory().getResolution(0);
      if (distance < maxDistance) {
        final double x1 = segment2.getX(0);
        final double y1 = segment2.getY(0);
        final double x2 = segment2.getX(1);
        final double y2 = segment2.getY(1);
        final double projectionFactor = LineSegmentUtil.projectionFactor(x1, y1, x2, y2, x, y);
        if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
          return addEndPointIntersectionProjected(coordinates, intersectionCount, axisCount,
            segment1, vertexIndex, segment2, projectionFactor);
        }
      }
    }
    return intersectionCount;
  }

  /**
   * Computes the angle that the vector defined by this segment
   * makes with the X-axis.
   * The angle will be in the range [ -PI, PI ] radians.
   *
   * @return the angle this segment makes with the X-axis (in radians)
   */
  default double angle() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    return Angle.angle2d(x1, x2, y1, y2);
  }

  /**
   * Computes the closest point on this line segment to another point.
   * @param point the point to find the closest point to
   * @return a Point which is the closest point on the line segment to the point p
   */
  default Point closestPoint(final Point point) {
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
  default Point[] closestPoints(final LineSegment line) {
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

    final Point lineStart = line.getPoint(0);
    final Point lineEnd = line.getPoint(1);
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

    final Point start = getPoint(0);
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

  default int compareTo(final int index, final LineSegment lineSegment) {
    final double x1 = getX(index);
    final double y1 = getY(index);
    final double x2 = lineSegment.getX(index);
    final double y2 = lineSegment.getY(index);
    return CoordinatesUtil.compare(x1, y1, x2, y2);
  }

  /**
   *  Compares this object with the specified object for order.
   *  Uses the standard lexicographic ordering for the points in the LineSegmentDouble.
   *
   *@param  o  the <code>LineSegmentDouble</code> with which this <code>LineSegmentDouble</code>
   *      is being compared
   *@return    a negative integer, zero, or a positive integer as this <code>LineSegmentDouble</code>
   *      is less than, equal to, or greater than the specified <code>LineSegmentDouble</code>
   */
  @Override
  default int compareTo(final Object other) {
    if (other instanceof LineSegment) {
      final LineSegment segment = (LineSegment)other;
      int compare = compareTo(0, segment);
      if (compare == 0) {
        compare = compareTo(1, segment);
      }
      return compare;

    } else {
      return LineString.super.compareTo(other);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V convertGeometry(final GeometryFactory geometryFactory) {
    final GeometryFactory factory = getGeometryFactory();
    if (geometryFactory == factory) {
      return (V)this;
    } else {
      final Point point1 = ProjectionFactory.convert(getPoint(0), factory, geometryFactory);
      final Point point2 = ProjectionFactory.convert(getPoint(1), factory, geometryFactory);
      return (V)new LineSegmentDoubleGF(geometryFactory, point1, point2);
    }
  }

  default double distance(final double x, final double y) {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    return LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
  }

  default double distance(final LineSegment line) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineString convertedLine = line.convertGeometry(geometryFactory, 2);
    final double line1X1 = getX(0);
    final double line1Y1 = getY(0);
    final double line1X2 = getX(1);
    final double line1Y2 = getY(1);
    final double line2X1 = convertedLine.getX(0);
    final double line2Y1 = convertedLine.getY(0);
    final double line2X2 = convertedLine.getX(1);
    final double line2Y2 = convertedLine.getY(1);
    return LineSegmentUtil.distanceLineLine(line1X1, line1Y1, line1X2, line1Y2, line2X1, line2Y1,
      line2X2, line2Y2);
  }

  /**
   * Computes the distance between this line segment and a given point.
   *
   * @return the distance from this segment to the given point
   */
  @Override
  default double distance(Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convertGeometry(geometryFactory, 2);
    final double x = point.getX();
    final double y = point.getY();
    return distance(x, y);
  }

  /**
   * Computes the distance between this line segment and a given point.
   *
   * @return the distance from this segment to the given point
   */
  @Override
  default double distance(Point point, final double terminateDistance) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convertGeometry(geometryFactory, 2);
    final double x = point.getX();
    final double y = point.getY();
    return distance(x, y);
  }

  default double distanceAlong(final double x, final double y) {
    final double projectionFactor = projectionFactor(x, y);
    if (projectionFactor >= 0 && projectionFactor <= 1) {
      return getLength() * projectionFactor;
    } else {
      return 0;
    }
  }

  @Override
  default double distanceAlong(final Point point) {
    return distanceAlong(point.getX(), point.getY());
  }

  /**
   * Computes the perpendicular distance between the (infinite) line defined
   * by this line segment and a point.
   *
   * @return the perpendicular distance between the defined line and the given point
   */
  default double distancePerpendicular(final Point p) {
    return CGAlgorithms.distancePointLinePerpendicular(p, getP0(), getP1());
  }

  default boolean equals(final LineSegment segment) {
    if (isEmpty()) {
      return false;
    } else if (segment.isEmpty()) {
      return false;
    } else {
      if (equalsVertex(2, 0, segment, 0)) {
        if (equalsVertex(2, 1, segment, 1)) {
          return true;
        }
      } else if (equalsVertex(2, 0, segment, 1)) {
        if (equalsVertex(2, 1, segment, 0)) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   *  Returns <code>true</code> if <code>other</code> is
   *  topologically equal to this LineSegment (e.g. irrespective
   *  of orientation).
   *
   *@param  other  a <code>LineSegment</code> with which to do the comparison.
   *@return        <code>true</code> if <code>other</code> is a <code>LineSegment</code>
   *      with the same values for the x and y ordinates in forwards or reverse order.
   */
  default boolean equalsTopo(final LineSegment other) {
    return equals(other);
  }

  default LineSegment extend(final double startDistance, final double endDistance) {
    final double angle = angle();
    final Point c1 = CoordinatesUtil.offset(getPoint(0), angle, -startDistance);
    final Point c2 = CoordinatesUtil.offset(getPoint(1), angle, endDistance);
    return new LineSegmentDoubleGF(getGeometryFactory(), c1, c2);

  }

  @Override
  default int getAxisCount() {
    return 3;
  }

  default Point getCrossing(final Point point1, final Point point2, final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    Point intersection = null;
    final Polygon polygon = boundingBox.toPolygon(1);
    final LineString ring = polygon.getShell();
    for (int i = 0; i < 4; i++) {
      final Point ringC1 = ring.getPoint(i);
      final Point ringC2 = ring.getPoint(i);
      final LineString currentIntersections = LineSegmentUtil.getIntersection(geometryFactory,
        point1, point2, ringC1, ringC2);
      if (currentIntersections.getVertexCount() == 1) {
        final Point currentIntersection = currentIntersections.getPoint(0);
        if (intersection == null) {
          intersection = currentIntersection;
        } else if (point1.distance(currentIntersection) < point1.distance(intersection)) {
          intersection = currentIntersection;
        }
      }
    }
    return intersection;
  }

  default double getElevation(final Point point) {
    return CoordinatesUtil.getElevation(point, getPoint(0), getPoint(1));
  }

  default LineSegment getIntersection(BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    boundingBox = boundingBox.convert(geometryFactory);
    final Point lineStart = getPoint(0);
    final Point lineEnd = getPoint(1);
    final boolean contains1 = boundingBox.covers(lineStart);
    final boolean contains2 = boundingBox.covers(lineEnd);
    if (contains1) {
      if (contains2) {
        return this;
      } else {
        final Point c1 = lineStart;
        final Point c2 = getCrossing(lineEnd, lineStart, boundingBox);
        return new LineSegmentDoubleGF(geometryFactory, c1, c2);
      }
    } else {
      if (contains2) {
        final Point c1 = getCrossing(lineStart, lineEnd, boundingBox);
        final Point c2 = lineEnd;
        return new LineSegmentDoubleGF(geometryFactory, c1, c2);
      } else {
        final Point c1 = getCrossing(lineStart, lineEnd, boundingBox);
        final Point c2 = getCrossing(lineEnd, lineStart, boundingBox);
        return new LineSegmentDoubleGF(geometryFactory, c1, c2);
      }
    }
  }

  default Geometry getIntersection(final LineSegment lineSegment2) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    lineSegment2.convertGeometry(geometryFactory);
    final double line1x1 = getX(0);
    final double line1y1 = getY(0);
    final double line1x2 = getX(1);
    final double line1y2 = getY(1);

    final double line2x1 = lineSegment2.getX(0);
    final double line2y1 = lineSegment2.getY(0);
    final double line2x2 = lineSegment2.getX(1);
    final double line2y2 = lineSegment2.getY(1);
    if (BoundingBoxUtil.intersectsMinMax(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
      line2x2, line2y2)) {
      int intersectionCount = 0;
      final int axisCount = geometryFactory.getAxisCount();
      final double[] coordinates = new double[2 * axisCount];
      for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
        intersectionCount = addPointIntersection(coordinates, intersectionCount, axisCount, this,
          vertexIndex, lineSegment2);
      }
      for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
        intersectionCount = addPointIntersection(coordinates, intersectionCount, axisCount,
          lineSegment2, vertexIndex, this);
      }

      if (intersectionCount == 0) {
        final int Pq1 = CoordinatesListUtil.orientationIndex(line1x1, line1y1, line1x2, line1y2,
          line2x1, line2y1);
        final int Pq2 = CoordinatesListUtil.orientationIndex(line1x1, line1y1, line1x2, line1y2,
          line2x2, line2y2);

        if (!(Pq1 > 0 && Pq2 > 0 || Pq1 < 0 && Pq2 < 0)) {
          final int Qp1 = CoordinatesListUtil.orientationIndex(line2x1, line2y1, line2x2, line2y2,
            line1x1, line1y1);
          final int Qp2 = CoordinatesListUtil.orientationIndex(line2x1, line2y1, line2x2, line2y2,
            line1x2, line1y2);

          if (!(Qp1 > 0 && Qp2 > 0 || Qp1 < 0 && Qp2 < 0)) {
            final double detLine1StartLine1End = LineSegmentUtil.det(line1x1, line1y1, line1x2,
              line1y2);
            final double detLine2StartLine2End = LineSegmentUtil.det(line2x1, line2y1, line2x2,
              line2y2);
            double x = LineSegmentUtil.det(detLine1StartLine1End, line1x1 - line1x2,
              detLine2StartLine2End, line2x1 - line2x2)
              / LineSegmentUtil.det(line1x1 - line1x2, line1y1 - line1y2, line2x1 - line2x2,
                line2y1 - line2y2);
            x = geometryFactory.makePrecise(0, x);
            double y = LineSegmentUtil.det(detLine1StartLine1End, line1y1 - line1y2,
              detLine2StartLine2End, line2y1 - line2y2)
              / LineSegmentUtil.det(line1x1 - line1x2, line1y1 - line1y2, line2x1 - line2x2,
                line2y1 - line2y2);
            y = geometryFactory.makePrecise(1, y);
            coordinates[0] = x;
            coordinates[1] = y;
            boolean hasNaN = false;
            double projectionFactor = projectionFactor(x, y);
            for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
              final double value = projectCoordinate(axisIndex, projectionFactor);
              coordinates[axisIndex] = value;
              hasNaN |= Double.isNaN(value);
            }
            if (hasNaN) {
              projectionFactor = lineSegment2.projectionFactor(x, y);
              for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
                double value = coordinates[axisIndex];
                if (Double.isNaN(value)) {
                  value = lineSegment2.projectCoordinate(axisIndex, projectionFactor);
                  coordinates[axisIndex] = value;
                  hasNaN |= Double.isNaN(value);
                }
              }
            }
            return geometryFactory.point(coordinates);
          }
        }
      } else if (intersectionCount == 1) {
        return geometryFactory.point(coordinates);
      } else if (intersectionCount == 2) {
        final double distance1 = MathUtil.distance(line1x1, line1y1, coordinates[0],
          coordinates[1]);
        final double distance2 = MathUtil.distance(line1x1, line1y1, coordinates[axisCount],
          coordinates[axisCount + 1]);
        if (distance1 > distance2) {
          CoordinatesListUtil.switchCoordinates(coordinates, axisCount, 0, 1);
        }
        return new LineSegmentDoubleGF(geometryFactory, axisCount, coordinates);
      }
    }
    return null;
  }

  /**
   * Computes the length of the line segment.
   * @return the length of the line segment
   */
  @Override
  default double getLength() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    return MathUtil.distance(x1, y1, x2, y2);
  }

  default double getOrientaton() {
    if (isEmpty()) {
      return 0;
    } else {
      final double x1 = getX(0);
      final double y1 = getY(0);
      final double x2 = getX(1);
      final double y2 = getY(1);
      final double angle = Angle.angleDegrees(x1, y1, x2, y2);
      if (Double.isNaN(angle)) {
        return 0;
      } else {
        return angle;
      }
    }
  }

  default Point getP0() {
    return getPoint(0);
  }

  default Point getP1() {
    return getPoint(1);
  }

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

  @Override
  default int getVertexCount() {
    return 2;
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
  default Point intersection(final LineSegment line) {
    final Geometry intersection = getIntersection(line);
    if (intersection == null) {
      return null;
    } else {
      return intersection.getPoint();
    }
  }

  default boolean intersects(final Point point, final double maxDistance) {
    return LineSegmentUtil.isPointOnLine(getPoint(0), getPoint(1), point, maxDistance);
  }

  @Override
  default boolean isEmpty() {
    return Double.isNaN(getCoordinate(0, 1));
  }

  default boolean isEndPoint(final Point point) {
    if (equalsVertex(2, 0, point)) {
      return true;
    } else if (equalsVertex(2, -1, point)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Tests whether the segment is horizontal.
   *
   * @return <code>true</code> if the segment is horizontal
   */
  default boolean isHorizontal() {
    return getY(0) == getY(1);
  }

  default boolean isPerpendicularTo(Point point) {
    if (Property.hasValuesAll(point, this)) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertGeometry(geometryFactory, 2);
      final double x = point.getX();
      final double y = point.getY();
      final double projectionFactor = projectionFactor(x, y);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        return true;
      }
    }
    return false;
  }

  default boolean isPointOnLineMiddle(final double x, final double y, final double maxDistance) {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    if (Doubles.equal(x, x1) && Doubles.equal(y, y1)) {
      return false;
    } else if (Doubles.equal(x, x2) && Doubles.equal(y, y2)) {
      return false;
    } else {
      final double distance = LineSegmentUtil.distanceLinePoint(x1, y1, x2, y2, x, y);
      if (distance < maxDistance) {
        final double projectionFactor = LineSegmentUtil.projectionFactor(x1, y1, x2, y2, x, y);
        if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
          return true;
        }
      }
      return false;
    }
  }

  default boolean isPointOnLineMiddle(Point point, final double maxDistance) {
    if (point == null || point.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertGeometry(geometryFactory, 2);
      final double x = point.getX();
      final double y = point.getY();
      return isPointOnLineMiddle(x, y, maxDistance);
    }
  }

  /**
   * Tests whether the segment is vertical.
   *
   * @return <code>true</code> if the segment is vertical
   */
  default boolean isVertical() {
    return getX(0) == getX(1);
  }

  default boolean isWithinDistance(final Point point, final double distance) {
    return distance(point) <= distance;
  }

  default boolean isZeroLength() {
    return equalsVertex(2, 0, 1);
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
  default Point lineIntersection(final LineSegment line) {
    try {
      final Point intPt = HCoordinate.intersection(getP0(), getP1(), line.getP0(), line.getP1());
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
  default Point midPoint() {
    return LineSegmentUtil.midPoint(getGeometryFactory(), getPoint(0), getPoint(1));
  }

  default LineSegment newLineSegment(final int axisCount, final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return new LineSegmentDoubleGF(geometryFactory, axisCount, coordinates);
  }

  default Point newPoint(final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.point(coordinates);
  }

  /**
   * Puts the line segment into a normalized form.
   * This is useful for using line segments in maps and indexes when
   * topological equality rather than exact equality is desired.
   * A segment in normalized form has the first point smaller
   * than the second (according to the standard ordering on {@link Coordinates}).
   */
  @Override
  default LineSegment normalize() {
    final Point p1 = getP1();
    final Point p0 = getP0();
    if (p1.compareTo(p0) < 0) {
      return reverse();
    } else {
      return this;
    }
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
  default int orientationIndex(final LineSegment seg) {
    final int orient0 = CoordinatesUtil.orientationIndex(getPoint(0), getPoint(1), seg.getPoint(0));
    final int orient1 = CoordinatesUtil.orientationIndex(getPoint(0), getPoint(1), seg.getPoint(1));
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
  default Point pointAlong(final double segmentLengthFraction) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      final double value1 = getCoordinate(0, i);
      final double value2 = getCoordinate(1, i);
      final double delta = value2 - value1;
      double newValue = value1 + delta * segmentLengthFraction;
      newValue = geometryFactory.makePrecise(i, newValue);
      coordinates[i] = newValue;
    }
    return newPoint(coordinates);
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
  default Point pointAlongOffset(final double segmentLengthFraction, final double offsetDistance) {
    final double x1 = getX(0);
    final double x2 = getX(1);
    final double dx = x2 - x1;

    final double y1 = getY(0);
    final double y2 = getY(1);
    final double dy = y2 - y1;

    // the point on the segment line
    double x = x1 + segmentLengthFraction * dx;
    double y = y1 + segmentLengthFraction * dy;

    final double len = Math.sqrt(dx * dx + dy * dy);
    if (offsetDistance != 0.0) {
      if (len <= 0.0) {
        throw new IllegalStateException("Cannot compute offset from zero-length line segment");
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

    return newPoint(x, y);
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
  default LineSegment project(final LineSegment seg) {
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

    return new LineSegmentDoubleGF(newp0, newp1);
  }

  /**
   * Compute the projection of a point onto the line determined
   * by this line segment.
   * <p>
   * Note that the projected point
   * may lie outside the line segment.  If this is the case,
   * the projection factor will lie outside the range [0.0, 1.0].
   */
  default Point project(final Point point) {
    final Point lineStart = getPoint(0);
    final Point lineEnd = getPoint(1);
    final Point newPoint = LineSegmentUtil.project(getGeometryFactory(), lineStart, lineEnd, point);
    return newPoint;
  }

  default double projectCoordinate(final int axisIndex, final double projectionFactor) {
    final double value1 = getCoordinate(0, axisIndex);
    final double value2 = getCoordinate(1, axisIndex);
    if (Double.isNaN(value1) || Double.isNaN(value2)) {
      return Double.NaN;
    } else {
      return value1 + (value2 - value1) * projectionFactor;
    }
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
  default LineSegment reverse() {
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[axisCount * 2];
    for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[vertexIndex * axisCount + axisIndex] = getCoordinate(1 - vertexIndex,
          axisIndex);
      }
    }
    return newLineSegment(axisCount, coordinates);
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
  default double segmentFraction(final Point inputPt) {
    double segFrac = projectionFactor(inputPt);
    if (segFrac < 0.0) {
      segFrac = 0.0;
    } else if (segFrac > 1.0 || Double.isNaN(segFrac)) {
      segFrac = 1.0;
    }
    return segFrac;
  }

  default boolean touchesEnd(final LineSegment closestSegment) {
    if (isEndPoint(closestSegment.getPoint(0))) {
      return true;
    } else if (isEndPoint(closestSegment.getPoint(1))) {
      return true;
    }
    return false;
  }

}
