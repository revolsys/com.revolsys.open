package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.data.equals.NumberEquals;
import com.revolsys.jts.algorithm.Angle;
import com.revolsys.jts.algorithm.HCoordinate;
import com.revolsys.jts.algorithm.NotRepresentableException;
import com.revolsys.jts.algorithm.RobustDeterminant;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Trig;

public class CoordinatesUtil {

  public static Coordinates add(final Coordinates c1, final Coordinates c2) {
    final int numAxis = Math.min(c1.getNumAxis(), c2.getNumAxis());
    final Coordinates newPoint = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value1 = c1.getValue(i);
      final double value2 = c2.getValue(i);
      final double value = value1 + value2;
      newPoint.setValue(i, value);
    }
    return newPoint;
  }

  public static Coordinates add(final Coordinates coordinates,
    final double... deltas) {
    final Coordinates newCoordinates = new DoubleCoordinates(coordinates);
    for (int i = 0; i < deltas.length; i++) {
      final double delta = deltas[i];
      final double oldValue = coordinates.getValue(i);
      final double newValue = oldValue + delta;
      newCoordinates.setValue(i, newValue);
    }
    return null;
  }

  public static Point add(final Point c1, final Point c2) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(c1);
    final Point p2 = (Point)factory.createGeometry(c2);
    return factory.createPoint(add(getInstance(c1), getInstance(p2)));
  }

  public static double angle(final Coordinates p1, final Coordinates p2,
    final Coordinates p3) {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double x3 = p3.getX();
    final double y3 = p3.getY();
    return MathUtil.angle(x1, y1, x2, y2, x3, y3);
  }

  public static double angle2d(final Coordinates point1,
    final Coordinates point2) {
    final double dx = point2.getX() - point1.getX();
    final double dy = point2.getY() - point1.getY();
    final double angle = Math.atan2(dy, dx);
    return angle;
  }

  /**
   * Returns the oriented smallest angle between two vectors. The computed angle
   * will be in the range (-Pi, Pi]. A positive result corresponds to a
   * counterclockwise rotation from v1 to v2; a negative result corresponds to a
   * clockwise rotation.
   * 
   * @param tip1 the tip of v1
   * @param tail the tail of each vector
   * @param tip2 the tip of v2
   * @return the angle between v1 and v2, relative to v1
   */
  public static double angleBetweenOriented(final Coordinates tip1,
    final Coordinates tail, final Coordinates tip2) {
    final double a1 = tail.angle2d(tip1);
    final double a2 = tail.angle2d(tip2);
    final double angDel = a2 - a1;

    // normalize, maintaining orientation
    if (angDel <= -Math.PI) {
      return angDel + Angle.PI_TIMES_2;
    } else if (angDel > Math.PI) {
      return angDel - Angle.PI_TIMES_2;
    } else {
      return angDel;
    }
  }

  public static Coordinates average(final Coordinates c1, final Coordinates c2) {
    final int numAxis = Math.min(c1.getNumAxis(), c2.getNumAxis());
    final Coordinates newPoint = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value1 = c1.getValue(i);
      final double value2 = c2.getValue(i);
      double value;
      if (Double.isNaN(value1) || Double.isNaN(value1)) {
        value = value2;
      } else if (Double.isNaN(value2) || Double.isNaN(value2)) {
        value = value1;
      } else {
        value = MathUtil.avg(value1, value2);
      }
      newPoint.setValue(i, value);
    }
    return newPoint;
  }

  public static Coordinates circumcentre(final double x1, final double y1,
    final double x2, final double y2, final double x3, final double y3) {
    // compute the perpendicular bisector of chord ab
    final HCoordinate cab = perpendicularBisector(x1, y1, x2, y2);
    // compute the perpendicular bisector of chord bc
    final HCoordinate cbc = perpendicularBisector(x2, y2, x3, y3);
    // compute the intersection of the bisectors (circle radii)
    final HCoordinate hcc = new HCoordinate(cab, cbc);
    Coordinates cc = null;
    try {
      cc = new DoubleCoordinates(hcc.getX(), hcc.getY());
    } catch (final NotRepresentableException ex) {
      // MD - not sure what we can do to prevent this (robustness problem)
      // Idea - can we condition which edges we choose?
      throw new IllegalStateException(ex.getMessage() + " POLYGON((" + x1 + " "
        + y1 + "," + x2 + " " + y2 + "," + x3 + " " + y3 + "," + x1 + " " + y1
        + "))");
    }
    return cc;
  }

  public static int compareTo(final Coordinates point1, final Coordinates point2) {
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

  public static int compareTo(final Coordinates point1, final Object other) {
    if (other instanceof Coordinates) {
      final Coordinates point2 = (Coordinates)other;
      return compareTo(point1, point2);
    } else {
      return -1;
    }
  }

  /**
   * Computes the 2-dimensional Euclidean distance to another location.
   * The Z-ordinate is ignored.
   * 
   * @param c a point
   * @return the 2-dimensional Euclidean distance between the locations
   */
  public static double distance(final Coordinates point1,
    final Coordinates point2) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    return MathUtil.distance(x1, y1, x2, y2);
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   * 
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  public static double distance3d(final Coordinates point1,
    final Coordinates point2) {
    final double dx = point1.getX() - point2.getX();
    final double dy = point1.getY() - point2.getY();
    final double dz = point1.getZ() - point2.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  public static Coordinates divide(final Coordinates c1, final double d) {
    final int numAxis = c1.getNumAxis();
    final Coordinates newPoint = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value1 = c1.getValue(i);
      final double value = value1 / d;
      newPoint.setValue(i, value);
    }
    return newPoint;
  }

  public static boolean equals(final Coordinates point,
    final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double coordinate = coordinates[i];
      if (coordinate != point.getValue(i)) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals(final double x1, final double y1,
    final double x2, final double y2) {
    return x1 == x2 && y1 == y2;
  }

  public static boolean equals2d(final Coordinates point1,
    final Coordinates point2) {
    if (NumberEquals.equal(point1.getX(), point2.getX())) {
      if (NumberEquals.equal(point1.getY(), point2.getY())) {
        return true;
      }
    }
    return false;
  }

  public static boolean equals3d(final Coordinates point1,
    final Coordinates point2) {
    if (equals2d(point1, point2)) {
      if (NumberEquals.equal(point1.getZ(), point2.getZ())) {
        return true;
      }
    }
    return false;
  }

  public static Coordinates get(final Coordinates coordinate) {
    if (Double.isNaN(coordinate.getZ())) {
      return new DoubleCoordinates(coordinate.getX(), coordinate.getY());
    } else {
      return new DoubleCoordinates(coordinate.getX(), coordinate.getY(),
        coordinate.getZ());
    }
  }

  public static Coordinates get(final Geometry geometry) {
    if (geometry == null || geometry.isEmpty()) {
      return null;
    } else {
      final CoordinatesList points = CoordinatesListUtil.get(geometry);
      return points.get(0);
    }
  }

  public static Coordinates get2d(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return null;
    } else {
      final Coordinates point = get(geometry);
      return new DoubleCoordinates(point, 2);
    }
  }

  public static double[] getCoordinates(final Coordinates point) {
    final double[] coordinates = new double[point.getNumAxis()];
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = point.getValue(i);
    }
    return coordinates;
  }

  public static double getElevation(final Coordinates coordinate,
    final Coordinates c0, final Coordinates c1) {
    final double fraction = coordinate.distance(c0) / c0.distance(c1);
    final double z = c0.getZ() + (c1.getZ() - c0.getZ()) * (fraction);
    return z;
  }

  @Deprecated
  public static Coordinates getInstance(final Point point) {
    if (point == null || point.isEmpty()) {
      return null;
    } else {
      return point;
    }
  }

  public static int getNumAxis(final Coordinates... points) {
    int numAxis = 2;
    for (final Coordinates point : points) {
      numAxis = Math.max(numAxis, point.getNumAxis());
    }
    return numAxis;
  }

  public static double getX(final Coordinates point) {
    if (point == null) {
      return Double.NaN;
    } else {
      return point.getX();
    }
  }

  public static double getY(final Coordinates point) {
    if (point == null) {
      return Double.NaN;
    } else {
      return point.getY();
    }
  }

  public static int hashCode(final Coordinates point) {
    int result = 17;
    result = 37 * result + hashCode(point.getX());
    result = 37 * result + hashCode(point.getY());
    return result;
  }

  public static int hashCode(final double d) {
    final long f = Double.doubleToLongBits(d);
    return (int)(f ^ (f >>> 32));
  }

  public static boolean isAcute(final Coordinates point1,
    final Coordinates point2, final Coordinates point3) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    final double x3 = point3.getX();
    final double y3 = point3.getY();

    return MathUtil.isAcute(x1, y1, x2, y2, x3, y3);
  }

  public static Coordinates multiply(final double d, final Coordinates c1) {
    final int numAxis = c1.getNumAxis();
    final Coordinates newPoint = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value1 = c1.getValue(i);
      final double value = value1 * d;
      newPoint.setValue(i, value);
    }
    return newPoint;
  }

  /**
   * Returns the octant of a directed line segment from p0 to p1.
   */
  public static int octant(final Coordinates p0, final Coordinates p1) {
    final double dx = p1.getX() - p0.getX();
    final double dy = p1.getY() - p0.getY();
    if (dx == 0.0 && dy == 0.0) {
      throw new IllegalArgumentException(
        "Cannot compute the octant for two identical points " + p0);
    }
    return octant(dx, dy);
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
      throw new IllegalArgumentException(
        "Cannot compute the octant for point ( " + dx + ", " + dy + " )");
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

  public static Coordinates offset(final Coordinates coordinate,
    final double angle, final double distance) {
    final double newX = coordinate.getX() + distance * Math.cos(angle);
    final double newY = coordinate.getY() + distance * Math.sin(angle);
    final Coordinates newCoordinate = new DoubleCoordinates(newX, newY);
    return newCoordinate;

  }

  public static int orientationIndex(final Coordinates p1,
    final Coordinates p2, final Coordinates q) {
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

  public static HCoordinate perpendicularBisector(final double x1,
    final double y1, final double x2, final double y2) {
    final double dx = x2 - x1;
    final double dy = y2 - y1;
    final HCoordinate l1 = new HCoordinate(x1 + dx / 2.0, y1 + dy / 2.0, 1.0);
    final HCoordinate l2 = new HCoordinate(x1 - dy + dx / 2.0, y1 + dx + dy
      / 2.0, 1.0);
    return new HCoordinate(l1, l2);
  }

  public static Coordinates setElevation(final Coordinates newLocation,
    final Coordinates originalLocation) {
    if (originalLocation.getNumAxis() > 2) {
      final double z = originalLocation.getZ();
      if (Double.isNaN(z)) {
        return newLocation;
      } else {
        final Coordinates newCoordinates = new DoubleCoordinates(newLocation,
          originalLocation.getNumAxis());
        newCoordinates.setZ(z);
        return newCoordinates;
      }
    } else {
      return newLocation;
    }
  }

  public static Coordinates subtract(final Coordinates c1, final Coordinates c2) {
    final int numAxis = Math.min(c1.getNumAxis(), c2.getNumAxis());
    final Coordinates newPoint = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value1 = c1.getValue(i);
      final double value2 = c2.getValue(i);
      final double value = value1 - value2;
      newPoint.setValue(i, value);
    }
    return newPoint;
  }

  public static Point subtract(final Point c1, final Point c2) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(c1);
    final Point p2 = (Point)factory.createGeometry(c2);
    return factory.createPoint(subtract(getInstance(c1), getInstance(p2)));
  }

  public static float[] toFloatArray(final CoordinatesList points,
    final int numAxis) {
    final float[] coordinates = new float[numAxis * points.size()];
    for (int i = 0; i < points.size(); i++) {
      for (int axis = 0; axis < numAxis; axis++) {
        coordinates[i * numAxis + axis] = (float)points.getValue(i, axis);
      }
    }
    return coordinates;
  }

  public static Coordinates translate(final Coordinates point,
    final Double angle, final double length) {
    final double x = point.getX();
    final double y = point.getY();

    final double newX = Trig.adjacent(x, angle, length);
    final double newY = Trig.opposite(y, angle, length);

    final Coordinates newPoint = new DoubleCoordinates(newX, newY);
    return newPoint;
  }
}
