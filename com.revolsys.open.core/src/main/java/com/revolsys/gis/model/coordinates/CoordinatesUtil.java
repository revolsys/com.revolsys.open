package com.revolsys.gis.model.coordinates;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.algorithm.HCoordinate;
import com.vividsolutions.jts.algorithm.NotRepresentableException;
import com.vividsolutions.jts.algorithm.RobustDeterminant;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

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

  public static Point add(final Point c1, final Point c2) {
    final GeometryFactory factory = GeometryFactory.getFactory(c1);
    final Point p2 = (Point)factory.createGeometry(c2);
    return factory.createPoint(add(get(c1), get(p2)));
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
  public static int octant(double dx, double dy) {
    if (dx == 0.0 && dy == 0.0)
      throw new IllegalArgumentException(
        "Cannot compute the octant for point ( " + dx + ", " + dy + " )");

    double adx = Math.abs(dx);
    double ady = Math.abs(dy);

    if (dx >= 0) {
      if (dy >= 0) {
        if (adx >= ady)
          return 0;
        else
          return 1;
      } else { // dy < 0
        if (adx >= ady)
          return 7;
        else
          return 6;
      }
    } else { // dx < 0
      if (dy >= 0) {
        if (adx >= ady)
          return 3;
        else
          return 2;
      } else { // dy < 0
        if (adx >= ady)
          return 4;
        else
          return 5;
      }
    }
  }

  /**
   * Returns the octant of a directed line segment from p0 to p1.
   */
  public static int octant(Coordinates p0, Coordinates p1) {
    double dx = p1.getX() - p0.getX();
    double dy = p1.getY() - p0.getY();
    if (dx == 0.0 && dy == 0.0)
      throw new IllegalArgumentException(
        "Cannot compute the octant for two identical points " + p0);
    return octant(dx, dy);
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

  public static Coordinates average(final Coordinates c1, final Coordinates c2) {
    final int numAxis = Math.min(c1.getNumAxis(), c2.getNumAxis());
    final Coordinates newPoint = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value1 = c1.getValue(i);
      final double value2 = c2.getValue(i);
      final double value = MathUtil.avg(value1, value2);
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

  public static double distance(final Coordinates point1,
    final Coordinates point2) {
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final double x2 = point2.getX();
    final double y2 = point2.getY();
    return MathUtil.distance(x1, y1, x2, y2);
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

  public static boolean equals(final double x1, final double y1,
    final double x2, final double y2) {
    return x1 == x2 && y1 == y2;
  }

  public static Coordinates get(final Coordinate coordinate) {
    if (Double.isNaN(coordinate.z)) {
      return new DoubleCoordinates(coordinate.z, coordinate.y);
    } else {
      return new DoubleCoordinates(coordinate.z, coordinate.y, coordinate.z);
    }
  }

  public static Coordinates get(final Geometry geometry) {
    if (geometry.isEmpty()) {
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

  public static double getElevation(final Coordinates coordinate,
    final Coordinates c0, final Coordinates c1) {
    final double fraction = coordinate.distance(c0) / c0.distance(c1);
    final double z = c0.getZ() + (c1.getZ() - c0.getZ()) * (fraction);
    return z;
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
    final GeometryFactory factory = GeometryFactory.getFactory(c1);
    final Point p2 = (Point)factory.createGeometry(c2);
    return factory.createPoint(subtract(get(c1), get(p2)));
  }

  public static Coordinate toCoordinate(final Coordinates point) {
    return new Coordinate(point.getX(), point.getY(), point.getZ());
  }
}
