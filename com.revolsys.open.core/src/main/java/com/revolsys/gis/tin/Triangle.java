package com.revolsys.gis.tin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.AbstractLineString;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDoubleGF;
import com.revolsys.jts.util.BoundingBoxUtil;
import com.revolsys.math.Angle;
import com.revolsys.util.MathUtil;

public class Triangle extends AbstractLineString {
  private static final long serialVersionUID = -4513931832875328029L;

  private static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.fixed(0, 1.0);

  public static Triangle createClockwiseTriangle(final Point c0, final Point c1, final Point c2) {
    try {
      if (CoordinatesUtil.orientationIndex(c0, c1, c2) == CGAlgorithms.CLOCKWISE) {
        return new Triangle(c0, c1, c2);
      } else {
        return new Triangle(c0, c2, c1);
      }
    } catch (final IllegalStateException e) {
      throw e;
    }

  }

  private final double[] coordinates = new double[9];

  public Triangle() {
  }

  public Triangle(final Point... points) {
    if (points.length > 3) {
      throw new IllegalArgumentException("A traingle must have exeactly 3 points not "
        + getVertexCount());
    }
    for (int i = 0; i < 3; i++) {
      final Point point = points[i];
      this.coordinates[i * 3] = point.getX();
      this.coordinates[i * 3 + 1] = point.getY();
      this.coordinates[i * 3 + 2] = point.getZ();
    }
  }

  private void addIntersection(final GeometryFactory geometryFactory, final Set<Point> coordinates,
    final Point line1Start, final Point line1End, final Point line2Start, final Point line2End) {
    final LineString intersections = LineSegmentUtil.getIntersection(geometryFactory, line1Start,
      line1End, line2Start, line2End);
    for (int i = 0; i < intersections.getVertexCount(); i++) {
      final Point point = intersections.getPoint(i);
      coordinates.add(point);
    }
  }

  public boolean equals(final Triangle triangle) {
    final HashSet<Point> coords = new HashSet<Point>();
    coords.add(triangle.getP0());
    coords.add(triangle.getP1());
    coords.add(triangle.getP2());
    coords.add(getP0());
    coords.add(getP1());
    coords.add(getP2());
    return coords.size() == 3;
  }

  @Override
  public int getAxisCount() {
    return 3;
  }

  /**
   * Computes the circumcircle of a triangle. The circumcircle is the smallest
   * circle which encloses the triangle.
   *
   * @return The circumcircle of the triangle.
   */
  public Circle getCircumcircle() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double x3 = getX(2);
    final double y3 = getY(2);

    final Point centre = CoordinatesUtil.circumcentre(x1, y1, x2, y2, x3, y3);
    final double angleB = Angle.angle(x1, y1, x2, y2, x3, y3);
    final double radius = MathUtil.distance(x1, y1, x3, y3) / Math.sin(angleB) * 0.5;

    return new Circle(centre, radius);
  }

  @Override
  public double getCoordinate(final int index, final int axisIndex) {
    final int coordinateIndex = getCoordinatesIndex(index, axisIndex);
    return this.coordinates[coordinateIndex];
  }

  @Override
  public double[] getCoordinates() {
    return this.coordinates;
  }

  private int getCoordinatesIndex(final int index, int axisIndex) {
    final byte axisCount = 3;
    axisIndex = axisIndex % 3;
    if (axisIndex < 0) {
      axisIndex = 3 - axisIndex;
    }

    final int coordinateIndex = index * axisCount + axisIndex;
    return coordinateIndex;
  }

  /**
   * Get the envelope of the Triangle.
   *
   * @return The envelope.
   */
  public BoundingBoxDoubleGf getEnvelopeInternal() {
    double[] bounds = null;
    for (int i = 0; i < 3; i++) {
      final double x = getX(i);
      final double y = getY(i);
      if (bounds == null) {
        bounds = BoundingBoxUtil.createBounds(2, x, y);
      } else {
        BoundingBoxUtil.expand(null, bounds, 2, x, y);
      }
    }
    return new BoundingBoxDoubleGf(2, bounds);
  }

  public Point getInCentre() {
    final Point a = getPoint(0);
    final Point b = getPoint(1);
    final Point c = getPoint(2);
    // the lengths of the sides, labelled by their opposite vertex
    final double len0 = b.distance(c);
    final double len1 = a.distance(c);
    final double len2 = a.distance(b);
    final double circum = len0 + len1 + len2;

    final double inCentreX = (len0 * a.getX() + len1 * b.getX() + len2 * c.getX()) / circum;
    final double inCentreY = (len0 * a.getY() + len1 * b.getY() + len2 * c.getY()) / circum;
    return new PointDouble(inCentreX, inCentreY);
  }

  public Point getP0() {
    return getPoint(0);
  }

  public Point getP1() {
    return getPoint(1);
  }

  public Point getP2() {
    return getPoint(2);
  }

  public Polygon getPolygon(final GeometryFactory geometryFactory) {
    final LinearRing shell = geometryFactory.linearRing(getP0(), getP1(), getP2(), getP0());
    return geometryFactory.polygon(shell);
  }

  @Override
  public int getVertexCount() {
    return 3;
  }

  /**
   * Returns true if the coordinate lies inside or on the edge of the Triangle.
   *
   * @param coordinate The coordinate.
   * @return True if the coordinate lies inside or on the edge of the Triangle.
   */
  @Override
  public boolean hasVertex(final Point coordinate) {
    final int triangleOrientation = CoordinatesUtil.orientationIndex(getP0(), getP1(), getP2());
    final int p0p1Orientation = CoordinatesUtil.orientationIndex(getP0(), getP1(), coordinate);
    if (p0p1Orientation != triangleOrientation && p0p1Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p1p2Orientation = CoordinatesUtil.orientationIndex(getP1(), getP2(), coordinate);
    if (p1p2Orientation != triangleOrientation && p1p2Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p2p0Orientation = CoordinatesUtil.orientationIndex(getP2(), getP0(), coordinate);
    if (p2p0Orientation != triangleOrientation && p2p0Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    return true;
  }

  public LineSegment intersection(final GeometryFactory geometryFactory, final LineSegment line) {
    final Point lc0 = line.getPoint(0);
    final Point lc1 = line.getPoint(1);
    final boolean lc0Contains = hasVertex(lc0);
    final boolean lc1Contains = hasVertex(lc1);
    if (lc0Contains && lc1Contains) {
      return line;
    } else {
      final Set<Point> coordinates = new HashSet<Point>();
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP0(), getP1());
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP1(), getP2());
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP2(), getP0());

      final Iterator<Point> coordIterator = coordinates.iterator();
      if (coordIterator.hasNext()) {
        final Point c1 = coordIterator.next();
        if (coordIterator.hasNext()) {
          final Point c2 = coordIterator.next();
          if (coordIterator.hasNext()) {
            // TODO Too many intersect
          }
          return new LineSegmentDoubleGF(c1, c2);
        } else {
          return new LineSegmentDoubleGF(c1, c1);
        }
      } else {
        return null;
      }
    }
  }

  public boolean intersectsCircumCircle(final Point point) {
    return getCircumcircle().contains(point);
  }

  @Override
  public boolean isEmpty() {
    return this.coordinates == null;
  }

  @Override
  public String toString() {
    return getPolygon(GEOMETRY_FACTORY).toWkt();
  }
}
