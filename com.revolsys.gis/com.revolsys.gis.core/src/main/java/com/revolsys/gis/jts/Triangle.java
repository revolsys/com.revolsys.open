package com.revolsys.gis.jts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.revolsys.gis.tin.Circle;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;

public class Triangle extends com.vividsolutions.jts.geom.Triangle {
  /**
   * Create a new Triangle with the coordinates in a clockwise direction.
   * 
   * @param c0 The first coordinate.
   * @param c1 The second coordinate.
   * @param c2 The third coordinate.
   * @return The Triangle.
   */
  public static Triangle createClockwiseTriangle(
    final Coordinate c0,
    final Coordinate c1,
    final Coordinate c2) {
    try {
      if (CGAlgorithms.computeOrientation(c0, c1, c2) == CGAlgorithms.CLOCKWISE) {
        return new Triangle(c0, c1, c2);
      } else {
        return new Triangle(c0, c2, c1);
      }
    } catch (final IllegalStateException e) {
      throw e;
    }

  }

  private Circle circumcircle;

  private final Envelope envelope;

  /**
   * Construct a new Triangle.
   * 
   * @param c0 The first coordinate.
   * @param c1 The second coordinate.
   * @param c2 The third coordinate.
   */
  public Triangle(
    final Coordinate c0,
    final Coordinate c1,
    final Coordinate c2) {
    super(c0, c1, c2);
    envelope = new Envelope(c0, c1);
    envelope.expandToInclude(c2);
    createCircumcircle();
  }

  private void addIntersection(
    final Set<Coordinate> coordinates,
    final Coordinate lc0,
    final Coordinate lc1,
    final Coordinate c0,
    final Coordinate c1) {
    final RobustLineIntersector intersector = new RobustLineIntersector();
    intersector.computeIntersection(lc0, lc1, c0, c1);
    for (int i = 0; i < intersector.getIntersectionNum(); i++) {
      coordinates.add(intersector.getIntersection(i));
    }
  }

  /**
   * Returns true if the coordinate lies inside or on the edge of the Triangle.
   * 
   * @param coordinate The coordinate.
   * @return True if the coordinate lies inside or on the edge of the Triangle.
   */
  public boolean contains(
    final Coordinate coordinate) {
    final int triangleOrientation = CGAlgorithms.computeOrientation(p0, p1, p2);
    final int p0p1Orientation = CGAlgorithms.computeOrientation(p0, p1,
      coordinate);
    if (p0p1Orientation != triangleOrientation
      && p0p1Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p1p2Orientation = CGAlgorithms.computeOrientation(p1, p2,
      coordinate);
    if (p1p2Orientation != triangleOrientation
      && p1p2Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    final int p2p0Orientation = CGAlgorithms.computeOrientation(p2, p0,
      coordinate);
    if (p2p0Orientation != triangleOrientation
      && p2p0Orientation != CGAlgorithms.COLLINEAR) {
      return false;
    }
    return true;
  }

  private void createCircumcircle() {
    final double angleB = Angle.angleBetween(p0, p1, p2);

    final double radius = p0.distance(p2) / Math.sin(angleB) * 0.5;
    final Coordinate coordinate = getCircumcentre();
    circumcircle = new Circle(coordinate, radius);
  }

  public boolean equals(
    final Triangle triangle) {
    final HashSet<Coordinate> coords = new HashSet<Coordinate>();
    coords.add(triangle.p0);
    coords.add(triangle.p1);
    coords.add(triangle.p2);
    coords.add(p0);
    coords.add(p1);
    coords.add(p2);
    return coords.size() == 3;
  }

  /**
   * Computes the circumcentre of a triangle. The circumcentre is the centre of
   * the circumcircle, the smallest circle which encloses the triangle.
   * 
   * @return The circumcentre of the triangle.
   */
  public Coordinate getCircumcentre() {
    return circumcentre(p0, p1, p2);
  }

  /**
   * Computes the circumcircle of a triangle. The circumcircle is the smallest
   * circle which encloses the triangle.
   * 
   * @return The circumcircle of the triangle.
   */
  public Circle getCircumcircle() {
    return circumcircle;
  }

  public Coordinate[] getCoordinates() {
    return new Coordinate[] {
      p0, p1, p2
    };
  }

  /**
   * Get the envelope of the Triangle.
   * 
   * @return The envelope.
   */
  public Envelope getEnvelopeInternal() {
    return envelope;
  }

  public Polygon getPolygon() {
    final GeometryFactory factory = new GeometryFactory();
    final LinearRing shell = factory.createLinearRing(new Coordinate[] {
      p0, p1, p2, p0
    });
    return factory.createPolygon(shell, null);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + p0.hashCode();
    result = prime * result + p1.hashCode();
    result = prime * result + p2.hashCode();
    return result;
  }

  public LineSegment intersection(
    final LineSegment line) {
    final Coordinate lc0 = line.p0;
    final Coordinate lc1 = line.p1;
    final boolean lc0Contains = contains(lc0);
    final boolean lc1Contains = contains(lc1);
    if (lc0Contains && lc1Contains) {
      return line;
    } else {
      final Set<Coordinate> coordinates = new HashSet<Coordinate>();
      addIntersection(coordinates, lc0, lc1, p0, p1);
      addIntersection(coordinates, lc0, lc1, p1, p2);
      addIntersection(coordinates, lc0, lc1, p2, p0);

      final Iterator<Coordinate> coordIterator = coordinates.iterator();
      if (coordIterator.hasNext()) {
        final Coordinate c1 = coordIterator.next();
        if (coordIterator.hasNext()) {
          final Coordinate c2 = coordIterator.next();
          if (coordIterator.hasNext()) {
            // TODO Too many intersect
          }
          return new LineSegment(c1, c2);
        } else {
          return new LineSegment(c1, c1);
        }
      } else {
        return null;
      }
    }
  }

  @Override
  public String toString() {
    return new WKTWriter(3).write(getPolygon());
  }
}
