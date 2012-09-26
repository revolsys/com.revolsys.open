package com.revolsys.gis.model.geometry;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.AbstractCoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

public class LineSegment extends AbstractCoordinatesList {
  private static final long serialVersionUID = 3905321662159212931L;

  private static final GeometryFactory FACTORY = GeometryFactory.getFactory();

  public static void visit(final LineString line,
    final Visitor<LineSegment> visitor) {
    final CoordinatesList coords = CoordinatesListUtil.get(line);
    Coordinates previousCoordinate = coords.get(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinates coordinate = coords.get(i);
      final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
      final LineSegment segment = new LineSegment(geometryFactory,
        previousCoordinate, coordinate);
      if (segment.getLength() > 0) {
        if (!visitor.visit(segment)) {
          return;
        }
      }
      previousCoordinate = coordinate;
    }
  }

  private Coordinates coordinates1;

  private Coordinates coordinates2;

  private GeometryFactory geometryFactory;

  public LineSegment() {
  }

  public LineSegment(final Coordinates coordinates1,
    final Coordinates coordinates2) {
    this(FACTORY, coordinates1, coordinates2);
  }

  public LineSegment(final double x1, final double y1, final double x2,
    final double y2) {
    this(new DoubleCoordinates(x1, y1), new DoubleCoordinates(x2, y2));
  }

  public LineSegment(final GeometryFactory geometryFactory,
    final Coordinates coordinates1, final Coordinates coordinates2) {
    this.geometryFactory = geometryFactory;
    this.coordinates1 = new DoubleCoordinates(coordinates1);
    this.coordinates2 = new DoubleCoordinates(coordinates2);
  }

  public LineSegment(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2) {
    this(geometryFactory, geometryFactory.createCoordinates(x1, y1),
      geometryFactory.createCoordinates(x2, y2));
  }

  public LineSegment(final GeometryFactory geometryFactory,
    final LineSegment line) {
    this(geometryFactory, line.get(0), line.get(1));
  }

  public LineSegment(final LineSegment line) {
    this(line.get(0), line.get(1));
  }

  public LineSegment(final LineString line) {
    this(GeometryFactory.getFactory(line), CoordinatesListUtil.get(line, 0),
      CoordinatesListUtil.get(line, line.getNumPoints() - 1));
  }

  public double angle() {
    return Math.atan2(coordinates2.getY() - coordinates1.getY(),
      coordinates2.getX() - coordinates1.getX());
  }

  @Override
  public LineSegment clone() {
    return new LineSegment(geometryFactory, coordinates1, coordinates2);
  }

  public Coordinates closestPoint(final Coordinates p) {
    final double factor = projectionFactor(p);
    if (factor > 0 && factor < 1) {
      return project(p);
    }
    final double dist0 = coordinates1.distance(p);
    final double dist1 = coordinates2.distance(p);
    if (dist0 < dist1) {
      return coordinates1;
    } else {
      return coordinates2;
    }
  }

  @Override
  public boolean contains(final Coordinates coordinate) {
    if (get(0).equals(coordinate)) {
      return true;
    } else if (get(1).equals(coordinate)) {
      return true;
    } else {
      return false;
    }
  }

  public double distance(final Coordinates p) {
    return LineSegmentUtil.distance(coordinates1, coordinates2, p);
  }

  public double distance(final LineSegment lineSegment) {
    return LineSegmentUtil.distance(coordinates1, coordinates2,
      lineSegment.coordinates1, lineSegment.coordinates2);
  }

  public LineSegment extend(final double startDistance, final double endDistance) {
    final double angle = angle();
    final Coordinates c1 = CoordinatesUtil.offset(coordinates1, angle,
      -startDistance);
    final Coordinates c2 = CoordinatesUtil.offset(coordinates2, angle,
      endDistance);
    return new LineSegment(c1, c2);

  }

  private Coordinates getCrossing(final Coordinates coordinates1,
    final Coordinates coordinates2, final BoundingBox boundingBox) {
    Coordinates intersection = null;
    final Polygon polygon = boundingBox.toPolygon(1);
    final LineString ring = polygon.getExteriorRing();
    final CoordinatesList points = CoordinatesListUtil.get(ring);
    for (int i = 0; i < 4; i++) {
      final Coordinates ringC1 = points.get(i);
      final Coordinates ringC2 = points.get(i);
      final CoordinatesList currentIntersections = LineSegmentUtil.getIntersection(
        geometryFactory, coordinates1, coordinates2, ringC1, ringC2);
      if (currentIntersections.size() == 1) {
        final Coordinates currentIntersection = currentIntersections.get(0);
        if (intersection == null) {
          intersection = currentIntersection;
        } else if (coordinates1.distance(currentIntersection) < coordinates1.distance(intersection)) {
          intersection = currentIntersection;
        }
      }
    }
    return intersection;
  }

  public double getElevation(final Coordinates point) {
    return CoordinatesUtil.getElevation(point, coordinates1, coordinates2);
  }

  public Envelope getEnvelope() {
    return getLine().getEnvelopeInternal();
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public LineSegment getIntersection(BoundingBox boundingBox) {
    boundingBox = boundingBox.convert(geometryFactory);
    final boolean contains1 = boundingBox.contains(coordinates1);
    final boolean contains2 = boundingBox.contains(coordinates2);
    if (contains1) {
      if (contains2) {
        return this;
      } else {
        final Coordinates c1 = coordinates1;
        final Coordinates c2 = getCrossing(coordinates2, coordinates1,
          boundingBox);
        return new LineSegment(geometryFactory, c1, c2);
      }
    } else {
      if (contains2) {
        final Coordinates c1 = getCrossing(coordinates1, coordinates2,
          boundingBox);
        final Coordinates c2 = coordinates2;
        return new LineSegment(geometryFactory, c1, c2);
      } else {
        final Coordinates c1 = getCrossing(coordinates1, coordinates2,
          boundingBox);
        final Coordinates c2 = getCrossing(coordinates2, coordinates1,
          boundingBox);
        return new LineSegment(geometryFactory, c1, c2);
      }
    }
  }

  public CoordinatesList getIntersection(
    final CoordinatesPrecisionModel precisionModel,
    final LineSegment lineSegment2) {
    return LineSegmentUtil.getIntersection(geometryFactory, coordinates1,
      coordinates2, lineSegment2.coordinates1, lineSegment2.coordinates2);
  }

  public CoordinatesList getIntersection(final LineSegment lineSegment2) {
    final CoordinatesList intersection = LineSegmentUtil.getIntersection(
      geometryFactory, coordinates1, coordinates2, lineSegment2.coordinates1,
      lineSegment2.coordinates2);
    return intersection;
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
    return geometryFactory.createLineString(this);
  }

  @Override
  public byte getNumAxis() {
    return (byte)Math.max(coordinates1.getNumAxis(), coordinates2.getNumAxis());
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    switch (index) {
      case 0:
        return coordinates1.getValue(axisIndex);
      case 1:
        return coordinates2.getValue(axisIndex);

      default:
        return 0;
    }
  }

  public boolean intersects(final BoundingBox boundingBox) {
    return (boundingBox.contains(coordinates1) || boundingBox.contains(coordinates2));
  }

  public boolean intersects(final Coordinates point, final double maxDistance) {
    return LineSegmentUtil.isPointOnLine(coordinates1, coordinates2, point,
      maxDistance);
  }

  public boolean isEmpty() {
    return coordinates1 == null || coordinates2 == null;
  }

  public boolean isPointOnLineMiddle(final Coordinates point,
    final double maxDistance) {
    return LineSegmentUtil.isPointOnLineMiddle(coordinates1, coordinates2,
      point, maxDistance);
  }

  public Coordinates project(final Coordinates p) {
    return LineSegmentUtil.project(coordinates1, coordinates2,
      projectionFactor(p));
  }

  public double projectionFactor(final Coordinates p) {
    return LineSegmentUtil.projectionFactor(coordinates1, coordinates2, p);
  }

  public void setElevationOnPoint(
    final CoordinatesPrecisionModel precisionModel, final Coordinates point) {
    final double z = getElevation(point);
    point.setZ(z);
    precisionModel.makePrecise(point);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
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

  @Override
  public int size() {
    return 2;
  }

  public int orientationIndex(LineSegment seg) {
    int orient0 = CoordinatesUtil.orientationIndex(coordinates1, coordinates2,
      seg.coordinates1);
    int orient1 = CoordinatesUtil.orientationIndex(coordinates1, coordinates2,
      seg.coordinates2);
    // this handles the case where the points are L or collinear
    if (orient0 >= 0 && orient1 >= 0)
      return Math.max(orient0, orient1);
    // this handles the case where the points are R or collinear
    if (orient0 <= 0 && orient1 <= 0)
      return Math.max(orient0, orient1);
    // points lie on opposite sides ==> indeterminate orientation
    return 0;
  }

  /**
   * Tests whether the segment is horizontal.
   * 
   * @return <code>true</code> if the segment is horizontal
   */
  public boolean isHorizontal() {
    return coordinates1.getY() == coordinates2.getY();
  }

  /**
   * Tests whether the segment is vertical.
   * 
   * @return <code>true</code> if the segment is vertical
   */
  public boolean isVertical() {
    return coordinates1.getX() == coordinates2.getY();
  }

  public void setCoordinates(Coordinates s0, Coordinates s1) {
    setPoint(0, s0);
    setPoint(1, s1);
  }

  // TODO add 3D
  public Coordinates pointAlongOffset(double segmentLengthFraction,
    double offsetDistance) {
    double x1 = getX(0);
    double x2 = getX(1);
    double dx = x2 - x1;

    double y1 = getY(0);
    double y2 = getY(1);
    double dy = y2 - y1;

    // the point on the segment line
    double x = x1 + segmentLengthFraction * (dx);
    double y = y1 + segmentLengthFraction * (dy);

    double len = Math.sqrt(dx * dx + dy * dy);
    if (offsetDistance != 0.0) {
      if (len <= 0.0) {
        throw new IllegalStateException(
          "Cannot compute offset from zero-length line segment");
      }
      double ux = 0.0;
      double uy = 0.0;

      // u is the vector that is the length of the offset, in the direction of
      // the segment
      ux = offsetDistance * dx / len;
      uy = offsetDistance * dy / len;
      // the offset point is the seg point plus the offset vector rotated 90
      // degrees CCW
      x = x - uy;
      y = y + ux;
    }

    DoubleCoordinates coord = new DoubleCoordinates(x, y);
    return coord;
  }
}
