package com.revolsys.geometry.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.HCoordinate;
import com.revolsys.geometry.algorithm.NotRepresentableException;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.Circle;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.TriangleLinearRing;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.geometry.util.Triangles;
import com.revolsys.math.Angle;
import com.revolsys.util.Exceptions;
import com.revolsys.util.MathUtil;

public interface Triangle extends Polygon {
  static void addIntersection(final GeometryFactory geometryFactory, final Set<Point> coordinates,
    final Point line1Start, final Point line1End, final Point line2Start, final Point line2End) {
    final LineString intersections = LineSegmentUtil.getIntersection(geometryFactory, line1Start,
      line1End, line2Start, line2End);
    for (int i = 0; i < intersections.getVertexCount(); i++) {
      final Point point = intersections.getPoint(i);
      coordinates.add(point);
    }
  }

  static double getCircumcircleRadius(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3) {
    final double angleB = Angle.angleBetween(x1, y1, x2, y2, x3, y3);
    final double distanceP1P3 = MathUtil.distance(x1, y1, x3, y3);
    final double radius = distanceP1P3 / Math.sin(angleB) * 0.5;
    return radius;
  }

  default boolean circumcircleContains(final double x, final double y) {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);
    final HCoordinate hcc = CoordinatesUtil.getCircumcentreHCoordinate(x1, y1, x2, y2, x3, y3);
    try {
      final double centreX = hcc.getX();
      final double centreY = hcc.getY();

      final double distanceFromCentre = MathUtil.distance(centreX, centreY, x, y);
      final double circumcircleRadius = getCircumcircleRadius(x1, y1, x2, y2, x3, y3);
      return distanceFromCentre < circumcircleRadius + 0.0001;

    } catch (final NotRepresentableException e) {
      throw Exceptions.wrap("Cannot get circumcentre for " + this, e);
    }
  }

  default boolean equals(final Triangle triangle) {
    final HashSet<Point> coords = new HashSet<>();
    coords.add(triangle.getP0());
    coords.add(triangle.getP1());
    coords.add(triangle.getP2());
    coords.add(getP0());
    coords.add(getP1());
    coords.add(getP2());
    return coords.size() == 3;
  }

  /**
   * Computes the 2D area of this triangle. The area value is always
   * non-negative.
   *
   * @return the area of this triangle
   *
   * @see #signedArea()
   */
  @Override
  default double getArea() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double x3 = getX(2);
    final double y3 = getY(2);
    return Triangles.area(x1, y1, x2, y2, x3, y3);
  }

  /**
   * Computes the 3D area of this triangle. The value computed is always
   * non-negative.
   *
   * @return the 3D area of this triangle
   */
  default double getArea3D() {
    final double x1 = getX(0);
    final double y1 = getY(0);
    final double z1 = getZ(0);
    final double x2 = getX(1);
    final double y2 = getY(1);
    final double z2 = getZ(2);
    final double x3 = getX(2);
    final double y3 = getY(2);
    final double z3 = getZ(2);
    return Triangles.area3D(x1, y1, z1, x2, y2, z2, x3, y3, z3);
  }

  @Override
  default int getAxisCount() {
    return 3;
  }

  default Point getCircumcentre() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);
    final HCoordinate hcc = CoordinatesUtil.getCircumcentreHCoordinate(x1, y1, x2, y2, x3, y3);
    try {
      final double x = hcc.getX();
      final double y = hcc.getY();
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point(x, y);
    } catch (final NotRepresentableException e) {
      throw Exceptions.wrap("Cannot get circumcentre for " + this, e);
    }
  }

  /**
   * Computes the circumcircle of a triangle. The circumcircle is the smallest
   * circle which encloses the triangle.
   *
   * @return The circumcircle of the triangle.
   */
  default Circle getCircumcircle() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);

    final Point centre = getCircumcentre();
    final double radius = getCircumcircleRadius(x1, y1, x2, y2, x3, y3);

    return new Circle(centre, radius);
  }

  default BoundingBox getCircumcircleBoundingBox() {
    final Point centre = getCircumcentre();
    final double centreX = centre.getX();
    final double centreY = centre.getY();

    final double radius = getCircumcircleRadius();
    final double minX = centreX - radius;
    final double minY = centreY - radius;
    final double maxX = centreX + radius;
    final double maxY = centreY + radius;
    final GeometryFactory geometryFactory = getGeometryFactory();
    double[] bounds = {
      minX, minY, maxX, maxY
    };
    return geometryFactory.newBoundingBox(2, bounds);
  }

  default double getCircumcircleRadius() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);

    return getCircumcircleRadius(x1, y1, x2, y2, x3, y3);
  }

  double getCoordinate(int vertexIndex, int axisIndex);

  @Override
  default double getCoordinate(final int ringIndex, final int vertexIndex, final int axisIndex) {
    if (ringIndex == 0) {
      return getCoordinate(vertexIndex, axisIndex);
    } else {
      return Double.NaN;
    }
  }

  double[] getCoordinates();

  @Override
  default GeometryFactory getGeometryFactory() {
    final int axisCount = getAxisCount();
    return GeometryFactory.floating(0, axisCount);
  }

  default Point getInCentre() {
    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);

    final double len0 = MathUtil.distance(x2, y2, x3, y3);
    final double len1 = MathUtil.distance(x1, y1, x3, y3);
    final double len2 = MathUtil.distance(x1, y1, x2, y2);
    final double circum = len0 + len1 + len2;

    final double inCentreX = (len0 * x1 + len1 * x2 + len2 * x3) / circum;
    final double inCentreY = (len0 * y1 + len1 * y2 + len2 * y3) / circum;
    return new PointDouble(inCentreX, inCentreY);
  }

  default double getM(final int vertexIndex) {
    return getCoordinate(vertexIndex, M);
  }

  default Point getP0() {
    return getPoint(0);
  }

  default Point getP1() {
    return getPoint(1);
  }

  default Point getP2() {
    return getPoint(2);
  }

  default Point getPoint(int vertexIndex) {
    while (vertexIndex < 0) {
      vertexIndex += getVertexCount();
    }
    if (vertexIndex > getVertexCount()) {
      return null;
    } else {
      final int axisCount = getAxisCount();
      final double[] coordinates = new double[axisCount];
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[axisIndex] = getCoordinate(vertexIndex, axisIndex);
      }
      final GeometryFactory geometryFactory = getGeometryFactory();
      return geometryFactory.point(coordinates);
    }
  }

  @Override
  default LinearRing getRing(final int ringIndex) {
    if (ringIndex == 0) {
      return new TriangleLinearRing(this);
    } else {
      return null;
    }
  }

  @Override
  default int getRingCount() {
    return 1;
  }

  @Override
  default List<LinearRing> getRings() {
    return Collections.singletonList(new TriangleLinearRing(this));
  }

  @Override
  default int getVertexCount() {
    return 4;
  }

  default double getX(final int vertexIndex) {
    return getCoordinate(vertexIndex, X);
  }

  default double getY(final int vertexIndex) {
    return getCoordinate(vertexIndex, Y);
  }

  default double getZ(final int vertexIndex) {
    return getCoordinate(vertexIndex, Z);
  }

  /**
   * Returns true if the coordinate lies inside or on the edge of the TriangleImpl.
   *
   * @param coordinate The coordinate.
   * @return True if the coordinate lies inside or on the edge of the TriangleImpl.
   */
  default boolean hasVertex(final Point point) {
    final double x = point.getX();
    final double y = point.getY();

    final double x1 = getCoordinate(0, X);
    final double y1 = getCoordinate(0, Y);

    final double x2 = getCoordinate(1, X);
    final double y2 = getCoordinate(1, Y);

    final double x3 = getCoordinate(2, X);
    final double y3 = getCoordinate(2, Y);

    final int triangleOrientation = CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x3, y3);
    final int p0p1Orientation = CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x, y);
    if (p0p1Orientation != triangleOrientation && p0p1Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    } else {
      final int p1p2Orientation = CoordinatesListUtil.orientationIndex(x2, y2, x3, y3, x, y);
      if (p1p2Orientation != triangleOrientation && p1p2Orientation != CGAlgorithms.COLLINEAR) {
        return false;
      } else {
        final int p2p0Orientation = CoordinatesListUtil.orientationIndex(x3, y3, x1, y1, x, y);
        if (p2p0Orientation != triangleOrientation && p2p0Orientation != CGAlgorithms.COLLINEAR) {
          return false;
        }
      }
    }
    return true;
  }

  default LineSegment intersection(final GeometryFactory geometryFactory, final LineSegment line) {
    final Point lc0 = line.getPoint(0);
    final Point lc1 = line.getPoint(1);
    final boolean lc0Contains = hasVertex(lc0);
    final boolean lc1Contains = hasVertex(lc1);
    if (lc0Contains && lc1Contains) {
      return line;
    } else {
      final Set<Point> coordinates = new HashSet<>();
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

  default boolean intersectsCircumCircle(final Point point) {
    final double x = point.getX();
    final double y = point.getY();
    return circumcircleContains(x, y);
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

}
