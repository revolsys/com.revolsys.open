package com.revolsys.gis.model.geometry;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.visitor.Visitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.AbstractCoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;

public class LineSegment extends AbstractCoordinatesList {
  private static final GeometryFactory FACTORY = new GeometryFactory();

  public static void visit(
    final LineString line,
    final Visitor<LineSegment> visitor) {
    final CoordinatesList coords = CoordinatesListUtil.get(line);
    Coordinates previousCoordinate = coords.get(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinates coordinate = coords.get(i);
      final LineSegment segment = new LineSegment(
        GeometryFactory.getFactory(line), previousCoordinate, coordinate);
      if (segment.getLength() > 0) {
        if (!visitor.visit(segment)) {
          return;
        }
      }
      previousCoordinate = coordinate;
    }
  }

  private final Coordinates coordinates1;

  private final Coordinates coordinates2;

  private final GeometryFactory geometryFactory;

  private LineString line;

  public LineSegment(
    final Coordinates coordinates1,
    final Coordinates coordinates2) {
    this(FACTORY, coordinates1, coordinates2);
  }

  public LineSegment(
    final GeometryFactory geometryFactory,
    final Coordinates coordinates1,
    final Coordinates coordinates2) {
    this.geometryFactory = geometryFactory;
    this.coordinates1 = coordinates1;
    this.coordinates2 = coordinates2;
  }

  @Override
  public LineSegment clone() {
    return new LineSegment(geometryFactory, coordinates1, coordinates2);
  }

  public double distance(
    final Coordinates p) {
    return LineSegmentUtil.distance(p, coordinates1, coordinates2);
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
    return coordinates1.distance(coordinates2);
  }

  public LineString getLine() {
    if (line == null) {
      line = geometryFactory.createLineString(this);
    }
    return line;
  }

  public Coordinates project(
    final Coordinates p) {
    return LineSegmentUtil.project(coordinates1, coordinates2,
      projectionFactor(p));
  }

  public double projectionFactor(
    final Coordinates p) {
    return LineSegmentUtil.projectionFactor(coordinates1, coordinates2, p);
  }

  public int size() {
    return 2;
  }

  @Override
  public String toString() {
    return getLine().toString();
  }

  public byte getNumAxis() {
    return (byte)Math.max(coordinates1.getNumAxis(), coordinates2.getNumAxis());
  }

  public double getValue(
    int index,
    int axisIndex) {
    switch (index) {
      case 0:
        return coordinates1.getValue(axisIndex);
      case 1:
        return coordinates2.getValue(axisIndex);

      default:
        return 0;
    }
  }

  public void setValue(
    int index,
    int axisIndex,
    double value) {
    switch (index) {
      case 0:
        coordinates1.setValue(axisIndex, value);
      break;
      case 1:
        coordinates2.setValue(axisIndex, value);
      break;
      default:
    }
  }

  public boolean contains(
    Coordinates coordinate) {
    if (get(0).equals(coordinate)) {
      return true;
    } else if (get(1).equals(coordinate)) {
      return true;
    } else {
      return false;
    }
  }

}
