package com.revolsys.gis.model.coordinates;

import java.util.Set;
import java.util.TreeSet;

import com.revolsys.data.equals.NumberEquals;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustDeterminant;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.LineStringDouble;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.util.BoundingBoxUtil;
import com.revolsys.util.MathUtil;

public class LineSegmentUtil {

  public static Point closestPoint(final Point lineStart, final Point lineEnd,
    final Point point) {
    final double factor = projectionFactor(lineStart, lineEnd, point);
    if (factor > 0 && factor < 1) {
      return project(null, lineStart, lineEnd, point);
    }
    final double dist0 = lineStart.distance(point);
    final double dist1 = lineEnd.distance(point);
    if (dist0 < dist1) {
      return lineStart;
    }
    return lineEnd;
  }

  public static double det(final double a, final double b, final double c,
    final double d) {
    return a * d - b * c;
  }

  public static double distanceLineLine(final Point line1From, final Point line1To,
    final Point line2From, final Point line2To) {
    final double line1X1 = line1From.getX();
    final double line1Y1 = line1From.getY();

    final double line1X2 = line1To.getX();
    final double line1Y2 = line1To.getY();

    final double line2X1 = line2From.getX();
    final double line2Y1 = line2From.getY();

    final double line2X2 = line2To.getX();
    final double line2Y2 = line2To.getY();
    return distanceLineLine(line1X1, line1Y1, line1X2, line1Y2, line2X1,
      line2Y1, line2X2, line2Y2);
  }

  /**
   * Computes the distance from a line segment AB to a line segment CD
   * 
   * Note: NON-ROBUST!
   * 
   * @param A
   *          a point of one line
   * @param B
   *          the second point of (must be different to A)
   * @param C
   *          one point of the line
   * @param D
   *          another point of the line (must be different to A)
   */
  public static double distanceLineLine(final double line1X1,
    final double line1Y1, final double line1X2, final double line1Y2,
    final double line2X1, final double line2Y1, final double line2X2,
    final double line2Y2) {
    // check for zero-length segments
    if (line1X1 == line1X2 && line1Y1 == line1Y2) {
      return distanceLinePoint(line2X1, line2Y1, line2X2, line2Y2, line1X1,
        line1Y1);
    } else if (line2X1 == line2X2 && line2Y1 == line2Y2) {
      return distanceLinePoint(line1X1, line1Y1, line1X2, line1Y2, line2X1,
        line2Y1);
    } else {
      // AB and CD are line segments
      /*
       * from comp.graphics.algo Solving the above for r and s yields
       * (Ay-Cy)(Dx-Cx)-(Ax-Cx)(Dy-Cy) r = ----------------------------- (eqn 1)
       * (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx) (Ay-Cy)(Bx-Ax)-(Ax-Cx)(By-Ay) s =
       * ----------------------------- (eqn 2) (Bx-Ax)(Dy-Cy)-(By-Ay)(Dx-Cx) Let
       * P be the position vector of the intersection point, then P=A+r(B-A) or
       * Px=Ax+r(Bx-Ax) Py=Ay+r(By-Ay) By examining the values of r & s, you can
       * also determine some other limiting conditions: If 0<=r<=1 & 0<=s<=1,
       * intersection exists r<0 or r>1 or s<0 or s>1 line segments do not
       * intersect If the denominator in eqn 1 is zero, AB & CD are parallel If
       * the numerator in eqn 1 is also zero, AB & CD are collinear.
       */

      boolean noIntersection = false;
      if (!BoundingBoxUtil.intersects(line1X1, line1Y1, line1X2, line1Y2, line2X1,
        line2Y1, line2X2, line2Y2)) {
        noIntersection = true;
      } else {
        final double denom = (line1X2 - line1X1) * (line2Y2 - line2Y1)
          - (line1Y2 - line1Y1) * (line2X2 - line2X1);

        if (denom == 0) {
          noIntersection = true;
        } else {
          final double r_num = (line1Y1 - line2Y1) * (line2X2 - line2X1)
            - (line1X1 - line2X1) * (line2Y2 - line2Y1);
          final double s_num = (line1Y1 - line2Y1) * (line1X2 - line1X1)
            - (line1X1 - line2X1) * (line1Y2 - line1Y1);

          final double s = s_num / denom;
          final double r = r_num / denom;

          if ((r < 0) || (r > 1) || (s < 0) || (s > 1)) {
            noIntersection = true;
          }
        }
      }
      if (noIntersection) {
        final double distance1 = distanceLinePoint(line2X1, line2Y1, line2X2,
          line2Y2, line1X1, line1Y1);
        final double distance2 = distanceLinePoint(line2X1, line2Y1, line2X2,
          line2Y2, line1X2, line1Y2);
        final double distance3 = distanceLinePoint(line1X1, line1Y1, line1X2,
          line1Y2, line2X1, line2Y1);
        final double distance4 = distanceLinePoint(line1X1, line1Y1, line1X2,
          line1Y2, line2X2, line2Y2);
        return com.revolsys.jts.math.MathUtil.min(distance1, distance2,
          distance3, distance4);
      } else {
        // segments intersect
        return 0.0;
      }
    }
  }

  /**
   * Calculate the distance between the line from x1,y1 to x2,y2 and the point
   * x,y.
   * 
   * @param x1 The x coordinate at the start of the line.
   * @param y1 The y coordinate at the start of the line.
   * @param x2 The x coordinate at the end of the line.
   * @param y2 The y coordinate at the end of the line.
   * @param x The x coordinate of the point.
   * @param y The y coordinate of the point.
   * @return The distance.
   */
  public static double distanceLinePoint(final double x1, final double y1,
    final double x2, final double y2, final double x, final double y) {
    if (x1 == x2 && y1 == y2) {
      return MathUtil.distance(x, y, x1, y1);
    } else {
      final double dxx1 = x - x1;
      final double dx2x1 = x2 - x1;
      final double dyy1 = y - y1;
      final double dy2y1 = y2 - y1;
      final double r = (dxx1 * dx2x1 + dyy1 * dy2y1)
        / (dx2x1 * dx2x1 + dy2y1 * dy2y1);

      if (r <= 0.0) {
        return MathUtil.distance(x, y, x1, y1);
      } else if (r >= 1.0) {
        return MathUtil.distance(x, y, x2, y2);
      } else {
        final double dy1y = y1 - y;
        final double dx1x = x1 - x;
        final double s = (dy1y * dx2x1 - dx1x * dy2y1)
          / (dx2x1 * dx2x1 + dy2y1 * dy2y1);

        return Math.abs(s) * Math.sqrt(dx2x1 * dx2x1 + dy2y1 * dy2y1);
      }
    }

  }

  /**
   * Calculate the distance between the line from lineStart to lineEnd and the
   * point.
   * 
   * @param lineStart The point at the start of the line.
   * @param lineEnd The point at the end of the line.
   * @param point The point.
   * @param point The coordinates of the point location.
   * @return The distance.
   */
  public static double distanceLinePoint(final Point lineStart,
    final Point lineEnd, final Point point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();
    final double x = point.getX();
    final double y = point.getY();
    return distanceLinePoint(x1, y1, x2, y2, x, y);
  }

  /**
   * Check to see if the point (x,y) intersects the envelope of the line from
   * (x1,y1) to (x2,y2).
   * 
   * @param x1 The x coordinate at the start of the line.
   * @param y1 The y coordinate at the start of the line.
   * @param x2 The x coordinate at the end of the line.
   * @param y2 The y coordinate at the end of the line.
   * @param x The x coordinate of the point.
   * @param y The y coordinate of the point.
   * @return True if the point intersects the line's envelope.
   */
  public static boolean envelopeIntersects(final double x1, final double y1,
    final double x2, final double y2, final double x, final double y) {
    final double minX = Math.min(x1, x2);
    if (x >= minX) {
      final double maxX = Math.max(x1, x2);
      if (x <= maxX) {
        final double minY = Math.min(y1, y2);
        if (y >= minY) {
          final double maxY = Math.max(y1, y2);
          if (y <= maxY) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Check to see if the point intersects the envelope of the line from
   * lineStart to lineEnd.
   * 
   * @param lineStart The point at the start of the line.
   * @param lineEnd The point at the end of the line.
   * @param point The point.
   * @return True if the point intersects the line's envelope.
   */
  public static boolean envelopeIntersects(final Point lineStart,
    final Point lineEnd, final Point point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();

    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();

    return envelopeIntersects(x1, y1, x2, y2, x, y);
  }

  /**
   * Check to see if the envelope one the line from line1Start to line1End
   * intersects the envelope of the line from line2Start to line2End.
   * 
   * @param line1Start The point at the start of the first line.
   * @param line1End The point at the end of the first line.
   * @param line2Start The point at the start of the second line.
   * @param line2End The point at the end of the second line.
   * @return True if the envelope of line intersects the envelope of line 2.
   */
  public static boolean envelopeIntersects(final Point line1Start,
    final Point line1End, final Point line2Start, final Point line2End) {
    final double line1X1 = line1Start.getX();
    final double line1X2 = line1End.getX();

    final double line2X1 = line2Start.getX();
    final double line2X2 = line2End.getX();

    final double max1X = Math.max(line1X1, line1X2);
    final double min2X = Math.min(line2X1, line2X2);
    if (min2X <= max1X) {
      final double min1X = Math.min(line1X1, line1X2);
      final double max2X = Math.max(line2X1, line2X2);
      if (min1X <= max2X) {
        final double line1Y1 = line1Start.getY();
        final double line1Y2 = line1End.getY();

        final double line2Y1 = line2Start.getY();
        final double line2Y2 = line2End.getY();

        final double max1Y = Math.max(line1Y1, line1Y2);
        final double min2Y = Math.min(line2Y1, line2Y2);
        if (min2Y <= max1Y) {
          final double min1Y = Math.min(line1Y1, line1Y2);
          final double max2Y = Math.max(line2Y1, line2Y2);
          if (min1Y <= max2Y) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static Point getElevation(
    final GeometryFactory geometryFactory,
    final Point lineStart, final Point lineEnd, final Point point) {
    final int axisCount = geometryFactory.getAxisCount();
    final double[] coordinates = point.getCoordinates();
    if (axisCount > 2) {
      final double fraction = point.distance(lineStart)
        / lineStart.distance(lineEnd);
      double z1 = lineStart.getZ();
      if (Double.isNaN(z1)) {
        z1 = 0;
      }
      double z2 = lineEnd.getZ();
      if (Double.isNaN(z2)) {
        z2 = 0;
      }
      final double z = z1 + (z2 - z1) * (fraction);
      if (coordinates.length < 3) {
        return geometryFactory.point(coordinates[0], coordinates[1], z);
      } else {
        coordinates[2] = z;
        return geometryFactory.point(coordinates);
      }
    } else {
      return point;
    }
  }

  public static double getElevation(final Point lineStart, final Point lineEnd,
    final Point point) {
    final double fraction = point.distance(lineStart)
      / lineStart.distance(lineEnd);
    final double z = lineStart.getZ() + (lineEnd.getZ() - lineStart.getZ())
      * (fraction);
    return z;
  }

  /**
   * Get the intersection between line (segment) 1 and line (segment) 2. The
   * result will be either and empty collection, a single coordinates value for
   * a crosses intersection or a pair of coordinates for a linear intersection.
   * The results will be rounded according to the precision model. Any z-value
   * interpolation will be calculated using the z-values from line (segment) 1.
   * For linear intersections the order of the points will be the same as the
   * orientation of line1.
   * 
   * @param geometryFactory
   * @param line1Start
   * @param line1End
   * @param line2Start
   * @param line2End
   * @return
   */
  public static LineString getIntersection(
    final GeometryFactory geometryFactory, Point line1Start, Point line1End,
    Point line2Start, Point line2End) {
    line1Start = geometryFactory.createCoordinates(line1Start);
    line1End = geometryFactory.createCoordinates(line1End);
    line2Start = geometryFactory.createCoordinates(line2Start);
    line2End = geometryFactory.createCoordinates(line2End);
    if (BoundingBoxUtil.intersects(line1Start, line1End, line2Start, line2End)) {
      final Set<Point> intersections = new TreeSet<Point>(
        new CoordinatesDistanceComparator(line1Start));
      if (LineSegmentUtil.isPointOnLine(geometryFactory, line2Start, line2End,
        line1Start)) {
        intersections.add(line1Start);
      }
      if (LineSegmentUtil.isPointOnLine(geometryFactory, line2Start, line2End,
        line1End)) {
        intersections.add(line1End);
      }
      if (LineSegmentUtil.isPointOnLine(geometryFactory, line1Start, line1End,
        line2Start)) {
        final Point intersection = getElevation(geometryFactory, line1Start,
          line1End, line2Start);
        intersections.add(intersection);
      }
      if (LineSegmentUtil.isPointOnLine(geometryFactory, line1Start, line1End,
        line2End)) {
        final Point intersection = getElevation(geometryFactory, line1Start,
          line1End, line2End);
        intersections.add(intersection);
      }

      if (intersections.isEmpty()) {
        final double line1x1 = line1Start.getX();
        final double line1y1 = line1Start.getY();
        final double line1x2 = line1End.getX();
        final double line1y2 = line1End.getY();

        final double line2x1 = line2Start.getX();
        final double line2y1 = line2Start.getY();
        final double line2x2 = line2End.getX();
        final double line2y2 = line2End.getY();

        final int Pq1 = CoordinatesListUtil.orientationIndex(line1x1, line1y1,
          line1x2, line1y2, line2x1, line2y1);
        final int Pq2 = CoordinatesListUtil.orientationIndex(line1x1, line1y1,
          line1x2, line1y2, line2x2, line2y2);

        if (!((Pq1 > 0 && Pq2 > 0) || (Pq1 < 0 && Pq2 < 0))) {
          final int Qp1 = CoordinatesListUtil.orientationIndex(line2x1,
            line2y1, line2x2, line2y2, line1x1, line1y1);
          final int Qp2 = CoordinatesListUtil.orientationIndex(line2x1,
            line2y1, line2x2, line2y2, line1x2, line1y2);

          if (!((Qp1 > 0 && Qp2 > 0) || (Qp1 < 0 && Qp2 < 0))) {
            final double detLine1StartLine1End = LineSegmentUtil.det(line1x1,
              line1y1, line1x2, line1y2);
            final double detLine2StartLine2End = LineSegmentUtil.det(line2x1,
              line2y1, line2x2, line2y2);
            final double x = LineSegmentUtil.det(detLine1StartLine1End, line1x1
              - line1x2, detLine2StartLine2End, line2x1 - line2x2)
              / LineSegmentUtil.det(line1x1 - line1x2, line1y1 - line1y2,
                line2x1 - line2x2, line2y1 - line2y2);
            final double y = LineSegmentUtil.det(detLine1StartLine1End, line1y1
              - line1y2, detLine2StartLine2End, line2y1 - line2y2)
              / LineSegmentUtil.det(line1x1 - line1x2, line1y1 - line1y2,
                line2x1 - line2x2, line2y1 - line2y2);
            Point intersection = geometryFactory.createCoordinates(x, y);
            intersection = getElevation(geometryFactory, line1Start, line1End,
              intersection);
            final LineStringDouble points = new LineStringDouble(
              geometryFactory.getAxisCount(), intersection);
            return points;
          }
        }
      } else {
        return geometryFactory.createCoordinatesList(intersections);
      }
    }
    return new LineStringDouble(2);
  }

  public static boolean intersects(final Point line1p1, final Point line1p2,
    final Point line2p1, final Point line2p2) {
    final LineIntersector li = new RobustLineIntersector();
    li.computeIntersection(line1p1, line1p2, line2p1, line2p2);
    return li.hasIntersection();
  }

  public static boolean isPointOnLine(final double x1, final double y1,
    final double x2, final double y2, final double x, final double y,
    final double maxDistance) {
    if (NumberEquals.equal(x, x1) && NumberEquals.equal(y, y1)) {
      return true;
    } else if (NumberEquals.equal(x, x2) && NumberEquals.equal(y, y2)) {
      return true;
    } else {
      final double distance = distanceLinePoint(x1, y1, x2, y2, x, y);
      if (distance < maxDistance) {
        final double projectionFactor = projectionFactor(x1, y1, x2, y2, y, y);
        if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
          return true;
        }
      }
      return false;
    }
  }

  /**
   * Check to see if the point is on the line between lineStart and lineEnd
   * using the precision model to see if a line split at the projection of the
   * point on the line would be the same point.
   * 
   * @param precisionModel The precision model.
   * @param lineStart The point at the start of the line.
   * @param lineEnd The point at the end of the line.
   * @param point The point.
   * @return True if the point is on the line.
   */
  public static boolean isPointOnLine(final GeometryFactory precisionModel,
    final Point lineStart, final Point lineEnd, final Point point) {
    if (lineStart.equals(2, point)) {
      return true;
    } else if (lineEnd.equals(2, point)) {
      return true;
    } else {
      final double projectionFactor = projectionFactor(lineStart, lineEnd,
        point);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        Point projectedPoint = project(2, lineStart, lineEnd, projectionFactor);
        if (precisionModel != null) {
          projectedPoint = precisionModel.getPreciseCoordinates(projectedPoint);
        }
        if (projectedPoint.equals(2, point)) {
          return true;
        }
      }
      return false;
    }
  }

  public static boolean isPointOnLine(final Point lineStart,
    final Point lineEnd, final Point point, final double maxDistance) {
    if (lineStart.equals(2, point)) {
      return true;
    } else if (lineEnd.equals(2, point)) {
      return true;
    } else {
      final double distance = distanceLinePoint(lineStart, lineEnd, point);
      if (distance < maxDistance) {
        final double projectionFactor = projectionFactor(lineStart, lineEnd,
          point);
        if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
          return true;
        }
      }
      return false;
    }
  }

  public static boolean isPointOnLineMiddle(final double x1, final double y1,
    final double x2, final double y2, final double x, final double y,
    final double maxDistance) {
    if (NumberEquals.equal(x, x1) && NumberEquals.equal(y, y1)) {
      return false;
    } else if (NumberEquals.equal(x, x2) && NumberEquals.equal(y, y2)) {
      return false;
    } else {
      final double distance = distanceLinePoint(x1, y1, x2, y2, x, y);
      if (distance < maxDistance) {
        final double projectionFactor = projectionFactor(x1, y1, x2, y2, x, y);
        if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
          return true;
        }
      }
      return false;
    }
  }

  public static boolean isPointOnLineMiddle(
    final GeometryFactory precisionModel, final Point lineStart,
    final Point lineEnd, final Point point) {
    if (point.equals(2, lineStart)) {
      return false;
    } else if (point.equals(2, lineEnd)) {
      return false;
    } else {
      final double projectionFactor = projectionFactor(lineStart, lineEnd,
        point);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        Point projectedPoint = project(2, lineStart, lineEnd, projectionFactor);
        projectedPoint = precisionModel.getPreciseCoordinates(projectedPoint);
        if (projectedPoint.equals(2, point)) {
          return true;
        }
      }
      return false;
    }
  }

  public static Point midPoint(final GeometryFactory precisionModel,
    final Point lineStart, final Point lineEnd) {
    return project(precisionModel, lineStart, lineEnd, 0.5);
  }

  public static Point midPoint(final Point lineStart, final Point lineEnd) {
    return midPoint(null, lineStart, lineEnd);
  }

  public static int orientationIndex(final Point lineStart,
    final Point lineEnd, final Point point) {
    final double lineDx = lineEnd.getX() - lineStart.getX();
    final double lineDy = lineEnd.getY() - lineStart.getY();
    final double dx2 = point.getX() - lineEnd.getX();
    final double dy2 = point.getY() - lineEnd.getY();
    return RobustDeterminant.signOfDet2x2(lineDx, lineDy, dx2, dy2);
  }

  /**
   * Calculate the counter clockwise angle in radians of the difference between
   * the two vectors from the start point and line1End and line2End. The angle
   * is relative to the vector from start to line1End. The angle will be in the
   * range 0 -> 2 * PI.
   * 
   * @return The angle in radians.
   */
  public static double orientedAngleBetween2d(final Point start,
    final Point line1End, final Point line2End) {
    final double angle1 = start.angle2d(line1End);
    final double angle2 = start.angle2d(line2End);
    return MathUtil.orientedAngleBetween(angle1, angle2);
  }

  public static Point pointAlong(final GeometryFactory precisionModel,
    final Point lineStart, final Point lineEnd, final Point point) {
    final double projectionFactor = projectionFactor(lineStart, lineEnd, point);
    if (projectionFactor < 0.0) {
      return lineStart;
    } else if (projectionFactor > 1.0) {
      return lineEnd;
    } else {
      return project(precisionModel, lineStart, lineEnd, projectionFactor);
    }
  }

  public static Point pointAlong(final Point p0, final Point p1,
    final double segmentLengthFraction) {
    final double x1 = p0.getX();
    final double x2 = p1.getX();
    final double y1 = p0.getY();
    final double y2 = p1.getY();
    final double x = x1 + segmentLengthFraction * (x2 - x1);
    final double y = y1 + segmentLengthFraction * (y2 - y1);
    final Point coord = new PointDouble(x, y, Point.NULL_ORDINATE);
    return coord;
  }

  public static Point project(final GeometryFactory precisionModel,
    final Point lineStart, final Point lineEnd, final double r) {
    final int axisCount = CoordinatesUtil.getAxisCount(lineStart, lineEnd);
    final Point point = project(axisCount, lineStart, lineEnd, r);
    if (precisionModel != null) {
      return precisionModel.getPreciseCoordinates(point);
    }

    return point;
  }

  public static Point project(final GeometryFactory precisionModel,
    final Point lineStart, final Point lineEnd, final Point point) {
    if (point.equals(2, lineStart) || point.equals(2, lineEnd)) {
      return point.clonePoint();
    } else {
      final double r = projectionFactor(lineStart, lineEnd, point);
      final int axisCount = CoordinatesUtil.getAxisCount(point, lineStart,
        lineEnd);
      Point projectedCoordinate = project(axisCount, lineStart, lineEnd, r);
      if (precisionModel != null) {
        projectedCoordinate = precisionModel.getPreciseCoordinates(projectedCoordinate);
      }
      if (projectedCoordinate.equals(2, lineStart)) {
        return lineStart;
      } else if (projectedCoordinate.equals(2, lineEnd)) {
        return lineEnd;
      } else {
        if (axisCount > 2) {
          final double z = projectedCoordinate.getZ();
          if (MathUtil.isNanOrInfinite(z) || z == 0) {
            final double[] coordinates = projectedCoordinate.getCoordinates();
            coordinates[2] = point.getZ();
            return new PointDouble(coordinates);
          }
        }

        return projectedCoordinate;
      }
    }
  }

  public static Point project(final int axisCount, final Point lineStart,
    final Point lineEnd, final double r) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double z1 = lineStart.getZ();

    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();
    final double z2 = lineEnd.getZ();

    final double x = x1 + r * (x2 - x1);
    final double y = y1 + r * (y2 - y1);

    if (axisCount == 2) {
      return new PointDouble(x, y);
    } else {
      double z;
      if (MathUtil.isNanOrInfinite(z1, z2)) {
        z = Double.NaN;
      } else {
        z = z1 + r * (z2 - z1);
      }
      return new PointDouble(axisCount, x, y, z);
    }
  }

  /**
   * Calculate the projection factor of the distance of the point (x,y)
   * coordinates along the line (x1,y1 -> x2,y2). If the point is within the
   * line the range will be between 0.0 -> 1.0.
   * 
   * @param x1 The x coordinate for the start of the line.
   * @param y1 The y coordinate for the start of the line.
   * @param x2 The x coordinate for the end of the line.
   * @param y2 The y coordinate for the end of the line.
   * @param x The x coordinate for the point.
   * @param y The y coordinate for the point.
   * @return The projection factor from (-inf -> +inf).
   */
  public static double projectionFactor(final double x1, final double y1,
    final double x2, final double y2, final double x, final double y) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double length = dx * dx + dy * dy;
    final double r = ((x - x1) * dx + (y - y1) * dy) / length;
    return r;
  }

  /**
   * Calculate the projection factor of the distance of the point coordinates
   * along the line. If the point is within the line the range will be between
   * 0.0 -> 1.0.
   * 
   * @param lineStart The start coordinates of the line.
   * @param lineEnd The end coordinates of the line.
   * @param point The point coordinates.
   * @return The projection factor from (-inf -> +inf).
   */
  public static double projectionFactor(final Point lineStart,
    final Point lineEnd, final Point point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();

    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();

    return projectionFactor(x1, y1, x2, y2, x, y);
  }

  public static double segmentFraction(final Point lineStart,
    final Point lineEnd, final Point point) {
    final double segFrac = projectionFactor(lineStart, lineEnd, point);
    if (segFrac < 0.0) {
      return 0.0;
    } else if (segFrac > 1.0) {
      return 1.0;
    } else {
      return segFrac;
    }
  }

}
