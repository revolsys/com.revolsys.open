package com.revolsys.gis.model.coordinates;

import java.util.Collections;
import java.util.List;

import com.revolsys.util.ListUtil;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.algorithm.RobustDeterminant;

public class LineSegmentUtil {
  /**
   * Calculate the counter clockwise angle in radians of the difference between
   * the two vectors from the start point and line1End and line2End. The angle
   * is relative to the vector from start to line1End. The angle will be in the
   * range 0 -> 2 * PI.
   * 
   * @return The angle in radians.
   */
  public static double orientedAngleBetween2d(
    Coordinates start,
    Coordinates line1End,
    Coordinates line2End) {
    double angle1 = start.angle2d(line1End);
    double angle2 = start.angle2d(line2End);
    return MathUtil.orientedAngleBetween(angle1, angle2);
  }

  public static double getElevation(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    final double fraction = point.distance(lineStart) / lineStart.distance(lineEnd);
    final double z = lineStart.getZ() + (lineEnd.getZ() - lineStart.getZ()) * (fraction);
    return z;
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
  public static double distance(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();
    final double x = point.getX();
    final double y = point.getY();
    return distance(x1, y1, x2, y2, x, y);
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
  public static double distance(
    final double x1,
    final double y1,
    final double x2,
    final double y2,
    final double x,
    final double y) {
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
  public static boolean envelopeIntersects(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();

    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();

    final double x = point.getX();
    final double y = point.getY();

    return envelopeIntersects(x1, y1, x2, y2, x, y);
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
  public static boolean envelopeIntersects(
    final double x1,
    final double y1,
    final double x2,
    final double y2,
    final double x,
    final double y) {
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
   * Check to see if the envelope one the line from line1Start to line1End
   * intersects the envelope of the line from line2Start to line2End.
   * 
   * @param line1Start The point at the start of the first line.
   * @param line1End The point at the end of the first line.
   * @param line2Start The point at the start of the second line.
   * @param line2End The point at the end of the second line.
   * @return True if the envelope of line intersects the envelope of line 2.
   */
  public static boolean envelopeIntersects(
    final Coordinates line1Start,
    final Coordinates line1End,
    final Coordinates line2Start,
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
   * Get the intersection between line (segment) 1 and line (segment) 2. The
   * result will be either and empty collection, a single coordinates value for
   * a crosses intersection or a pair of coordinates for a linear intersection.
   * The results will be rounded according to the precision model. Any z-value
   * interpolation will be calculated using the z-values from line (segment) 1.
   * 
   * @param precisionModel
   * @param line1Start
   * @param line1End
   * @param line2Start
   * @param line2End
   * @return
   */
  public static List<Coordinates> intersection(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates line1Start,
    final Coordinates line1End,
    final Coordinates line2Start,
    final Coordinates line2End) {
    if (envelopeIntersects(line1Start, line1End, line2Start, line2End)) {
      final PointLineProjection line1StartProjection = getPointLineProjection(
        precisionModel, line2Start, line2End, line1Start);
      final PointLineProjection line1EndProjection = getPointLineProjection(
        precisionModel, line2Start, line2End, line1End);
      final PointLineProjection line2StartProjection = getPointLineProjection(
        precisionModel, line1Start, line1End, line2Start);
      final PointLineProjection line2EndProjection = getPointLineProjection(
        precisionModel, line1Start, line1End, line2End);

      if (line1StartProjection.isPointOnLine()) {
        if (line1EndProjection.isPointOnLine()) {
          return getUniqueCoordinates(line1Start, line1End);
        } else if (line2StartProjection.isPointOnLine()) {
          final Coordinates point1 = line2StartProjection.getProjectedPoint();
          return getUniqueCoordinates(line1Start, point1);
        } else if (line2EndProjection.isPointOnLine()) {
          final Coordinates point2 = line2EndProjection.getProjectedPoint();
          return getUniqueCoordinates(line1Start, point2);
        } else {
          return ListUtil.create(line1Start);
        }
      } else if (line1EndProjection.isPointOnLine()) {
        if (line2StartProjection.isPointOnLine()) {
          final Coordinates point1 = line2StartProjection.getProjectedPoint();
          return getUniqueCoordinates(point1, line1End);
        } else if (line2EndProjection.isPointOnLine()) {
          final Coordinates point2 = line2EndProjection.getProjectedPoint();
          return getUniqueCoordinates(point2, line1End);
        } else {
          return ListUtil.create(line1End);
        }
      } else if (line2StartProjection.isPointOnLine()) {
        if (line2EndProjection.isPointOnLine()) {
          double factor1 = line2StartProjection.getProjectionFactor();
          double factor2 = line2EndProjection.getProjectionFactor();
          final Coordinates point1 = line2StartProjection.getProjectedPoint();
          final Coordinates point2 = line2EndProjection.getProjectedPoint();
          if (factor1 == factor2) {
            return ListUtil.create(point1);
          } else if (factor1 > factor2) {
            return ListUtil.create(point2, point1);
          } else {
            return ListUtil.create(point1, point2);
          }
        } else {
          final Coordinates point1 = line2StartProjection.getProjectedPoint();
          return ListUtil.create(point1);
        }
      } else if (line2EndProjection.isPointOnLine()) {
        final Coordinates point2 = line2EndProjection.getProjectedPoint();
        return ListUtil.create(point2);
      } else {
        // Check the orientations of the ends of each line in respect to the
        // other line
        int line2StartOrientation = orientationIndex(line1Start, line1End,
          line2Start);
        int line2EndOrientation = orientationIndex(line1Start, line1End,
          line2End);

        if (line2StartOrientation != line2EndOrientation
          || line2StartOrientation == 0) {
          int line1StartOrientation = orientationIndex(line2Start, line2End,
            line1Start);
          int line1EndOrientation = orientationIndex(line2Start, line2End,
            line1End);
          if (line1StartOrientation != line1EndOrientation
            || line1StartOrientation == 0) {
            Coordinates intersection = intersection(line1Start, line1End,
              line2Start, line2End);
            if (intersection != null) {
              Coordinates projectedCoordinate = project(precisionModel,
                line1Start, line1End, intersection);
              return ListUtil.create(projectedCoordinate);
            }
          }
        }
      }
    }
    return Collections.emptyList();
  }

  public static Coordinates intersection(
    Coordinates line1Start,
    Coordinates line1End,
    Coordinates line2Start,
    Coordinates line2End) {
    final double line1x1 = line1Start.getX();
    final double line1y1 = line1Start.getY();
    final double line1y2 = line1End.getY();
    final double line1x2 = line1End.getX();

    final double line2x1 = line2Start.getX();
    final double line2y1 = line2Start.getY();
    final double line2y2 = line2End.getY();
    final double line2x2 = line2End.getX();

    return intersection(line1x1, line1y1, line1x2, line1y2, line2x1, line2y1,
      line2x2, line2y2);
  }

  public static Coordinates intersection(
    final double line1x1,
    final double line1y1,
    final double line1x2,
    final double line1y2,
    final double line2x1,
    final double line2y1,
    final double line2x2,
    final double line2y2) {
    double x = det(det(line1x1, line1y1, line1x2, line1y2), line1x1 - line1x2,
      det(line2x1, line2y1, line2x2, line2y2), line2x1 - line2x2)
      / det(line1x1 - line1x2, line1y1 - line1y2, line2x1 - line2x2, line2y1
        - line2y2);
    double y = det(det(line1x1, line1y1, line1x2, line1y2), line1y1 - line1y2,
      det(line2x1, line2y1, line2x2, line2y2), line2y1 - line2y2)
      / det(line1x1 - line1x2, line1y1 - line1y2, line2x1 - line2x2, line2y1
        - line2y2);
    return new DoubleCoordinates(x, y);
  }

  static double det(
    double a,
    double b,
    double c,
    double d) {
    return a * d - b * c;
  }

  private static List<Coordinates> getUniqueCoordinates(
    final Coordinates point1,
    final Coordinates point2) {
    if (point1.equals2d(point2)) {
      return ListUtil.create(point1);
    } else {
      return ListUtil.create(point1, point2);
    }
  }

  public static int orientationIndex(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    final double lineDx = lineEnd.getX() - lineStart.getX();
    final double lineDy = lineEnd.getY() - lineStart.getY();
    final double dx2 = point.getX() - lineEnd.getX();
    final double dy2 = point.getY() - lineEnd.getY();
    return RobustDeterminant.signOfDet2x2(lineDx, lineDy, dx2, dy2);
  }

  public static Coordinates project(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final double r) {
    final double x1 = lineStart.getX();
    final double y1 = lineStart.getY();
    final double z1 = lineStart.getZ();

    final double x2 = lineEnd.getX();
    final double y2 = lineEnd.getY();
    final double z2 = lineEnd.getZ();

    final double x = x1 + r * (x2 - x1);
    final double y = y1 + r * (y2 - y1);

    if (Double.isNaN(z1) || Double.isNaN(z2)) {
      return new DoubleCoordinates(x, y);
    } else {
      final double z = z1 + r * (z2 - z1);
      return new DoubleCoordinates(x, y, z);
    }
  }

  public static Coordinates project(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final double r) {
    Coordinates point = project(lineStart, lineEnd, r);
    precisionModel.makePrecise(point);
    return point;
  }

  public static Coordinates midPoint(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart,
    final Coordinates lineEnd) {
    return project(precisionModel, lineStart, lineEnd, 0.5);
  }

  public static Coordinates pointAlong(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    double projectionFactor = projectionFactor(lineStart, lineEnd, point);
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
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    if (point.equals2d(lineStart) || point.equals2d(lineEnd)) {
      return point.clone();
    } else {
      final double r = projectionFactor(lineStart, lineEnd, point);
      final Coordinates projectedCoordinate = project(lineStart, lineEnd, r);
      precisionModel.makePrecise(projectedCoordinate);
      if (projectedCoordinate.equals2d(lineStart)) {
        return lineStart;
      } else if (projectedCoordinate.equals2d(lineEnd)) {
        return lineEnd;
      } else {
        return projectedCoordinate;
      }
    }
  }

  public static boolean isPointOnLineMiddle(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    if (point.equals2d(lineStart)) {
      return false;
    } else if (point.equals2d(lineEnd)) {
      return false;
    } else {
      double projectionFactor = projectionFactor(lineStart, lineEnd, point);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        final Coordinates projectedPoint = project(lineStart, lineEnd,
          projectionFactor);
        precisionModel.makePrecise(projectedPoint);
        if (projectedPoint.equals2d(point)) {
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
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
    double projectionFactor = projectionFactor(lineStart, lineEnd, point);
    if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
      final Coordinates projectedPoint = project(lineStart, lineEnd,
        projectionFactor);
      precisionModel.makePrecise(projectedPoint);
      if (projectedPoint.equals2d(point)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check to see if the point in relation to the line between lineStart and
   * lineEnd is between the line end points and is within the maxDistance from
   * the line.
   * 
   * @param precisionModel The precision model.
   * @param lineStart The point at the start of the line.
   * @param lineEnd The point at the end of the line.
   * @param maxDistance The distance the point must be less than from the line.
   * @param point The point.
   * @return True if the point is on the line.
   */
  public static boolean isPointOnLine(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point,
    double maxDistance) {
    double distance = LineSegmentUtil.distance(lineStart, lineEnd, point);
    if (distance < maxDistance) {
      double projectionFactor = projectionFactor(lineStart, lineEnd, point);
      if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
        return true;
      }
    }
    return false;
  }

  public static boolean isPointOnLineMiddle(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point,
    double maxDistance) {
    if (point.equals2d(lineStart)) {
      return false;
    } else if (point.equals2d(lineEnd)) {
      return false;
    } else {
      double distance = LineSegmentUtil.distance(lineStart, lineEnd, point);
      if (distance < maxDistance) {
        double projectionFactor = projectionFactor(lineStart, lineEnd, point);
        if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
          return true;
        }
      }
      return false;
    }
  }

  public static PointLineProjection getPointLineProjection(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates pointCoordinates) {
    return new PointLineProjection(precisionModel, lineStart, lineEnd,
      pointCoordinates);
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
  public static double projectionFactor(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    final Coordinates point) {
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
  public static double projectionFactor(
    final double x1,
    final double y1,
    final double x2,
    final double y2,
    final double x,
    final double y) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final double length = dx * dx + dy * dy;
    final double r = ((x - x1) * dx + (y - y1) * dy) / length;
    return r;
  }

  public double segmentFraction(
    final Coordinates lineStart,
    final Coordinates lineEnd,
    Coordinates point) {
    double segFrac = projectionFactor(lineStart, lineEnd, point);
    if (segFrac < 0.0) {
      return 0.0;
    } else if (segFrac > 1.0) {
      return 1.0;
    } else {
      return segFrac;
    }
  }

  public static void addElevation(
    CoordinatesPrecisionModel precisionModel,
    Coordinates lineStart,
    Coordinates lineEnd,
    Coordinates point) {
    double z = getElevation(lineStart, lineEnd, point);
    point.setZ(z);
    precisionModel.makePrecise(point);
  }
}
