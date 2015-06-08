package com.revolsys.gis.model.coordinates;

import com.revolsys.jts.algorithm.HCoordinate;
import com.revolsys.jts.algorithm.NotRepresentableException;
import com.revolsys.jts.algorithm.RobustDeterminant;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Trig;

public class CoordinatesUtil {

  public static Point average(final Point c1, final Point c2) {
    final int axisCount = Math.min(c1.getAxisCount(), c2.getAxisCount());
    final double[] coordinates = new double[axisCount];
    for (int i = 0; i < axisCount; i++) {
      final double value1 = c1.getCoordinate(i);
      final double value2 = c2.getCoordinate(i);
      double value;
      if (Double.isNaN(value1) || Double.isNaN(value1)) {
        value = value2;
      } else if (Double.isNaN(value2) || Double.isNaN(value2)) {
        value = value1;
      } else {
        value = MathUtil.avg(value1, value2);
      }
      coordinates[i] = value;
    }
    return new PointDouble(coordinates);
  }

  public static Point circumcentre(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3) {
    // compute the perpendicular bisector of chord ab
    final HCoordinate cab = perpendicularBisector(x1, y1, x2, y2);
    // compute the perpendicular bisector of chord bc
    final HCoordinate cbc = perpendicularBisector(x2, y2, x3, y3);
    // compute the intersection of the bisectors (circle radii)
    final HCoordinate hcc = new HCoordinate(cab, cbc);
    Point cc = null;
    try {
      cc = new PointDouble(hcc.getX(), hcc.getY());
    } catch (final NotRepresentableException ex) {
      // MD - not sure what we can do to prevent this (robustness problem)
      // Idea - can we condition which edges we choose?
      throw new IllegalStateException(ex.getMessage() + " POLYGON((" + x1 + " " + y1 + "," + x2
        + " " + y2 + "," + x3 + " " + y3 + "," + x1 + " " + y1 + "))");
    }
    return cc;
  }

  public static int compare(final double x1, final double y1, final double x2, final double y2) {
    if (x1 < x2) {
      return -1;
    } else if (x1 > x2) {
      return 1;
    } else {
      if (y1 < y2) {
        return -1;
      } else if (y1 > y2) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public static int compareToOrigin(final Point point1, final Object other) {
    if (other instanceof Point) {
      final Point point2 = (Point)other;
      return compareToOrigin(point1, point2);
    } else {
      return -1;
    }
  }

  public static int compareToOrigin(final Point point1, final Point point2) {
    final double x = point1.getX();
    final double y = point1.getY();
    final double distance = MathUtil.distance(0, 0, x, y);

    final double otherX = point2.getX();
    final double otherY = point2.getY();
    final double otherDistance = MathUtil.distance(0, 0, otherX, otherY);
    final int distanceCompare = Double.compare(distance, otherDistance);
    if (distanceCompare == 0) {
      final int yCompare = Double.compare(y, otherY);
      return yCompare;
    } else {
      return distanceCompare;
    }
  }

  public static boolean contains(final Iterable<? extends Point> points, final Point point) {
    for (final Point point1 : points) {
      if (point1.equals(point)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   *
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  public static double distance3d(final Point point1, final Point point2) {
    final double dx = point1.getX() - point2.getX();
    final double dy = point1.getY() - point2.getY();
    final double dz = point1.getZ() - point2.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public static boolean equals(final double x1, final double y1, final double x2, final double y2) {
    return x1 == x2 && y1 == y2;
  }

  public static Point get2d(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return null;
    } else {
      final Point point = geometry.getPoint();
      return new PointDouble(point, 2);
    }
  }

  public static int getAxisCount(final Point... points) {
    int axisCount = 2;
    for (final Point point : points) {
      axisCount = Math.max(axisCount, point.getAxisCount());
    }
    return axisCount;
  }

  public static double getElevation(final LineString line, final Point coordinate) {
    final LineString coordinates = line;
    Point previousCoordinate = coordinates.getPoint(0);
    for (int i = 1; i < coordinates.getVertexCount(); i++) {
      final Point currentCoordinate = coordinates.getPoint(i);

      if (LineSegmentUtil.distanceLinePoint(previousCoordinate, currentCoordinate, coordinate) < 1) {
        return LineSegmentUtil.getElevation(previousCoordinate, currentCoordinate, coordinate);
      }
      previousCoordinate = currentCoordinate;
    }
    return Double.NaN;
  }

  public static double getElevation(final Point coordinate, final Point c0, final Point c1) {
    final double fraction = coordinate.distance(c0) / c0.distance(c1);
    final double z = c0.getZ() + (c1.getZ() - c0.getZ()) * fraction;
    return z;
  }

  public static Point getPrecise(final double scale, final Point point) {
    if (scale <= 0) {
      return point;
    } else {
      final double[] coordinates = point.getCoordinates();
      coordinates[0] = MathUtil.makePrecise(scale, coordinates[0]);
      coordinates[1] = MathUtil.makePrecise(scale, coordinates[1]);
      return new PointDouble(coordinates);
    }
  }

  public static boolean isAcute(final Point point1, final Point point2, final Point point3) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    final double x3 = point3.getX();
    final double y3 = point3.getY();

    return MathUtil.isAcute(x1, y1, x2, y2, x3, y3);
  }

  /**
   * Methods for computing and working with octants of the Cartesian plane
   * Octants are numbered as follows:
   *
   * <pre>
   *  \2|1/
   * 3 \|/ 0
   * ---+--
   * 4 /|\ 7
   * /5|6\
   *
   * <pre>
   * If line segments lie along a coordinate axis, the octant is the lower of the two
   * possible values.
   *
   * Returns the octant of a directed line segment (specified as x and y
   * displacements, which cannot both be 0).
   */
  public static int octant(final double dx, final double dy) {
    if (dx == 0.0 && dy == 0.0) {
      throw new IllegalArgumentException("Cannot compute the octant for point ( " + dx + ", " + dy
        + " )");
    }

    final double adx = Math.abs(dx);
    final double ady = Math.abs(dy);

    if (dx >= 0) {
      if (dy >= 0) {
        if (adx >= ady) {
          return 0;
        } else {
          return 1;
        }
      } else { // dy < 0
        if (adx >= ady) {
          return 7;
        } else {
          return 6;
        }
      }
    } else { // dx < 0
      if (dy >= 0) {
        if (adx >= ady) {
          return 3;
        } else {
          return 2;
        }
      } else { // dy < 0
        if (adx >= ady) {
          return 4;
        } else {
          return 5;
        }
      }
    }
  }

  /**
   * Returns the octant of a directed line segment from p0 to p1.
   */
  public static int octant(final Point p0, final Point p1) {
    final double dx = p1.getX() - p0.getX();
    final double dy = p1.getY() - p0.getY();
    if (dx == 0.0 && dy == 0.0) {
      throw new IllegalArgumentException("Cannot compute the octant for two identical points " + p0);
    }
    return octant(dx, dy);
  }

  public static Point offset(final Point coordinate, final double angle, final double distance) {
    final double newX = coordinate.getX() + distance * Math.cos(angle);
    final double newY = coordinate.getY() + distance * Math.sin(angle);
    final Point newCoordinate = new PointDouble(newX, newY);
    return newCoordinate;

  }

  public static int orientationIndex(final Point p1, final Point p2, final Point q) {
    // travelling along p1->p2, turn counter clockwise to get to q return 1,
    // travelling along p1->p2, turn clockwise to get to q return -1,
    // p1, p2 and q are colinear return 0.
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double qX = q.getX();
    final double qY = q.getY();
    final double dx1 = x2 - x1;
    final double dy1 = y2 - y1;
    final double dx2 = qX - x2;
    final double dy2 = qY - y2;
    return RobustDeterminant.signOfDet2x2(dx1, dy1, dx2, dy2);
  }

  public static HCoordinate perpendicularBisector(final double x1, final double y1,
    final double x2, final double y2) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final HCoordinate l1 = new HCoordinate(x1 + dx / 2.0, y1 + dy / 2.0, 1.0);
    final HCoordinate l2 = new HCoordinate(x1 - dy + dx / 2.0, y1 + dx + dy / 2.0, 1.0);
    return new HCoordinate(l1, l2);
  }

  /**
   * Return the first point of points1 not in points2
   * @author Paul Austin <paul.austin@revolsys.com>
   * @param points1
   * @param points2
   * @return
   */
  public static Point pointNotInList(final Iterable<? extends Point> points1,
    final Iterable<? extends Point> points2) {
    for (final Point point : points1) {
      if (contains(points2, point)) {
        return point;
      }
    }
    return null;
  }

  public static Point setElevation(final Point newLocation, final Point originalLocation) {
    if (originalLocation.getAxisCount() > 2) {
      final double z = originalLocation.getZ();
      if (Double.isNaN(z)) {
        return newLocation;
      } else {
        final double[] points = originalLocation.getCoordinates();
        points[2] = z;
        final Point newCoordinates = new PointDouble(points);
        return newCoordinates;
      }
    } else {
      return newLocation;
    }
  }

  public static float[] toFloatArray(final LineString points, final int axisCount) {
    final float[] coordinates = new float[axisCount * points.getVertexCount()];
    for (int i = 0; i < points.getVertexCount(); i++) {
      for (int axis = 0; axis < axisCount; axis++) {
        coordinates[i * axisCount + axis] = (float)points.getCoordinate(i, axis);
      }
    }
    return coordinates;
  }

  public static Point translate(final Point point, final Double angle, final double length) {
    final double x = point.getX();
    final double y = point.getY();

    final double newX = Trig.adjacent(x, angle, length);
    final double newY = Trig.opposite(y, angle, length);

    final Point newPoint = new PointDouble(newX, newY);
    return newPoint;
  }
}
