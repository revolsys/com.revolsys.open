package com.revolsys.gis.model.coordinates;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.revolsys.jts.algorithm.RobustDeterminant;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.MathUtil;

public class LineSegmentUtil {
  public static void addElevation(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart, final Coordinates lineEnd,
    final Coordinates point) {
    final double z = getElevation(lineStart, lineEnd, point);
    point.setZ(z);
    precisionModel.makePrecise(point);
  }

  static double det(final double a, final double b, final double c,
    final double d) {
    return a * d - b * c;
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
  public static double distance(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();
    final double x = point.getX();
    final double y = point.getY();
    return distance(x1, y1, x2, y2, x, y);
  }

  public static double distance(final Coordinates line1From,
    final Coordinates line1To, final Coordinates line2From,
    final Coordinates line2To) {
    if (line1From.equals(line1To)) {
      return distance(line1From, line2From, line2To);
    } else if (line2From.equals(line2To)) {
      return distance(line2To, line1From, line1To);
    } else {
      final double line1FromX = line1From.getX();
      final double line1FromY = line1From.getY();

      final double line1ToX = line1To.getX();
      final double line1ToY = line1To.getY();

      final double line2FromX = line2From.getX();
      final double line2FromY = line2From.getY();

      final double line2ToX = line2To.getX();
      final double line2ToY = line2To.getY();

      final double r_top = (line1FromY - line2FromY) * (line2ToX - line2FromX)
        - (line1FromX - line2FromX) * (line2ToY - line2FromY);
      final double r_bot = (line1ToX - line1FromX) * (line2ToY - line2FromY)
        - (line1ToY - line1FromY) * (line2ToX - line2FromX);

      final double s_top = (line1FromY - line2FromY) * (line1ToX - line1FromX)
        - (line1FromX - line2FromX) * (line1ToY - line1FromY);
      final double s_bot = (line1ToX - line1FromX) * (line2ToY - line2FromY)
        - (line1ToY - line1FromY) * (line2ToX - line2FromX);

      if ((r_bot == 0) || (s_bot == 0)) {
        return Math.min(
          distance(line1From, line2From, line2To),
          Math.min(
            distance(line1To, line2From, line2To),
            Math.min(distance(line2From, line1From, line1To),
              distance(line2To, line1From, line1To))));
      } else {
        final double s = s_top / s_bot;
        final double r = r_top / r_bot;

        if ((r < 0) || (r > 1) || (s < 0) || (s > 1)) {
          return Math.min(
            distance(line2From, line2To, line1From),
            Math.min(
              distance(line2From, line2To, line1To),
              Math.min(distance(line1From, line1To, line2From),
                distance(line1From, line1To, line2To))));
        } else {
          return 0.0;
        }
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
  public static double distance(final double x1, final double y1,
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
   * Check to see if the point intersects the envelope of the line from
   * lineStart to lineEnd.
   * 
   * @param lineStart The point at the start of the line.
   * @param lineEnd The point at the end of the line.
   * @param point The point.
   * @return True if the point intersects the line's envelope.
   */
  public static boolean envelopeIntersects(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
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
  public static boolean envelopeIntersects(final Coordinates line1Start,
    final Coordinates line1End, final Coordinates line2Start,
    final Coordinates line2End) {
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

  public static Coordinates getElevation(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory,
    final Coordinates lineStart, final Coordinates lineEnd,
    final Coordinates point) {
    final int numAxis = geometryFactory.getNumAxis();
    final Coordinates newPoint = geometryFactory.createCoordinates(point);
    if (numAxis > 2) {
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
      newPoint.setZ(z);
    }
    geometryFactory.makePrecise(newPoint);
    return newPoint;
  }

  public static double getElevation(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
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
  public static CoordinatesList getIntersection(
    final GeometryFactory geometryFactory, Coordinates line1Start,
    Coordinates line1End, Coordinates line2Start, Coordinates line2End) {
    line1Start = geometryFactory.createCoordinates(line1Start);
    line1End = geometryFactory.createCoordinates(line1End);
    line2Start = geometryFactory.createCoordinates(line2Start);
    line2End = geometryFactory.createCoordinates(line2End);
    if (BoundingBox.intersects(line1Start, line1End, line2Start, line2End)) {
      final Set<Coordinates> intersections = new TreeSet<Coordinates>(
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
        final Coordinates intersection = getElevation(geometryFactory,
          line1Start, line1End, line2Start);
        intersections.add(intersection);
      }
      if (LineSegmentUtil.isPointOnLine(geometryFactory, line1Start, line1End,
        line2End)) {
        final Coordinates intersection = getElevation(geometryFactory,
          line1Start, line1End, line2End);
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
            Coordinates intersection = geometryFactory.createCoordinates(x, y);
            intersection = getElevation(geometryFactory, line1Start, line1End,
              intersection);
            return geometryFactory.createCoordinatesList(intersection);
          }
        }
      } else {
        return geometryFactory.createCoordinatesList(intersections);
      }
    }
    return geometryFactory.createCoordinatesList(0);
  }

  public static boolean intersects(final Coordinates line1p1,
    final Coordinates line1p2, final Coordinates line2p1,
    final Coordinates line2p2) {
    final LineIntersector li = new RobustLineIntersector();
    li.computeIntersection(line1p1, line1p2, line2p1, line2p2);
    return li.hasIntersection();
  }

  public static boolean isPointOnLine(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point, final double maxDistance) {
    if (lineStart.equals2d(point)) {
      return true;
    } else if (lineEnd.equals2d(point)) {
      return true;
    } else {
      final double distance = distance(lineStart, lineEnd, point);
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
  public static boolean isPointOnLine(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart, final Coordinates lineEnd,
    final Coordinates point) {
    if (lineStart.equals2d(point)) {
      return true;
    } else if (lineEnd.equals2d(point)) {
      return true;
    } else {
      final double projectionFactor = projectionFactor(lineStart, lineEnd,
        point);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        final Coordinates projectedPoint = project(2, lineStart, lineEnd,
          projectionFactor);
        if (precisionModel != null) {
          precisionModel.makePrecise(projectedPoint);
        }
        if (projectedPoint.equals2d(point)) {
          return true;
        }
      }
      return false;
    }
  }

  public static boolean isPointOnLine(final double x1, final double y1,
    final double x2, final double y2, final double x, final double y,
    final double maxDistance) {
    final double distance = distance(x1, y1, x2, y2, x, y);
    if (distance < maxDistance) {
      final double projectionFactor = projectionFactor(x1, y1, x2, y2, x, y);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPointOnLineMiddle(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point, final double maxDistance) {
    if (point.equals2d(lineStart)) {
      return false;
    } else if (point.equals2d(lineEnd)) {
      return false;
    } else {
      return isPointOnLine(lineStart, lineEnd, point, maxDistance);
    }
  }

  public static boolean isPointOnLineMiddle(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart, final Coordinates lineEnd,
    final Coordinates point) {
    if (point.equals2d(lineStart)) {
      return false;
    } else if (point.equals2d(lineEnd)) {
      return false;
    } else {
      final double projectionFactor = projectionFactor(lineStart, lineEnd,
        point);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        final Coordinates projectedPoint = project(2, lineStart, lineEnd,
          projectionFactor);
        precisionModel.makePrecise(projectedPoint);
        if (projectedPoint.equals2d(point)) {
          return true;
        }
      }
      return false;
    }
  }

  public static Coordinates midPoint(final Coordinates lineStart,
    final Coordinates lineEnd) {
    return midPoint(null, lineStart, lineEnd);
  }

  public static Coordinates midPoint(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart, final Coordinates lineEnd) {
    return project(precisionModel, lineStart, lineEnd, 0.5);
  }

  public static int orientationIndex(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
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
  public static double orientedAngleBetween2d(final Coordinates start,
    final Coordinates line1End, final Coordinates line2End) {
    final double angle1 = start.angle2d(line1End);
    final double angle2 = start.angle2d(line2End);
    return MathUtil.orientedAngleBetween(angle1, angle2);
  }

  public static Coordinates pointAlong(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart, final Coordinates lineEnd,
    final Coordinates point) {
    final double projectionFactor = projectionFactor(lineStart, lineEnd, point);
    if (projectionFactor < 0.0) {
      return lineStart;
    } else if (projectionFactor > 1.0) {
      return lineEnd;
    } else {
      return project(precisionModel, lineStart, lineEnd, projectionFactor);
    }
  }

  public static Coordinates project(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart, final Coordinates lineEnd,
    final Coordinates point) {
    if (point.equals2d(lineStart) || point.equals2d(lineEnd)) {
      return point.cloneCoordinates();
    } else {
      final double r = projectionFactor(lineStart, lineEnd, point);
      final int numAxis = CoordinatesUtil.getNumAxis(point, lineStart, lineEnd);
      final Coordinates projectedCoordinate = project(numAxis, lineStart,
        lineEnd, r);
      precisionModel.makePrecise(projectedCoordinate);
      if (projectedCoordinate.equals2d(lineStart)) {
        return lineStart;
      } else if (projectedCoordinate.equals2d(lineEnd)) {
        return lineEnd;
      } else {
        if (numAxis > 2) {
          final double z = projectedCoordinate.getZ();
          if (MathUtil.isNanOrInfinite(z) || z == 0) {
            projectedCoordinate.setZ(point.getZ());
          }
        }

        return projectedCoordinate;
      }
    }
  }

  public static Coordinates project(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart, final Coordinates lineEnd, final double r) {
    final int numAxis = CoordinatesUtil.getNumAxis(lineStart, lineEnd);
    final Coordinates point = project(numAxis, lineStart, lineEnd, r);
    if (precisionModel != null) {
      precisionModel.makePrecise(point);
    }

    return point;
  }

  public static Coordinates project(final int numAxis,
    final Coordinates lineStart, final Coordinates lineEnd, final double r) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double z1 = lineStart.getZ();

    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();
    final double z2 = lineEnd.getZ();

    final double x = x1 + r * (x2 - x1);
    final double y = y1 + r * (y2 - y1);

    if (numAxis == 2) {
      return new DoubleCoordinates(x, y);
    } else {
      double z;
      if (MathUtil.isNanOrInfinite(z1, z2)) {
        z = Double.NaN;
      } else {
        z = z1 + r * (z2 - z1);
      }
      return new DoubleCoordinates(numAxis, x, y, z);
    }
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
  public static double projectionFactor(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();

    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();

    return projectionFactor(x1, y1, x2, y2, x, y);
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

  public static double segmentFraction(final Coordinates lineStart,
    final Coordinates lineEnd, final Coordinates point) {
    final double segFrac = projectionFactor(lineStart, lineEnd, point);
    if (segFrac < 0.0) {
      return 0.0;
    } else if (segFrac > 1.0) {
      return 1.0;
    } else {
      return segFrac;
    }
  }

  public static CoordinatesList toCoordinatesList(final int numAxis,
    final List<LineSegment> lineSegments) {
    final CoordinatesList points = new DoubleCoordinatesList(
      lineSegments.size() + 1, numAxis);

    for (int i = 0; i < lineSegments.size(); i++) {
      final LineSegment lineSegment = lineSegments.get(i);
      points.setPoint(i, lineSegment.get(0));
    }
    final LineSegment lineSegment = lineSegments.get(lineSegments.size() - 1);
    points.setPoint(lineSegments.size(), lineSegment.get(1));
    return points;
  }
}
