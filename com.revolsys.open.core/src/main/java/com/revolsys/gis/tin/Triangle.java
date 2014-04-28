package com.revolsys.gis.tin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.revolsys.gis.jts.LineSegmentImpl;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.AbstractCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.util.EnvelopeUtil;
import com.revolsys.util.MathUtil;

public class Triangle extends AbstractCoordinatesList {
  private static final long serialVersionUID = -4513931832875328029L;

  public static Triangle createClockwiseTriangle(final Coordinates c0,
    final Coordinates c1, final Coordinates c2) {
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

  private static final com.revolsys.jts.geom.GeometryFactory GEOMETRY_FACTORY = GeometryFactory.getFactory(
    0, 2, 1.0, 1);

  public Triangle() {
  }

  public Triangle(final Coordinates... points) {
    if (points.length > 3) {
      throw new IllegalArgumentException(
        "A traingle must have exeactly 3 points not " + size());
    }
    for (int i = 0; i < 3; i++) {
      final Coordinates point = points[i];
      setPoint(i, point);
    }
  }

  private void addIntersection(final GeometryFactory geometryFactory,
    final Set<Coordinates> coordinates, final Coordinates line1Start,
    final Coordinates line1End, final Coordinates line2Start,
    final Coordinates line2End) {
    final CoordinatesList intersections = LineSegmentUtil.getIntersection(
      geometryFactory, line1Start, line1End, line2Start, line2End);
    for (final Coordinates point : intersections) {
      coordinates.add(point);
    }
  }

  /**
   * Returns true if the coordinate lies inside or on the edge of the Triangle.
   * 
   * @param coordinate The coordinate.
   * @return True if the coordinate lies inside or on the edge of the Triangle.
   */
  @Override
  public boolean contains(final Coordinates coordinate) {
    final int triangleOrientation = CoordinatesUtil.orientationIndex(getP0(),
      getP1(), getP2());
    final int p0p1Orientation = CoordinatesUtil.orientationIndex(getP0(),
      getP1(), coordinate);
    if (p0p1Orientation != triangleOrientation
      && p0p1Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p1p2Orientation = CoordinatesUtil.orientationIndex(getP1(),
      getP2(), coordinate);
    if (p1p2Orientation != triangleOrientation
      && p1p2Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p2p0Orientation = CoordinatesUtil.orientationIndex(getP2(),
      getP0(), coordinate);
    if (p2p0Orientation != triangleOrientation
      && p2p0Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    return true;
  }

  public boolean equals(final Triangle triangle) {
    final HashSet<Coordinates> coords = new HashSet<Coordinates>();
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

    final Coordinates centre = CoordinatesUtil.circumcentre(x1, y1, x2, y2, x3,
      y3);
    final double angleB = MathUtil.angle(x1, y1, x2, y2, x3, y3);
    final double radius = MathUtil.distance(x1, y1, x3, y3) / Math.sin(angleB)
      * 0.5;

    return new Circle(centre, radius);
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
  public Envelope getEnvelopeInternal() {
    double[] bounds = null;
    for (int i = 0; i < 3; i++) {
      final double x = getX(i);
      final double y = getY(i);
      if (bounds == null) {
        bounds = EnvelopeUtil.createBounds(2, x, y);
      } else {
        EnvelopeUtil.expand(null, bounds, 2, x, y);
      }
    }
    return new Envelope(2, bounds);
  }

  public Coordinates getInCentre() {
    final Coordinates a = get(0);
    final Coordinates b = get(1);
    final Coordinates c = get(2);
    // the lengths of the sides, labelled by their opposite vertex
    final double len0 = b.distance(c);
    final double len1 = a.distance(c);
    final double len2 = a.distance(b);
    final double circum = len0 + len1 + len2;

    final double inCentreX = (len0 * a.getX() + len1 * b.getX() + len2
      * c.getX())
      / circum;
    final double inCentreY = (len0 * a.getY() + len1 * b.getY() + len2
      * c.getY())
      / circum;
    return new DoubleCoordinates(inCentreX, inCentreY);
  }

  public Coordinates getP0() {
    return get(0);
  }

  public Coordinates getP1() {
    return get(1);
  }

  public Coordinates getP2() {
    return get(2);
  }

  public Polygon getPolygon(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    final LinearRing shell = geometryFactory.linearRing(new DoubleCoordinatesList(
      getAxisCount(), getP0(), getP1(), getP2(), getP0()));
    return geometryFactory.polygon(shell);
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final int coordinateIndex = getCoordinatesIndex(index, axisIndex);
    return coordinates[coordinateIndex];
  }

  public LineSegment intersection(final GeometryFactory geometryFactory,
    final LineSegment line) {
    final Coordinates lc0 = line.get(0);
    final Coordinates lc1 = line.get(1);
    final boolean lc0Contains = contains(lc0);
    final boolean lc1Contains = contains(lc1);
    if (lc0Contains && lc1Contains) {
      return line;
    } else {
      final Set<Coordinates> coordinates = new HashSet<Coordinates>();
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP0(), getP1());
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP1(), getP2());
      addIntersection(geometryFactory, coordinates, lc0, lc1, getP2(), getP0());

      final Iterator<Coordinates> coordIterator = coordinates.iterator();
      if (coordIterator.hasNext()) {
        final Coordinates c1 = coordIterator.next();
        if (coordIterator.hasNext()) {
          final Coordinates c2 = coordIterator.next();
          if (coordIterator.hasNext()) {
            // TODO Too many intersect
          }
          return new LineSegmentImpl(c1, c2);
        } else {
          return new LineSegmentImpl(c1, c1);
        }
      } else {
        return null;
      }
    }
  }

  public boolean intersectsCircumCircle(final Coordinates point) {
    return getCircumcircle().contains(point);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final int coordinateIndex = getCoordinatesIndex(index, axisIndex);
    coordinates[coordinateIndex] = value;
  }

  @Override
  public int size() {
    return 3;
  }

  @Override
  public String toString() {
    return getPolygon(GEOMETRY_FACTORY).toWkt();
  }
}
