package com.revolsys.gis.model.geometry;

import com.revolsys.gis.data.visitor.Visitor;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

public class LineSegment implements CoordinateSequence {
  public static void visit(
    final LineString line,
    final Visitor<LineSegment> visitor) {
    final CoordinateSequence coords = line.getCoordinateSequence();
    Coordinate previousCoordinate = coords.getCoordinate(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinate coordinate = coords.getCoordinate(i);
      final LineSegment segment = new LineSegment(line.getFactory(),
        previousCoordinate, coordinate);
      if (segment.getLength() > 0) {
        if (!visitor.visit(segment)) {
          return;
        }
      }
      previousCoordinate = coordinate;
    }
  }

  private final Coordinate coordinate1;

  private final Coordinate coordinate2;

  private final GeometryFactory geometryFactory;

  private LineString line;

  public LineSegment(
    final GeometryFactory geometryFactory,
    final Coordinate coordinate1,
    final Coordinate coordinate2) {
    this.geometryFactory = geometryFactory;
    this.coordinate1 = coordinate1;
    this.coordinate2 = coordinate2;
  }

  @Override
  public LineSegment clone() {
    return new LineSegment(geometryFactory, coordinate1, coordinate2);
  }

  public double distance(
    final Coordinate p) {
    return CGAlgorithms.distancePointLine(p, coordinate1, coordinate2);
  }

  public Envelope expandEnvelope(
    final Envelope env) {
    env.expandToInclude(coordinate1);
    env.expandToInclude(coordinate2);
    return env;
  }

  public Coordinate getCoordinate(
    final int i) {
    switch (i) {
      case 0:
        return coordinate1;
      case 1:
        return coordinate2;
      default:
        return null;
    }
  }

  public void getCoordinate(
    final int index,
    final Coordinate coord) {
    final Coordinate coordinate = getCoordinate(index);
    coord.setCoordinate(coordinate);
  }

  public Coordinate getCoordinateCopy(
    final int i) {
    return new Coordinate(getCoordinate(i));
  }

  public int getDimension() {
    return 3;
  }

  public Envelope getEnvelope() {
    return getLine().getEnvelopeInternal();
  }

  /**
   * Computes the length of the line segment.
   * 
   * @return the length of the line segment
   */
  public double getLength() {
    return coordinate1.distance(coordinate2);
  }

  public LineString getLine() {
    if (line == null) {
      line = geometryFactory.createLineString(this);
    }
    return line;
  }

  public double getOrdinate(
    final int index,
    final int ordinateIndex) {
    final Coordinate coordinate = getCoordinate(index);
    switch (ordinateIndex) {
      case 0:
        return coordinate.x;
      case 1:
        return coordinate.y;
      case 2:
        return coordinate.z;
      default:
        return 0;
    }
  }

  public double getX(
    final int index) {
    return getOrdinate(index, 0);
  }

  public double getY(
    final int index) {
    return getOrdinate(index, 1);
  }

  public Coordinate project(
    final Coordinate p) {
    if (p.equals(coordinate1) || p.equals(coordinate2)) {
      return new Coordinate(p);
    }

    final double r = projectionFactor(p);
    final Coordinate coord = new Coordinate();
    coord.x = coordinate1.x + r * (coordinate2.x - coordinate1.x);
    coord.y = coordinate1.y + r * (coordinate2.y - coordinate1.y);
    return coord;
  }

  public double projectionFactor(
    final Coordinate p) {
    if (p.equals(coordinate1)) {
      return 0.0;
    }
    if (p.equals(coordinate2)) {
      return 1.0;
    }
    // Otherwise, use comp.graphics.algorithms Frequently Asked Questions method
    /*
     * AC dot AB r = --------- ||AB||^2 r has the following meaning: r=0 P = A
     * r=1 P = B r<0 P is on the backward extension of AB r>1 P is on the
     * forward extension of AB 0<r<1 P is interior to AB
     */
    final double dx = coordinate2.x - coordinate1.x;
    final double dy = coordinate2.y - coordinate1.y;
    final double len2 = dx * dx + dy * dy;
    final double r = ((p.x - coordinate1.x) * dx + (p.y - coordinate1.y) * dy)
      / len2;
    return r;
  }

  public void setOrdinate(
    final int index,
    final int ordinateIndex,
    final double value) {
    final Coordinate coordinate = getCoordinate(index);
    switch (ordinateIndex) {
      case 0:
        coordinate.x = value;
      break;
      case 1:
        coordinate.y = value;
      break;
      case 2:
        coordinate.z = value;
      break;
      default:
      break;
    }
  }

  public int size() {
    return 2;
  }

  public Coordinate[] toCoordinateArray() {
    return new Coordinate[] {
      coordinate1, coordinate2
    };
  }

  @Override
  public String toString() {
    return getLine().toString();
  }
}
