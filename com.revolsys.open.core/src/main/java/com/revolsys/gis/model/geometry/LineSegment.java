package com.revolsys.gis.model.geometry;

import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.projection.ProjectionFactory;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.AbstractCoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

public class LineSegment extends AbstractCoordinatesList implements
  Comparable<LineSegment> {
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

  private final double[] points;

  private GeometryFactory geometryFactory;

  public LineSegment() {
    points = new double[6];
  }

  public LineSegment(final Coordinates coordinates1,
    final Coordinates coordinates2) {
    this(FACTORY, coordinates1, coordinates2);
  }

  public LineSegment(final double x1, final double y1, final double x2,
    final double y2) {
    this(FACTORY, x1, y1, x2, y2);
  }

  public LineSegment(final GeometryFactory geometryFactory,
    final Coordinates coordinates1, final Coordinates coordinates2) {
    this.geometryFactory = geometryFactory;
    final int numAxis = Math.max(coordinates1.getNumAxis(),
      coordinates2.getNumAxis());
    points = new double[numAxis * 2];
    for (int i = 0; i < numAxis; i++) {
      setValue(0, i, coordinates1.getValue(i));
      setValue(1, i, coordinates2.getValue(i));
    }
  }

  public LineSegment(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2) {
    this.geometryFactory = geometryFactory;
    this.points = new double[] {
      x1, y1, x2, y2
    };
  }

  public LineSegment(final GeometryFactory geometryFactory,
    final LineSegment line) {
    this(geometryFactory, line.get(0), line.get(1));
  }

  public LineSegment(final LineSegment line) {
    this(line.getGeometryFactory(), line.get(0), line.get(1));
  }

  public LineSegment(final LineString line) {
    this(GeometryFactory.getFactory(line), CoordinatesListUtil.get(line, 0),
      CoordinatesListUtil.get(line, line.getNumPoints() - 1));
  }

  public double angle() {
    return Math.atan2(getCoordinates2().getY() - getCoordinates1().getY(),
      getCoordinates2().getX() - getCoordinates1().getX());
  }

  @Override
  public LineSegment clone() {
    return new LineSegment(geometryFactory, getCoordinates1(),
      getCoordinates2());
  }

  public Coordinates closestPoint(final Coordinates p) {
    final double factor = projectionFactor(p);
    if (factor > 0 && factor < 1) {
      return project(p);
    }
    final double dist0 = getCoordinates1().distance(p);
    final double dist1 = getCoordinates2().distance(p);
    if (dist0 < dist1) {
      return getCoordinates1();
    } else {
      return getCoordinates2();
    }
  }

  public Coordinates[] closestPoints(final LineSegment line) {
    // test for intersection
    final Coordinates intPt = intersection(line);
    if (intPt != null) {
      return new Coordinates[] {
        intPt, intPt
      };
    }

    /**
     * if no intersection closest pair contains at least one endpoint. Test each
     * endpoint in turn.
     */
    final Coordinates[] closestPt = new Coordinates[2];
    double minDistance = Double.MAX_VALUE;
    double dist;

    final Coordinates close00 = closestPoint(line.get(0));
    minDistance = close00.distance(line.get(0));
    closestPt[0] = close00;
    closestPt[1] = line.get(0);

    final Coordinates close01 = closestPoint(line.get(1));
    dist = close01.distance(line.get(1));
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = close01;
      closestPt[1] = line.get(1);
    }

    final Coordinates close10 = line.closestPoint(get(0));
    dist = close10.distance(get(0));
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = get(0);
      closestPt[1] = close10;
    }

    final Coordinates close11 = line.closestPoint(get(0));
    dist = close11.distance(get(0));
    if (dist < minDistance) {
      minDistance = dist;
      closestPt[0] = get(0);
      closestPt[1] = close11;
    }

    return closestPt;
  }

  @Override
  public int compareTo(final LineSegment other) {
    int compare = get(0).compareTo(other.get(0));
    if (compare == 0) {
      compare = get(1).compareTo(other.get(1));
    }
    return compare;
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

  public LineSegment convert(final GeometryFactory geometryFactory) {
    if (geometryFactory == this.geometryFactory) {
      return this;
    } else {
      final Coordinates point1 = ProjectionFactory.convert(getCoordinates1(),
        this.geometryFactory, geometryFactory);
      final Coordinates point2 = ProjectionFactory.convert(getCoordinates2(),
        this.geometryFactory, geometryFactory);
      return new LineSegment(geometryFactory, point1, point2);
    }
  }

  public double distance(final Coordinates p) {
    return LineSegmentUtil.distance(getCoordinates1(), getCoordinates2(), p);
  }

  public double distance(final LineSegment lineSegment) {
    return LineSegmentUtil.distance(getCoordinates1(), getCoordinates2(),
      lineSegment.getCoordinates1(), lineSegment.getCoordinates2());
  }

  public LineSegment extend(final double startDistance, final double endDistance) {
    final double angle = angle();
    final Coordinates c1 = CoordinatesUtil.offset(getCoordinates1(), angle,
      -startDistance);
    final Coordinates c2 = CoordinatesUtil.offset(getCoordinates2(), angle,
      endDistance);
    return new LineSegment(c1, c2);

  }

  public BoundingBox getBoundingBox() {
    return BoundingBox.getBoundingBox(getLine());
  }

  private Coordinates getCoordinates1() {
    final byte numAxis = getNumAxis();
    final Coordinates coordinates = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value = getValue(0, i);
      coordinates.setValue(i, value);
    }
    return coordinates;
  }

  private Coordinates getCoordinates2() {
    final byte numAxis = getNumAxis();
    final Coordinates coordinates = new DoubleCoordinates(numAxis);
    for (int i = 0; i < numAxis; i++) {
      final double value = getValue(1, i);
      coordinates.setValue(i, value);
    }
    return coordinates;
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
    return CoordinatesUtil.getElevation(point, getCoordinates1(),
      getCoordinates2());
  }

  public Envelope getEnvelope() {
    return getLine().getEnvelopeInternal();
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  public LineSegment getIntersection(BoundingBox boundingBox) {
    boundingBox = boundingBox.convert(geometryFactory);
    final boolean contains1 = boundingBox.contains(getCoordinates1());
    final boolean contains2 = boundingBox.contains(getCoordinates2());
    if (contains1) {
      if (contains2) {
        return this;
      } else {
        final Coordinates c1 = getCoordinates1();
        final Coordinates c2 = getCrossing(getCoordinates2(),
          getCoordinates1(), boundingBox);
        return new LineSegment(geometryFactory, c1, c2);
      }
    } else {
      if (contains2) {
        final Coordinates c1 = getCrossing(getCoordinates1(),
          getCoordinates2(), boundingBox);
        final Coordinates c2 = getCoordinates2();
        return new LineSegment(geometryFactory, c1, c2);
      } else {
        final Coordinates c1 = getCrossing(getCoordinates1(),
          getCoordinates2(), boundingBox);
        final Coordinates c2 = getCrossing(getCoordinates2(),
          getCoordinates1(), boundingBox);
        return new LineSegment(geometryFactory, c1, c2);
      }
    }
  }

  public CoordinatesList getIntersection(final Coordinates point1,
    final Coordinates point2) {
    final CoordinatesList intersection = LineSegmentUtil.getIntersection(
      geometryFactory, getCoordinates1(), getCoordinates2(), point1, point2);
    return intersection;
  }

  public CoordinatesList getIntersection(
    final CoordinatesPrecisionModel precisionModel,
    final LineSegment lineSegment2) {
    return LineSegmentUtil.getIntersection(geometryFactory, getCoordinates1(),
      getCoordinates2(), lineSegment2.getCoordinates1(),
      lineSegment2.getCoordinates2());
  }

  public CoordinatesList getIntersection(final LineSegment lineSegment2) {
    final CoordinatesList intersection = LineSegmentUtil.getIntersection(
      geometryFactory, getCoordinates1(), getCoordinates2(),
      lineSegment2.getCoordinates1(), lineSegment2.getCoordinates2());
    return intersection;
  }

  /**
   * Computes the length of the line segment.
   * 
   * @return the length of the line segment
   */
  public double getLength() {
    return getCoordinates1().distance(getCoordinates2());
  }

  public LineString getLine() {
    return geometryFactory.createLineString(this);
  }

  @Override
  public byte getNumAxis() {
    return (byte)(points.length / 2);
  }

  public Point getPoint(final int i) {
    final Coordinates coordinates = get(i);
    return geometryFactory.createPoint(coordinates);
  }

  @Override
  public double getValue(final int index, final int axisIndex) {
    final byte numAxis = getNumAxis();
    if (axisIndex >= 0 && axisIndex < numAxis) {
      if (index >= 0 && index < 2) {
        final int valueIndex = index * numAxis + axisIndex;
        final double value = points[valueIndex];
        return value;
      }
    }
    return Double.NaN;
  }

  public Coordinates intersection(final LineSegment line) {
    final CoordinatesList intersection = getIntersection(line);
    if (intersection.size() == 0) {
      return null;
    } else {
      return intersection.get(0);
    }
  }

  public boolean intersects(final BoundingBox boundingBox) {
    final Coordinates p1 = getCoordinates1();
    final Coordinates p2 = getCoordinates2();
    if (boundingBox.intersects(p1)) {
      return true;
    } else if (boundingBox.intersects(p2)) {
      return true;
    } else {
      final CoordinatesList cornerPoints = boundingBox.getCornerPoints();
      for (int i = 0; i < 4; i++) {
        final Coordinates bp1 = cornerPoints.get(i);
        final Coordinates bp2 = cornerPoints.get((i + 1) % 4);
        if (LineSegmentUtil.intersects(p1, p2, bp1, bp2)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean intersects(final Coordinates point, final double maxDistance) {
    return LineSegmentUtil.isPointOnLine(getCoordinates1(), getCoordinates2(),
      point, maxDistance);
  }

  public boolean isEmpty() {
    return getCoordinates1() == null || getCoordinates2() == null;
  }

  /**
   * Tests whether the segment is horizontal.
   * 
   * @return <code>true</code> if the segment is horizontal
   */
  public boolean isHorizontal() {
    return getCoordinates1().getY() == getCoordinates2().getY();
  }

  public boolean isPointOnLineMiddle(final Coordinates point,
    final double maxDistance) {
    return LineSegmentUtil.isPointOnLineMiddle(getCoordinates1(),
      getCoordinates2(), point, maxDistance);
  }

  /**
   * Tests whether the segment is vertical.
   * 
   * @return <code>true</code> if the segment is vertical
   */
  public boolean isVertical() {
    return getCoordinates1().getX() == getCoordinates2().getY();
  }

  public boolean isWithinDistance(final Coordinates point,
    final double maxDistance) {
    final double distance = distance(point);
    return distance <= maxDistance;
  }

  public boolean isWithinDistance(final Point point, final double maxDistance) {
    final Coordinates coordinates = CoordinatesUtil.getInstance(point);
    return isWithinDistance(coordinates, maxDistance);
  }

  public int orientationIndex(final LineSegment seg) {
    final int orient0 = CoordinatesUtil.orientationIndex(getCoordinates1(),
      getCoordinates2(), seg.getCoordinates1());
    final int orient1 = CoordinatesUtil.orientationIndex(getCoordinates1(),
      getCoordinates2(), seg.getCoordinates2());
    // this handles the case where the points are L or collinear
    if (orient0 >= 0 && orient1 >= 0) {
      return Math.max(orient0, orient1);
    }
    // this handles the case where the points are R or collinear
    if (orient0 <= 0 && orient1 <= 0) {
      return Math.max(orient0, orient1);
    }
    // points lie on opposite sides ==> indeterminate orientation
    return 0;
  }

  // TODO add 3D
  public Coordinates pointAlongOffset(final double segmentLengthFraction,
    final double offsetDistance) {
    final double x1 = getX(0);
    final double x2 = getX(1);
    final double dx = x2 - x1;

    final double y1 = getY(0);
    final double y2 = getY(1);
    final double dy = y2 - y1;

    // the point on the segment line
    double x = x1 + segmentLengthFraction * (dx);
    double y = y1 + segmentLengthFraction * (dy);

    final double len = Math.sqrt(dx * dx + dy * dy);
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

    final DoubleCoordinates coord = new DoubleCoordinates(x, y);
    return coord;
  }

  public Coordinates project(final Coordinates p) {
    final Coordinates newPoint = LineSegmentUtil.project(getGeometryFactory(),
      getCoordinates1(), getCoordinates2(), p);
    return newPoint;
  }

  public double projectionFactor(final Coordinates p) {
    return LineSegmentUtil.projectionFactor(getCoordinates1(),
      getCoordinates2(), p);
  }

  public void setCoordinates(final Coordinates s0, final Coordinates s1) {
    setPoint(0, s0);
    setPoint(1, s1);
  }

  public void setElevationOnPoint(
    final CoordinatesPrecisionModel precisionModel, final Coordinates point) {
    final double z = getElevation(point);
    point.setZ(z);
    precisionModel.makePrecise(point);
  }

  @Override
  public void setValue(final int index, final int axisIndex, final double value) {
    final byte numAxis = getNumAxis();
    if (axisIndex >= 0 && axisIndex < numAxis) {
      if (index >= 0 && index < 2) {
        final int valueIndex = index * numAxis + axisIndex;
        points[valueIndex] = value;
      }
    }
  }

  @Override
  public int size() {
    return 2;
  }

  @Override
  public String toString() {
    if (geometryFactory == null) {
      return super.toString();
    } else {
      return "SRID=" + geometryFactory.getSrid() + ";" + super.toString();
    }
  }

  public boolean touchesEnd(final LineSegment closestSegment) {
    if (contains(closestSegment.get(0))) {
      return true;
    } else if (contains(closestSegment.get(1))) {
      return true;
    }
    return false;
  }
}
