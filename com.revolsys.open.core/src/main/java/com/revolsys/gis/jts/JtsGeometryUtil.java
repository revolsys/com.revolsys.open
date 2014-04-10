package com.revolsys.gis.jts;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.revolsys.gis.model.coordinates.list.InPlaceIterator;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.gis.model.data.equals.Geometry3DExactEquals;
import com.revolsys.jts.algorithm.Angle;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.IntersectionMatrix;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.operation.linemerge.LineMerger;
import com.revolsys.util.JavaBeanUtil;

public final class JtsGeometryUtil {
  private static final PrecisionModel ELEVATION_PRECISION_MODEL = new PrecisionModel(
    1);

  public static final String FEATURE_PROPERTY = "feature";

  static {
    Geometry3DExactEquals.addExclude(FEATURE_PROPERTY);
  }

  /**
   * Add a evelation (z) value for a coordinate that is on a line segment.
   * 
   * @param coordinate The Coordinate.
   * @param line The line segment the coordinate is on.
   */
  public static void addElevation(final Coordinate coordinate,
    final LineSegment line) {
    final double z = getElevation(line, coordinate);
    coordinate.z = z;
  }

  public static void addElevation(final Coordinate coordinate,
    final LineString line) {
    final CoordinatesList coordinates = line.getCoordinatesList();
    Coordinate previousCoordinate = coordinates.getCoordinate(0);
    for (int i = 1; i < coordinates.size(); i++) {
      final Coordinate currentCoordinate = coordinates.getCoordinate(i);
      final LineSegment3D segment = new LineSegment3D(previousCoordinate,
        currentCoordinate);
      if (segment.distance(coordinate) < 1) {
        final PrecisionModel precisionModel = line.getGeometryFactory()
          .getPrecisionModel();
        addElevation(precisionModel, coordinate, segment);
        return;
      }
      previousCoordinate = currentCoordinate;
    }

  }

  private static void addElevation(final LineString original,
    final LineString update) {
    final Coordinate c0 = update.getCoordinateN(0);
    if (Double.isNaN(c0.z)) {
      addElevation(c0, original);
    }
    final Coordinate cN = update.getCoordinateN(update.getNumPoints() - 1);
    if (Double.isNaN(cN.z)) {
      addElevation(cN, original);
    }
  }

  public static void addElevation(final PrecisionModel precisionModel,
    final Coordinate coordinate, final LineSegment3D line) {
    addElevation(coordinate, line);
    coordinate.z = precisionModel.makePrecise(coordinate.z);

  }

  public static LineSegment addLength(final LineSegment line,
    final double startDistance, final double endDistance) {
    final double angle = line.angle();
    final Coordinate c1 = offset(line.p0, angle, -startDistance);
    final Coordinate c2 = offset(line.p1, angle, endDistance);
    return new LineSegment(c1, c2);

  }

  /**
   * Add the line formed using the coordinates to the list of lines. If the
   * length of the line is < 1m and one end of the line has a degree of 1 then
   * don't add the line as it is probably an overshoot.
   * 
   * @param graph The graph containing all the lines in the network.
   * @param factory The factory to create the line.
   * @param lines The list to add the line to.
   * @param coordinates The coordinates used to create the line.
   */
  public static void addLine(
    final com.revolsys.jts.geom.GeometryFactory factory,
    final List<LineString> lines, final List<Coordinate> coordinates) {
    if (coordinates.size() > 1) {
      boolean add = true;
      final LineString line = factory.createLineString(DoubleCoordinatesListFactory.create(coordinates));
      if (line.getLength() < 1) {
        // Node<T> firstNode = findNode(coordinates.get(0));
        // if (firstNode != null && firstNode.getDegree() == 1) {
        // add = false;
        // } else {
        // Node<T> lastNode = findNode(coordinates.get(coordinates.size() - 1));
        // if (lastNode != null && lastNode.getDegree() == 1) {
        add = false;
        // }
        // }
      }
      if (add) {
        lines.add(line);
      }
    }
  }

  public static void applyPrecisionModel(final Geometry geometry) {
    final PrecisionModel precisionModel = geometry.getPrecisionModel();
    final Coordinate[] coordinates = geometry.getCoordinateArray();
    for (int i = 0; i < coordinates.length; i++) {
      final Coordinate coordinate = coordinates[i];
      precisionModel.makePrecise(coordinate);
    }

  }

  public static Envelope buffer(final Envelope envelope, final int i) {
    // TODO Auto-generated method stub
    return new Envelope(envelope.getMinX() - i, envelope.getMaxX() + i,
      envelope.getMinY() - i, envelope.getMaxY() + i);
  }

  public static Coordinate closestCoordinate(final LineSegment lineSegment,
    final LineString line) {
    Coordinate closestCoordinate = null;
    double closestDistance = Double.MAX_VALUE;
    final CoordinatesList sequence = line.getCoordinatesList();
    for (int i = 0; i < sequence.size(); i++) {
      final Coordinate coordinate = sequence.getCoordinate(i);
      if (!coordinate.equals(lineSegment.p0)
        && !coordinate.equals(lineSegment.p1)) {
        final double distance = lineSegment.distance(coordinate);
        if (distance < closestDistance) {
          closestCoordinate = coordinate;
          closestDistance = distance;
        }
      }
    }
    return closestCoordinate;
  }

  @SuppressWarnings("unchecked")
  public static void copyUserData(final Geometry oldGeometry,
    final Geometry newGeometry) {
    if (oldGeometry != null && newGeometry != null
      && oldGeometry != newGeometry) {
      Object userData = oldGeometry.getUserData();
      if (userData instanceof Map) {
        final Map<String, Object> oldValues = (Map<String, Object>)userData;
        final Map<String, Object> newValues = new TreeMap<String, Object>();
        for (final Entry<String, Object> entry : oldValues.entrySet()) {
          final String key = entry.getKey();
          final Object value = entry.getValue();
          if (value != null) {
            if (!(value instanceof Reference)) {
              final Object newValue = JavaBeanUtil.clone(value);
              newValues.put(key, newValue);
            }
          }
        }
        if (newValues.isEmpty()) {
          userData = null;
        } else {
          userData = newValues;
        }
      } else if (userData != null) {
        userData = JavaBeanUtil.clone(userData);
      }
      newGeometry.setUserData(userData);
    }
  }

  public static <G extends com.revolsys.gis.model.geometry.Geometry> G createGeometry(
    final Geometry jtsGeometry) {
    final com.revolsys.jts.geom.GeometryFactory jtsFactory = GeometryFactory.getFactory(jtsGeometry);
    final int srid = jtsFactory.getSrid();
    final int numAxis = jtsFactory.getNumAxis();
    final double scaleXY = jtsFactory.getScaleXY();
    final double scaleZ = jtsFactory.getScaleZ();
    final com.revolsys.gis.model.geometry.impl.GeometryFactoryImpl factory = com.revolsys.gis.model.geometry.impl.GeometryFactoryImpl.getFactory(
      srid, numAxis, scaleXY, scaleZ);
    if (jtsGeometry instanceof Point) {
      final Point point = (Point)jtsGeometry;
      return (G)factory.createPoint(CoordinatesUtil.getInstance(point));
    } else if (jtsGeometry instanceof LineString) {
      final LineString line = (LineString)jtsGeometry;
      return (G)factory.createLineString(CoordinatesListUtil.get(line));
    } else if (jtsGeometry instanceof Polygon) {
      final Polygon polygon = (Polygon)jtsGeometry;
      return (G)factory.createPolygon(CoordinatesListUtil.getAll(polygon));
    } else {
      throw new IllegalArgumentException("Not supported "
        + jtsGeometry.getClass());
    }

  }

  public static LinearRing createLinearRing(
    final com.revolsys.jts.geom.GeometryFactory factory,
    final List<Coordinate> coordinates) {
    final Coordinate[] coords = new Coordinate[coordinates.size()];
    coordinates.toArray(coords);
    return factory.createLinearRing(coords);

  }

  public static LineString createLineString(
    final com.revolsys.jts.geom.GeometryFactory factory,
    final Coordinate coordinate, final double angle,
    final double lengthBackward, final double lengthForward) {
    final Coordinate c1 = new Coordinate(coordinate.x - lengthBackward
      * Math.cos(angle), coordinate.y - lengthBackward * Math.sin(angle));
    final Coordinate c2 = new Coordinate(coordinate.x + lengthForward
      * Math.cos(angle), coordinate.y + lengthForward * Math.sin(angle));
    final LineString line = factory.createLineString(new Coordinate[] {
      c1, c2
    });
    return line;
  }

  public static LineString createLineString(
    final com.revolsys.jts.geom.GeometryFactory factory,
    final List<Coordinate> coordinates) {
    final Coordinate[] coords = new Coordinate[coordinates.size()];
    coordinates.toArray(coords);
    return factory.createLineString(coords);

  }

  public static MultiLineString createMultiLineString(
    final List<LineString> lines) {
    if (lines.isEmpty()) {
      return null;
    } else {
      final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(lines.get(0));
      final LineString[] lineArray = new LineString[lines.size()];
      lines.toArray(lineArray);
      return factory.createMultiLineString(lineArray);
    }
  }

  public static LineString createParallelLineString(final LineString line,
    final int orientation, final double distance) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(line);
    final CoordinatesList coordinates = line.getCoordinatesList();
    final List<Coordinate> newCoordinates = new ArrayList<Coordinate>();
    Coordinate coordinate = coordinates.getCoordinate(0);
    LineSegment lastLineSegment = null;
    final int coordinateCount = coordinates.size();
    for (int i = 0; i < coordinateCount; i++) {
      Coordinate nextCoordinate = null;
      LineSegment lineSegment = null;
      if (i < coordinateCount - 1) {
        nextCoordinate = coordinates.getCoordinate(i + 1);
        lineSegment = new LineSegment(coordinate, nextCoordinate);
        lineSegment = offset(lineSegment, distance, orientation);
        // if (lastLineSegment == null) {
        // lineSegment = addLength(lineSegment, 0, distance*2);
        // } else if (i == coordinateCount - 2) {
        // lineSegment = addLength(lineSegment, distance*2, 0);
        // } else {
        // lineSegment = addLength(lineSegment, distance*2, distance*2);
        // }
      }
      if (lineSegment == null) {
        newCoordinates.add(lastLineSegment.p1);
      } else if (lastLineSegment == null) {
        newCoordinates.add(lineSegment.p0);
      } else {
        final Coordinate intersection = lastLineSegment.intersection(lineSegment);
        if (intersection != null) {
          newCoordinates.add(intersection);
        } else {
          // newCoordinates.add(lastLineSegment.p1);
          newCoordinates.add(lineSegment.p0);
        }
      }

      coordinate = nextCoordinate;
      lastLineSegment = lineSegment;
    }
    final CoordinatesList newCoords = DoubleCoordinatesListFactory.create(newCoordinates);
    return factory.createLineString(newCoords);
  }

  public static Polygon createPolygon(
    final com.revolsys.jts.geom.GeometryFactory factory,
    final List<Coordinate> coordinates) {
    final LinearRing shell = createLinearRing(factory, coordinates);
    return factory.createPolygon(shell, null);
  }

  public static Polygon createPolygon(final MultiLineString multiLine) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(multiLine);
    final Coordinate[] coordinates = getMergeLine(multiLine).getCoordinateArray();
    final LinearRing linearRing = factory.createLinearRing(coordinates);
    final Polygon polygon = factory.createPolygon(linearRing, null);
    return polygon;

  }

  public static boolean equalsExact3D(final Geometry geometry1,
    final Geometry geometry2) {
    if (geometry1 == null) {
      return geometry2 == null;
    } else if (geometry2 == null) {
      return false;
    } else if ((geometry1 instanceof LineString)
      && (geometry2 instanceof LineString)) {
      final LineString line1 = (LineString)geometry1;
      final LineString line2 = (LineString)geometry2;
      return equalsExact3D(line1, line2);
    } else if ((geometry1 instanceof Point) && (geometry2 instanceof Point)) {
      final Point point1 = (Point)geometry1;
      final Point point2 = (Point)geometry2;
      return equalsExact3D(point1, point2);
    } else if ((geometry1 instanceof MultiPoint)
      && (geometry2 instanceof MultiPoint)) {
      final MultiPoint multiPoint1 = (MultiPoint)geometry1;
      final MultiPoint multiPoint2 = (MultiPoint)geometry2;
      return equalsExact3D(multiPoint1, multiPoint2);
    } else if ((geometry1 instanceof Polygon) && (geometry2 instanceof Polygon)) {
      final Polygon polygon1 = (Polygon)geometry1;
      final Polygon polygon2 = (Polygon)geometry2;
      return equalsExact3D(polygon1, polygon2);
    } else {
      return false;
    }
  }

  public static boolean equalsExact3D(final GeometryCollection collection1,
    final GeometryCollection collection2) {
    if (collection1.getNumGeometries() != collection2.getNumGeometries()) {
      return false;
    } else {
      for (int i = 0; i < collection1.getNumGeometries(); i++) {
        final Geometry geometry1 = collection1.getGeometry(i);
        final Geometry geometry2 = collection2.getGeometry(i);
        if (!equalsExact3D(geometry1, geometry2)) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean equalsExact3D(final LineString line1,
    final LineString line2) {
    if (line1.getNumPoints() != line2.getNumPoints()) {
      return false;
    }
    for (int i = 0; i < line1.getNumPoints(); i++) {
      line1.getCoordinateN(i);
      final Coordinate coordinate1 = line1.getCoordinateN(i);
      final Coordinate coordinate2 = line2.getCoordinateN(i);
      if (!coordinate1.equals3D(coordinate2)) {
        return false;
      }
    }
    return true;
  }

  public static boolean equalsExact3D(final Point point1, final Point point2) {
    final Coordinates coordinate1 = CoordinatesUtil.getInstance(point1);
    final Coordinates coordinate2 = CoordinatesUtil.getInstance(point2);
    if (coordinate1 == null) {
      return coordinate2 == null;
    } else if (coordinate2 == null) {
      return false;
    } else {
      return coordinate1.equals3d(coordinate2);
    }
  }

  public static boolean equalsExact3D(final Polygon polygon1,
    final Polygon polygon2) {
    if (polygon1.getNumInteriorRing() == polygon2.getNumInteriorRing()) {
      final LineString exterior1 = polygon1.getExteriorRing();
      final LineString exterior2 = polygon2.getExteriorRing();
      if (equalsExact3D(exterior1, exterior2)) {
        for (int i = 0; i < polygon1.getNumInteriorRing(); i++) {
          if (!equalsExact3D(polygon1.getInteriorRingN(i),
            polygon2.getInteriorRingN(i))) {
            return false;
          }
        }
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public static boolean equalsZ(final double z1, final double z2) {
    if (z1 == z2) {
      return true;
    } else if (Double.isNaN(z1)) {
      return (Double.isNaN(z2) || z2 == 0);
    } else if (z1 == 0 && Double.isNaN(z2)) {
      return true;
    } else {
      return false;
    }

  }

  private static CoordinatesList get2DCoordinates(
    final CoordinatesList coordinateSequence) {
    final int numCoords = coordinateSequence.size();
    final CoordinatesList coordinates = new DoubleCoordinatesList(numCoords, 2);
    for (int i = 0; i < numCoords; i++) {
      final double x = coordinateSequence.getX(i);
      final double y = coordinateSequence.getY(i);
      coordinates.setValue(i, 0, x);
      coordinates.setValue(i, 1, y);
    }
    return coordinates;
  }

  public static Geometry get2DGeometry(final Geometry geometry) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(geometry);
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return factory.createPoint(get2DCoordinates(point.getCoordinateSequence()));
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return factory.createLineString(get2DCoordinates(line.getCoordinatesList()));
    } else if (geometry instanceof Polygon) {

      final Polygon polygon = (Polygon)geometry;
      final LinearRing shell = (LinearRing)polygon.getExteriorRing();
      final LinearRing shell2d = get2DGeometry(shell);
      final LinearRing[] holes2d = new LinearRing[polygon.getNumInteriorRing()];
      for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
        final LinearRing hole = (LinearRing)polygon.getInteriorRingN(i);
        holes2d[i] = get2DGeometry(hole);
      }

      return factory.createPolygon(shell2d, holes2d);
    }
    return null;
  }

  public static LinearRing get2DGeometry(final LinearRing ring) {
    final CoordinatesList coordinates = ring.getCoordinatesList();
    final CoordinatesList coordinates2d = get2DCoordinates(coordinates);
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(ring);
    return factory.createLinearRing(coordinates2d);
  }

  public static Coordinates getCoordinate(final Geometry geometry,
    final int pointIndex) {
    final CoordinatesList points = CoordinatesListUtil.get(geometry);
    if (pointIndex < points.size()) {
      return new DoubleCoordinates(points.get(pointIndex));
    } else {
      return null;
    }
  }

  public static Coordinates getCoordinate(final GeometryCollection geometry,
    final int partIndex, final int pointIndex) {
    if (partIndex < geometry.getNumGeometries()) {
      final Geometry part = geometry.getGeometry(partIndex);
      return getCoordinate(part, pointIndex);
    }
    return null;
  }

  public static Coordinates getCoordinate(final Polygon polygon,
    final int ringIndex, final int pointIndex) {
    if (ringIndex == 0) {
      final LineString ring = polygon.getExteriorRing();
      return getCoordinate(ring, pointIndex);
    } else if (ringIndex - 1 < polygon.getNumInteriorRing()) {
      final LineString ring = polygon.getInteriorRingN(ringIndex - 1);
      return getCoordinate(ring, pointIndex);
    } else {
      return null;
    }
  }

  public static List<Coordinate> getCoordinateList(final LineString line) {
    return new ArrayList<Coordinate>(Arrays.asList(line.getCoordinateArray()));

  }

  public static List<Coordinates> getCoordinates(final Geometry geometry) {
    final List<Coordinates> allPoints = new ArrayList<Coordinates>();
    for (final CoordinatesList points : CoordinatesListUtil.getAll(geometry)) {
      for (final Coordinates point : new InPlaceIterator(points)) {
        allPoints.add(new DoubleCoordinates(point));
      }
    }
    return allPoints;
  }

  public static Set<Coordinate> getCoordinateSet(final LineString line) {
    final Set<Coordinate> coordinates = new LinkedHashSet<Coordinate>();
    final CoordinatesList sequence = line.getCoordinatesList();
    for (int i = 0; i < sequence.size(); i++) {
      final Coordinate coordinate = sequence.getCoordinate(i);
      coordinates.add(coordinate);
    }
    return coordinates;
  }

  public static List<Coordinates> getCoordinatesList(
    final CoordinatesList points) {
    final List<Coordinates> allPoints = new ArrayList<Coordinates>();
    for (final Coordinates point : new InPlaceIterator(points)) {
      allPoints.add(new DoubleCoordinates(point));
    }
    return allPoints;
  }

  /**
   * Get the coordinate where two lines cross, or null if they don't cross.
   * 
   * @param line1 The first line.
   * @param line2 The second line
   * @return The coordinate or null if they don't cross
   */
  public static Coordinate getCrossingIntersection(final LineString line1,
    final LineString line2) {
    final RobustLineIntersector intersector = new RobustLineIntersector();

    final CoordinatesList coordinates1 = line1.getCoordinatesList();
    final CoordinatesList coordinates2 = line2.getCoordinatesList();
    final int numCoordinates1 = coordinates1.size();
    final int numCoordinates2 = coordinates2.size();
    final Coordinate firstCoord1 = coordinates1.getCoordinate(0);
    final Coordinate firstCoord2 = coordinates2.getCoordinate(0);
    final Coordinate lastCoord1 = coordinates1.getCoordinate(numCoordinates1 - 1);
    final Coordinate lastCoord2 = coordinates2.getCoordinate(numCoordinates2 - 2);

    Coordinate previousCoord1 = firstCoord1;
    for (int i1 = 1; i1 < numCoordinates1; i1++) {
      final Coordinate currentCoord1 = coordinates1.getCoordinate(i1);
      Coordinate previousCoord2 = firstCoord2;

      for (int i2 = 1; i2 < numCoordinates2; i2++) {
        final Coordinate currentCoord2 = coordinates2.getCoordinate(i2);

        intersector.computeIntersection(previousCoord1, currentCoord1,
          previousCoord2, currentCoord2);
        final int numIntersections = intersector.getIntersectionNum();
        if (intersector.hasIntersection()) {
          if (intersector.isProper()) {
            final Coordinate intersection = intersector.getIntersection(0);
            return intersection;
          } else if (numIntersections == 1) {
            final Coordinate intersection = intersector.getIntersection(0);
            if (i1 == 1 || i2 == 1 || i1 == numCoordinates1 - 1
              || i2 == numCoordinates2 - 1) {
              if (!((intersection.equals2D(firstCoord1) || intersection.equals2D(lastCoord1)) && (intersection.equals2D(firstCoord2) || intersection.equals2D(lastCoord2)))) {
                return intersection;
              }
            } else {
              return intersection;
            }
          } else if (intersector.isInteriorIntersection()) {
            for (int i = 0; i < numIntersections; i++) {
              final Coordinate intersection = intersector.getIntersection(i);
              if (!Arrays.asList(currentCoord1, previousCoord1, currentCoord2,
                previousCoord2).contains(intersection)) {
                return intersection;
              }
            }
          }

        }

        previousCoord2 = currentCoord2;
      }
      previousCoord1 = currentCoord1;
    }
    return null;

  }

  public static double getElevation(final Coordinate coordinate,
    final Coordinate c0, final Coordinate c1) {
    final double fraction = coordinate.distance(c0) / c0.distance(c1);
    final double z = c0.z + (c1.z - c0.z) * (fraction);
    return z;
  }

  public static double getElevation(final LineSegment line,
    final Coordinate coordinate) {
    final Coordinate c0 = line.p0;
    final Coordinate c1 = line.p1;
    return getElevation(coordinate, c0, c1);
  }

  public static Envelope getEnvelope(
    final com.revolsys.gis.model.geometry.impl.BoundingBox boundingBox) {
    return new Envelope(boundingBox.getMinX(), boundingBox.getMaxX(),
      boundingBox.getMinY(), boundingBox.getMaxY());
  }

  public static Point getFromPoint(final Geometry geometry) {
    if (geometry instanceof Point) {
      return (Point)geometry;
    } else if (geometry instanceof LineString) {
      return getFromPoint((LineString)geometry);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final LineString ring = polygon.getExteriorRing();
      return getFromPoint(ring);
    } else {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (part != null && !part.isEmpty()) {
          return getFromPoint(geometry);
        }
      }
    }
    return null;
  }

  public static Point getFromPoint(final LineString line) {
    final Coordinates coordinates = LineStringUtil.getFromCoordinates(line);
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    return geometryFactory.createPoint(coordinates);
  }

  public static Geometry getGeometries(
    final com.revolsys.jts.geom.GeometryFactory factory,
    final List<Coordinate> coords, final Set<Coordinate> intersectCoords) {
    final List<LineString> lines = new ArrayList<LineString>();
    final Iterator<Coordinate> iterator = coords.iterator();
    Coordinate previousCoordinate = iterator.next();
    final List<Coordinate> currentCoordinates = new ArrayList<Coordinate>();
    currentCoordinates.add(previousCoordinate);
    while (iterator.hasNext()) {
      final Coordinate currentCoordinate = iterator.next();
      currentCoordinates.add(currentCoordinate);
      if (intersectCoords.contains(currentCoordinate)) {
        addLine(factory, lines, currentCoordinates);
        currentCoordinates.clear();
        currentCoordinates.add(currentCoordinate);
      }
      previousCoordinate = currentCoordinate;
    }

    addLine(factory, lines, currentCoordinates);
    if (lines.size() == 1) {
      return lines.get(0);
    } else {
      return factory.createMultiLineString(lines.toArray(new LineString[0]));
    }
  }

  public static List<Geometry> getGeometries(final Geometry geometry) {
    final List<Geometry> parts = new ArrayList<Geometry>();
    for (int i = 0; i < geometry.getNumGeometries(); i++) {
      final Geometry part = geometry.getGeometry(i);
      parts.add(part);
    }
    return parts;
  }

  public static DataObject getGeometryFeature(final Geometry geometry) {
    return (DataObject)getGeometryProperty(geometry, FEATURE_PROPERTY);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getGeometryProperties(
    final Geometry geometry) {
    final Object userData = geometry.getUserData();
    if (userData instanceof Map) {
      final Map<String, Object> map = (Map<String, Object>)userData;
      return map;
    }
    return new TreeMap<String, Object>();
  }

  @SuppressWarnings("unchecked")
  public static <T extends Object> T getGeometryProperty(
    final Geometry geometry, final String name) {
    final Map<String, Object> map = getGeometryProperties(geometry);
    return (T)map.get(name);
  }

  public static List<LineString> getLines(final MultiLineString multiLine) {
    final List<LineString> lines = new ArrayList<LineString>();
    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
      lines.add((LineString)multiLine.getGeometry(i));
    }
    return lines;
  }

  public static List<LineSegment> getLineSegments(
    final CoordinatesList coords) {
    final List<LineSegment> segments = new ArrayList<LineSegment>();
    Coordinate previousCoordinate = coords.getCoordinate(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinate coordinate = coords.getCoordinate(i);
      final LineSegment segment = new LineSegment(previousCoordinate,
        coordinate);
      if (segment.getLength() > 0) {
        segments.add(segment);
      }
      previousCoordinate = coordinate;
    }
    return segments;
  }

  public static List<LineSegment> getLineSegments(final LineString line) {
    final CoordinatesList coords = line.getCoordinatesList();
    return getLineSegments(coords);
  }

  public static LineString getMatchingLines(final LineString line1,
    final LineString line2, final double maxDistance) {
    final List<Coordinate> newCoords = new ArrayList<Coordinate>();
    final CoordinatesList coords1 = line1.getCoordinatesList();
    final CoordinatesList coords2 = line1.getCoordinatesList();
    Coordinate previousCoordinate = coords1.getCoordinate(0);
    boolean finish = false;
    for (int i = 1; i < coords1.size() && !finish; i++) {
      final Coordinate coordinate = coords1.getCoordinate(i);
      Coordinate previousCoordinate2 = coords2.getCoordinate(0);
      for (int j = 1; j < coords1.size() && !finish; j++) {
        final Coordinate coordinate2 = coords2.getCoordinate(i);
        final double distance = CGAlgorithms.distanceLineLine(
          previousCoordinate, coordinate, previousCoordinate2, coordinate2);
        if (distance > maxDistance) {
          finish = true;
        }
        previousCoordinate2 = coordinate2;
      }
      previousCoordinate = coordinate;
    }
    if (newCoords.size() > 1) {
      return createLineString(GeometryFactory.getFactory(line1), newCoords);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static Collection<LineString> getMergedLines(
    final MultiLineString multiLineString) {
    final LineMerger merger = new LineMerger();
    merger.add(multiLineString);
    final Collection<LineString> lineStrings = merger.getMergedLineStrings();
    return lineStrings;
  }

  public static LineString getMergeLine(final MultiLineString multiLineString) {
    final Collection<LineString> lineStrings = getMergedLines(multiLineString);
    final int numLines = lineStrings.size();
    if (numLines == 1) {
      return lineStrings.iterator().next();
    } else {
      return null;
    }
  }

  public static double getMiddleAngle(final double lastAngle,
    final double angle, final int orientation) {
    if (orientation == Angle.COUNTERCLOCKWISE) {
      if (Double.isNaN(lastAngle)) {
        return angle + Angle.PI_OVER_2;
      } else if (Double.isNaN(angle)) {
        return lastAngle + Angle.PI_OVER_2;
      } else {
        final int turn = Angle.getTurn(lastAngle, angle);
        final double angleDiff = angle - lastAngle;
        if (turn == Angle.CLOCKWISE) {
          return lastAngle
            - (Angle.PI_TIMES_2 - (Math.PI - Math.abs(angleDiff)) / 2);
        } else {
          return angle + (Math.PI - Math.abs(angleDiff)) / 2;
        }

      }
    } else {
      if (Double.isNaN(lastAngle)) {
        return angle - Angle.PI_OVER_2;
      } else if (Double.isNaN(angle)) {
        return lastAngle - Angle.PI_OVER_2;
      } else {
        final int turn = Angle.getTurn(lastAngle, angle);
        final double angleDiff = angle - lastAngle;
        if (turn == Angle.CLOCKWISE) {
          return angle - (Math.PI - Math.abs(angleDiff)) / 2;
        } else {
          return lastAngle
            + (Angle.PI_TIMES_2 - (Math.PI - Math.abs(angleDiff)) / 2);
        }
      }
    }

  }

  public static Geometry getMitredBuffer(final Geometry geometry,
    final double distance) {
    if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return getMitredBuffer(polygon, distance);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return getMitredBuffer(line, distance);
    } else {
      return geometry.buffer(distance);
    }
  }

  public static Polygon getMitredBuffer(final LineSegment segment,
    final double distance) {

    final LineSegment extendedSegment = addLength(segment, distance, distance);
    final LineSegment clockwiseSegment = offset(extendedSegment, distance,
      Angle.CLOCKWISE);
    final LineSegment counterClockwiseSegment = offset(extendedSegment,
      distance, Angle.COUNTERCLOCKWISE);

    final Coordinate[] coords = new Coordinate[] {
      clockwiseSegment.p0, clockwiseSegment.p1, counterClockwiseSegment.p1,
      counterClockwiseSegment.p0, clockwiseSegment.p0
    };
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory();
    final LinearRing exteriorRing = factory.createLinearRing(coords);
    return factory.createPolygon(exteriorRing, null);
  }

  public static Polygon getMitredBuffer(final LineString lineString,
    final double distance) {
    final LineStringMitredBuffer visitor = new LineStringMitredBuffer(distance);
    visitLineSegments(lineString.getCoordinatesList(), visitor);
    return visitor.getBuffer();
  }

  public static Polygon getMitredBuffer(final Polygon polygon,
    final double distance) {
    Geometry buffer = polygon;
    final LineString exteriorRing = polygon.getExteriorRing();
    final Geometry exteriorBuffer = getMitredBuffer(exteriorRing, distance);
    buffer = buffer.union(exteriorBuffer);
    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
      final LineString ring = polygon.getInteriorRingN(i);
      final Geometry bufferedRing = getMitredBuffer(ring, distance);
      buffer = buffer.union(bufferedRing);
    }
    return (Polygon)buffer;
  }

  public static double[] getMRange(final CoordinatesList coordinates) {
    return getOrdinateRange(coordinates, 3);
  }

  public static double[] getMRange(final Geometry geometry) {
    return getOrdinateRange(geometry, 3);
  }

  public static double[] getOrdinateRange(final CoordinatesList coordinates,
    final int ordinateIndex) {
    double min = Double.MAX_VALUE;
    double max = -Double.MAX_VALUE;
    if (ordinateIndex < coordinates.getDimension()) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double value = coordinates.getOrdinate(i, ordinateIndex);
        if (!Double.isNaN(value)) {
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
      }
    }
    return new double[] {
      min, max
    };
  }

  public static void getOrdinateRange(final double[] range,
    final CoordinatesList coordinates, final int ordinateIndex) {
    double min = range[0];
    double max = range[1];
    if (ordinateIndex < coordinates.getDimension()) {
      for (int i = 0; i < coordinates.size(); i++) {
        final double value = coordinates.getValue(i, ordinateIndex);
        if (!Double.isNaN(value)) {
          min = Math.min(min, value);
          max = Math.max(max, value);
        }
      }
    }
    range[0] = min;
    range[1] = max;
  }

  public static double[] getOrdinateRange(final Geometry geometry,
    final int ordinateIndex) {
    final double[] range = {
      Double.MAX_VALUE, -Double.MAX_VALUE
    };
    for (final CoordinatesList points : CoordinatesListUtil.getAll(geometry)) {
      getOrdinateRange(range, points, ordinateIndex);
    }
    return range;
  }

  public static Coordinates getPointOnLine(final Geometry geometry,
    final Point point, final double maxDistance) {
    final Coordinates coordinates = CoordinatesUtil.getInstance(point);
    Coordinates pointOnLine = null;
    final double closestDistance = Double.MAX_VALUE;
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
    for (final LineString line : LineStringUtil.getAll(geometry)) {
      final Coordinates closestPoint = LineStringUtil.getClosestCoordinateOnLineString(
        geometryFactory, line, coordinates, maxDistance);
      final double distance = coordinates.distance(closestPoint);
      if (distance < closestDistance) {
        pointOnLine = closestPoint;
      }
    }
    return pointOnLine;
  }

  public static Point getToPoint(final Geometry geometry) {
    if (geometry instanceof Point) {
      return (Point)geometry;
    } else if (geometry instanceof LineString) {
      return LineStringUtil.getToPoint((LineString)geometry);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      final LineString ring = polygon.getExteriorRing();
      return LineStringUtil.getToPoint(ring);
    } else {
      for (int i = geometry.getNumGeometries() - 1; i < -1; i--) {
        final Geometry part = geometry.getGeometry(i);
        if (part != null && !part.isEmpty()) {
          return getToPoint(geometry);
        }
      }
    }
    return null;
  }

  public static double[] getZRange(final CoordinatesList coordinates) {
    return getOrdinateRange(coordinates, 2);
  }

  public static double[] getZRange(final Geometry geometry) {
    return getOrdinateRange(geometry, 2);
  }

  /**
   * Insert the coordinate at the specified index into the line, returning the
   * new line.
   * 
   * @param line The line.
   * @param index The index to insert the coordinate.
   * @param coordinate The coordinate.
   */
  public static LineString insert(final LineString line, final int index,
    final Coordinate coordinate) {
    final CoordinatesList coords = line.getCoordinatesList();
    final CoordinatesList newCoords = new DoubleCoordinatesList(
      coords.size() + 1, coords.getDimension());
    int j = 0;
    for (int i = 0; i < newCoords.size(); i++) {
      if (i == index) {
        newCoords.setX(i, coordinate.x);
        newCoords.setY(i, coordinate.y);
        if (newCoords.getDimension() > 2) {
          newCoords.setZ(i, coordinate.z);
        }
      } else {
        for (int o = 0; o < newCoords.getDimension(); o++) {
          newCoords.setValue(i, o, coords.getValue(j, o));
        }
        j++;
      }
    }
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(line);
    final LineString newLine = factory.createLineString(newCoords);
    return newLine;
  }

  public static Geometry intersection2DZ(final LineString line,
    final Geometry geometry) {
    final Geometry intersection = line.intersection(geometry);
    if (intersection instanceof LineString) {
      final LineString lineDiff = (LineString)intersection;
      addElevation(line, lineDiff);

    } else {
      if (intersection instanceof MultiLineString) {
        for (int i = 0; i < intersection.getNumGeometries(); i++) {
          final LineString lineDiff = (LineString)intersection.getGeometry(i);
          addElevation(line, lineDiff);
        }
      }
    }
    JtsGeometryUtil.copyUserData(line, intersection);
    return intersection;
  }

  /**
   * Change to a floating precision model to calculate the intersection. This
   * reduces the chance of lines being returned instead of points where there is
   * a sharp angle
   * 
   * @param line1
   * @param line2
   * @return
   */
  public static Geometry intersection2DZFloating(final LineString line1,
    final LineString line2) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory();
    final CoordinatesList coordinates1 = line1.getCoordinatesList();
    final LineString line1Floating = factory.createLineString(coordinates1);
    final CoordinatesList coordinates2 = line2.getCoordinatesList();
    final LineString line2Floating = factory.createLineString(coordinates2);
    return JtsGeometryUtil.intersection2DZ(line1Floating, line2Floating);
  }

  public static boolean intersects(final Geometry geometry1,
    final Geometry geometry2) {
    final int srid2 = geometry2.getSrid();
    final Geometry projectedGeometry1 = GeometryProjectionUtil.perform(
      geometry1, srid2);
    return projectedGeometry1.intersects(geometry2);
  }

  public static boolean intersectsLinearly(final LineString line1,
    final LineString line2) {
    final IntersectionMatrix matrix = line1.relate(line2);
    return matrix.get(0, 0) == Dimension.L;
  }

  public static boolean isAlmostParallel(final LineString line,
    final LineString matchLine, final double maxDistance) {
    final CoordinatesList coords = line.getCoordinatesList();
    final CoordinatesList matchCoords = line.getCoordinatesList();
    Coordinate previousCoordinate = coords.getCoordinate(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinate coordinate = coords.getCoordinate(i);
      Coordinate previousMatchCoordinate = matchCoords.getCoordinate(0);
      for (int j = 1; j < coords.size(); j++) {
        final Coordinate matchCoordinate = matchCoords.getCoordinate(i);
        final double distance = CGAlgorithms.distanceLineLine(
          previousCoordinate, coordinate, previousMatchCoordinate,
          matchCoordinate);
        if (distance <= maxDistance) {
          final double angle1 = Angle.normalizePositive(Angle.angle(
            previousCoordinate, coordinate));
          final double angle2 = Angle.normalizePositive(Angle.angle(
            previousMatchCoordinate, matchCoordinate));
          final double angleDiff = Math.abs(angle1 - angle2);
          if (angleDiff <= Math.PI / 6) {
            return true;
          }
        }
        previousMatchCoordinate = matchCoordinate;
      }
      previousCoordinate = coordinate;
    }
    return false;
  }

  public static boolean isBothWithinDistance(final LineString line1,
    final LineString line2, final double maxDistance) {
    if (isWithinDistance(line1, line2, maxDistance)) {
      return isWithinDistance(line2, line1, maxDistance);
    } else {
      return false;
    }
  }

  /**
   * Computes whether a ring defined by an array of {@link Coordinate} is
   * oriented counter-clockwise.
   * <ul>
   * <li>The list of points is assumed to have the first and last points equal.
   * <li>This will handle coordinate lists which contain repeated points.
   * </ul>
   * This algorithm is <b>only</b> guaranteed to work with valid rings. If the
   * ring is invalid (e.g. self-crosses or touches), the computed result
   * <b>may</b> not be correct.
   * 
   * @param ring an array of coordinates forming a ring
   * @return <code>true</code> if the ring is oriented counter-clockwise.
   */
  public static boolean isCCW(final CoordinatesList ring) {
    // # of points without closing endpoint
    final int nPts = ring.size() - 1;

    // find highest point
    Coordinate hiPt = ring.getCoordinate(0);
    int hiIndex = 0;
    for (int i = 1; i <= nPts; i++) {
      final Coordinate p = ring.getCoordinate(i);
      if (p.y > hiPt.y) {
        hiPt = p;
        hiIndex = i;
      }
    }

    // find distinct point before highest point
    int iPrev = hiIndex;
    do {
      iPrev = iPrev - 1;
      if (iPrev < 0) {
        iPrev = nPts;
      }
    } while (ring.getCoordinate(iPrev).equals2D(hiPt) && iPrev != hiIndex);

    // find distinct point after highest point
    int iNext = hiIndex;
    do {
      iNext = (iNext + 1) % nPts;
    } while (ring.getCoordinate(iNext).equals2D(hiPt) && iNext != hiIndex);

    final Coordinate prev = ring.getCoordinate(iPrev);
    final Coordinate next = ring.getCoordinate(iNext);

    /**
     * This check catches cases where the ring contains an A-B-A configuration
     * of points. This can happen if the ring does not contain 3 distinct points
     * (including the case where the input array has fewer than 4 elements), or
     * it contains coincident line segments.
     */
    if (prev.equals2D(hiPt) || next.equals2D(hiPt) || prev.equals2D(next)) {
      return false;
    }

    final int disc = CGAlgorithms.computeOrientation(prev, hiPt, next);

    /**
     * If disc is exactly 0, lines are collinear. There are two possible cases:
     * (1) the lines lie along the x axis in opposite directions (2) the lines
     * lie on top of one another (1) is handled by checking if next is left of
     * prev ==> CCW (2) will never happen if the ring is valid, so don't check
     * for it (Might want to assert this)
     */
    boolean isCCW = false;
    if (disc == 0) {
      // poly is CCW if prev x is right of next x
      isCCW = (prev.x > next.x);
    } else {
      // if area is positive, points are ordered CCW
      isCCW = (disc > 0);
    }
    return isCCW;
  }

  public static boolean isFromPoint(final Geometry geometry, final Point point) {
    final Point fromPoint = JtsGeometryUtil.getFromPoint(geometry);
    return EqualsRegistry.equal(point, fromPoint);
  }

  public static boolean isLessThanDistance(final Coordinate coordinate,
    final LineString line, final double maxDistance) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(line);
    final Point point = factory.createPoint(coordinate);
    final double distance = line.distance(point);
    return distance < maxDistance;
  }

  public static boolean isLessThanDistance(final LineString line1,
    final LineString line2, final double maxDistance) {
    final CoordinatesList coordinates1 = line1.getCoordinatesList();
    for (int i = 0; i < coordinates1.size(); i++) {
      final Coordinate coordinate = coordinates1.getCoordinate(i);
      if (!isLessThanDistance(coordinate, line2, maxDistance)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isToPoint(final Geometry geometry, final Point point) {
    final Point toPoint = getToPoint(geometry);
    return EqualsRegistry.equal(point, toPoint);
  }

  public static boolean isWithinDistance(final Coordinate coordinate,
    final LineString line, final double maxDistance) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(line);
    final Point point = factory.createPoint(coordinate);
    final double distance = line.distance(point);
    return distance <= maxDistance;
  }

  public static boolean isWithinDistance(final LineString line1,
    final LineString line2, final double maxDistance) {
    final CoordinatesList coordinates1 = line1.getCoordinatesList();
    for (int i = 0; i < coordinates1.size(); i++) {
      final Coordinate coordinate = coordinates1.getCoordinate(i);
      if (!isWithinDistance(coordinate, line2, maxDistance)) {
        return false;
      }
    }
    return true;
  }

  public static Coordinate offset(final Coordinate coordinate,
    final double angle, final double distance) {
    final double newX = coordinate.x + distance * Math.cos(angle);
    final double newY = coordinate.y + distance * Math.sin(angle);
    final Coordinate newCoordinate = new Coordinate(newX, newY);
    return newCoordinate;

  }

  public static LineSegment offset(final LineSegment line,
    final double distance, final int orientation) {
    double angle = line.angle();
    if (orientation == Angle.CLOCKWISE) {
      angle -= Angle.PI_OVER_2;
    } else {
      angle += Angle.PI_OVER_2;
    }
    final Coordinate c1 = offset(line.p0, angle, distance);
    final Coordinate c2 = offset(line.p1, angle, distance);
    return new LineSegment(c1, c2);
  }

  public static Point offset(final Point point, final double deltaX,
    final double deltaY) {
    final double x = point.getX();
    final double y = point.getY();

    final double newX = x + deltaX;
    final double newY = y + deltaY;

    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(point);
    final Point newPoint = factory.createPoint(newX, newY);
    return newPoint;
  }

  public static Coordinate processIntersection(
    final Set<Coordinate> intersectCoords, final Coordinate intersectCoord,
    final List<Coordinate> coords1, final ListIterator<Coordinate> iterator1,
    final int index1, final Coordinate previousCoord1,
    final Coordinate currentCoord1, final List<Coordinate> coords2,
    final ListIterator<Coordinate> iterator2, final int index2,
    final Coordinate previousCoord2, final Coordinate currentCoord2,
    final PrecisionModel precisionModel) {
    boolean intersectionFound = false;
    int matchIndex1 = -1;
    int matchIndex2 = -1;
    if (!Double.isNaN(previousCoord1.z)) {
      final LineSegment3D line = new LineSegment3D(previousCoord1,
        currentCoord1);
      line.addElevation(intersectCoord, ELEVATION_PRECISION_MODEL);
    } else if (!Double.isNaN(previousCoord2.z)) {
      final LineSegment3D line = new LineSegment3D(previousCoord2,
        currentCoord2);
      line.addElevation(intersectCoord, ELEVATION_PRECISION_MODEL);
    }

    precisionModel.makePrecise(intersectCoord);

    boolean line1ContainsCoord = false;
    boolean line2ContainsCoord = false;

    if (previousCoord1.equals2D(intersectCoord)) {
      line1ContainsCoord = true;
      matchIndex1 = index1;
    }
    if (currentCoord1.equals2D(intersectCoord)) {
      line1ContainsCoord = true;
      matchIndex1 = index1 + 1;
    }
    if (previousCoord2.equals2D(intersectCoord)) {
      line2ContainsCoord = true;
      matchIndex2 = index2;
    }
    if (currentCoord2.equals2D(intersectCoord)) {
      line2ContainsCoord = true;
      matchIndex2 = index2 + 1;
    }

    if (!line1ContainsCoord) {
      iterator1.previous();
      iterator1.add(intersectCoord);
      intersectionFound = true;
    }
    if (!line2ContainsCoord) {
      iterator2.previous();
      iterator2.add(intersectCoord);
      intersectionFound = true;
    }
    if (!((matchIndex1 == 0 || matchIndex1 == coords1.size() - 1) && (matchIndex2 == 0 || matchIndex2 == coords2.size() - 1))) {
      intersectCoords.add(intersectCoord);
    }
    if (intersectionFound) {
      return intersectCoord;
    } else {
      return null;
    }
  }

  public static Polygon reversePolygon(final Polygon polygon) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(polygon);
    final LineString exteriorRing = polygon.getExteriorRing();
    final CoordinatesList oldCoordinates = CoordinatesListUtil.get(exteriorRing);
    final CoordinatesList newCoordinates = oldCoordinates.reverse();
    final LinearRing shell = factory.createLinearRing(newCoordinates);
    return factory.createPolygon(shell, null);
  }

  public static Point setElevation(final Point newLocation,
    final Point originalLocation) {
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(originalLocation);
    final Point convertedNewPoint = geometryFactory.project(newLocation);
    if (geometryFactory.hasZ()) {
      final Coordinates coordinates = CoordinatesUtil.getInstance(originalLocation);
      final double z = coordinates.getZ();
      if (Double.isNaN(z)) {
        return convertedNewPoint;
      } else {
        final Coordinates newCoordinates = geometryFactory.createCoordinates(CoordinatesUtil.getInstance(newLocation));
        newCoordinates.setZ(z);
        return geometryFactory.createPoint(newCoordinates);
      }
    } else {
      return convertedNewPoint;
    }
  }

  public static void setGeometryFeature(final Geometry geometry,
    final DataObject feature) {
    setGeometryProperty(geometry, FEATURE_PROPERTY, feature);
  }

  @SuppressWarnings("unchecked")
  public static void setGeometryProperty(final Geometry geometry,
    final CharSequence name, final Object value) {
    Object userData = geometry.getUserData();
    if (!(userData instanceof Map)) {
      userData = new TreeMap<Object, Object>();
      geometry.setUserData(userData);
    }
    final Map<Object, Object> map = (Map<Object, Object>)userData;
    map.put(name.toString(), value);

  }

  public static List<Geometry> splitWhereCross(final LineString line1,
    final LineString line2) {
    final RobustLineIntersector intersector = new RobustLineIntersector();
    final List<Geometry> geometries = new ArrayList<Geometry>();

    final List<Coordinate> coords1 = new LinkedList<Coordinate>(
      Arrays.asList(line1.getCoordinateArray()));
    final List<Coordinate> coords2 = new LinkedList<Coordinate>(
      Arrays.asList(line2.getCoordinateArray()));
    final Set<Coordinate> intersectCoords = new LinkedHashSet<Coordinate>();
    final ListIterator<Coordinate> iterator1 = coords1.listIterator();
    Coordinate previousCoord1 = iterator1.next();
    boolean intersectionFound = false;
    Coordinate currentCoord1 = null;
    int index1 = 0;
    while (iterator1.hasNext()) {
      if (!intersectionFound) {
        currentCoord1 = iterator1.next();
      }
      intersectionFound = false;
      final ListIterator<Coordinate> iterator2 = coords2.listIterator();
      Coordinate previousCoord2 = iterator2.next();
      int index2 = 0;
      while (iterator2.hasNext() && !intersectionFound) {

        final Coordinate currentCoord2 = iterator2.next();
        intersector.computeIntersection(previousCoord1, currentCoord1,
          previousCoord2, currentCoord2);
        final PrecisionModel precisionModel = line1.getPrecisionModel();
        for (int i = 0; i < intersector.getIntersectionNum()
          && !intersectionFound; i++) {
          final Coordinate intersectCoord = intersector.getIntersection(i);
          final Coordinate newCoord = processIntersection(intersectCoords,
            intersectCoord, coords1, iterator1, index1, previousCoord1,
            currentCoord1, coords2, iterator2, index2, previousCoord2,
            currentCoord2, precisionModel);
          if (newCoord != null) {
            intersectionFound = true;
            currentCoord1 = newCoord;
          }
        }
        if (!intersectionFound) {
          previousCoord2 = currentCoord2;
          index2++;
        }
      }
      if (!intersectionFound) {
        previousCoord1 = currentCoord1;
        index1++;
      }
    }

    if (intersectCoords.isEmpty()) {
      geometries.add(line1);
      geometries.add(line2);
    } else {
      geometries.add(getGeometries(GeometryFactory.getFactory(line1), coords1,
        intersectCoords));
      geometries.add(getGeometries(GeometryFactory.getFactory(line2), coords2,
        intersectCoords));
    }

    return geometries;

  }

  public static boolean startAndEndEqual(final LineString geometry1,
    final LineString geometry2) {
    final Coordinate g1c0 = geometry1.getCoordinateN(0);
    final Coordinate g1cN = geometry1.getCoordinateN(geometry1.getNumPoints() - 1);
    final Coordinate g2c0 = geometry2.getCoordinateN(0);
    final Coordinate g2cN = geometry2.getCoordinateN(geometry2.getNumPoints() - 1);
    if (g1c0.equals2D(g2c0)) {
      return g1cN.equals2D(g2cN);
    } else if (g1c0.equals2D(g2cN)) {
      return g1cN.equals2D(g2c0);
    } else {
      return false;
    }
  }

  /**
   * @param line1 The line to match
   * @param line2 The line to compare the start of with the other line
   * @return
   */
  public static boolean touchesAtEnd(final LineString line1,
    final LineString line2) {
    final Coordinate l1c0 = line1.getCoordinateN(0);
    final Coordinate l1cN = line1.getCoordinateN(line1.getNumPoints() - 1);
    final Coordinate l2cN = line2.getCoordinateN(line2.getNumPoints() - 1);
    if (l2cN.equals2D(l1c0)) {
      return true;
    } else {
      return l2cN.equals2D(l1cN);
    }
  }

  /**
   * @param line1 The line to match
   * @param line2 The line to compare the start of with the other line
   * @return
   */
  public static boolean touchesAtStart(final LineString line1,
    final LineString line2) {
    final Coordinate l1c0 = line1.getCoordinateN(0);
    final Coordinate l1cN = line1.getCoordinateN(line1.getNumPoints() - 1);
    final Coordinate l2c0 = line2.getCoordinateN(0);
    if (l2c0.equals2D(l1c0)) {
      return true;
    } else {
      return l2c0.equals2D(l1cN);
    }
  }

  public static Geometry unionAll(final List<Geometry> geometries) {
    final com.revolsys.jts.geom.GeometryFactory factory = GeometryFactory.getFactory(geometries.get(0));
    final Geometry geometry = factory.createGeometry(geometries);
    if (geometry.getNumGeometries() == 1) {
      return geometry;
    } else {
      return geometry.union();
    }
  }

  public static void visitLineSegments(final CoordinatesList coords,
    final LineSegmentVisitor visitor) {
    Coordinate previousCoordinate = coords.getCoordinate(0);
    for (int i = 1; i < coords.size(); i++) {
      final Coordinate coordinate = coords.getCoordinate(i);
      final LineSegment segment = new LineSegment(previousCoordinate,
        coordinate);
      if (segment.getLength() > 0) {
        if (!visitor.visit(segment)) {
          return;
        }
      }
      previousCoordinate = coordinate;
    }
  }

  private JtsGeometryUtil() {
  }

}
