package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesListFactory;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Dimension;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.IntersectionMatrix;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

public final class JtsGeometryUtil {
  private static final PrecisionModel ELEVATION_PRECISION_MODEL = new PrecisionModel(
    1);

  public static final String FEATURE_PROPERTY = "feature";

  /**
   * Add a evelation (z) value for a coordinate that is on a line segment.
   * 
   * @param coordinate The Coordinate.
   * @param line The line segment the coordinate is on.
   */
  public static void addElevation(
    final Coordinate coordinate,
    final LineSegment line) {
    final double z = getElevation(line, coordinate);
    coordinate.z = z;
  }

  public static void addElevation(
    final Coordinate coordinate,
    final LineString line) {
    final CoordinateSequence coordinates = line.getCoordinateSequence();
    Coordinate previousCoordinate = coordinates.getCoordinate(0);
    for (int i = 1; i < coordinates.size(); i++) {
      final Coordinate currentCoordinate = coordinates.getCoordinate(i);
      final LineSegment3D segment = new LineSegment3D(previousCoordinate,
        currentCoordinate);
      if (segment.distance(coordinate) < 1) {
        final PrecisionModel precisionModel = line.getFactory()
          .getPrecisionModel();
        addElevation(precisionModel, coordinate, segment);
        return;
      }
      previousCoordinate = currentCoordinate;
    }

  }

  private static void addElevation(
    final LineString original,
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

  public static void addElevation(
    final PrecisionModel precisionModel,
    final Coordinate coordinate,
    final LineSegment3D line) {
    addElevation(coordinate, line);
    coordinate.z = precisionModel.makePrecise(coordinate.z);

  }

  public static LineSegment addLength(
    final LineSegment line,
    final double startDistance,
    final double endDistance) {
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
    final GeometryFactory factory,
    final List<LineString> lines,
    final List<Coordinate> coordinates) {
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

  public static void applyPrecisionModel(
    final Geometry geometry) {
    final PrecisionModel precisionModel = geometry.getPrecisionModel();
    final Coordinate[] coordinates = geometry.getCoordinates();
    for (int i = 0; i < coordinates.length; i++) {
      final Coordinate coordinate = coordinates[i];
      precisionModel.makePrecise(coordinate);
    }

  }

  public static Envelope buffer(
    final Envelope envelope,
    final int i) {
    // TODO Auto-generated method stub
    return new Envelope(envelope.getMinX() - i, envelope.getMaxX() + i,
      envelope.getMinY() - i, envelope.getMaxY() + i);
  }

  public static Coordinate closestCoordinate(
    final LineSegment lineSegment,
    final LineString line) {
    Coordinate closestCoordinate = null;
    double closestDistance = Double.MAX_VALUE;
    final CoordinateSequence sequence = line.getCoordinateSequence();
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

  public static void copyUserData(
    final Geometry oldGeometry,
    final Geometry newGeometry) {
    final Map<String, Object> userData = JtsGeometryUtil.getGeometryProperties(oldGeometry);
    if (userData != null) {
      newGeometry.setUserData(new TreeMap<String, Object>(userData));
    }
  }

  public static LinearRing createLinearRing(
    final GeometryFactory factory,
    final List<Coordinate> coordinates) {
    final Coordinate[] coords = new Coordinate[coordinates.size()];
    coordinates.toArray(coords);
    return factory.createLinearRing(coords);

  }

  public static LineString createLineString(
    final GeometryFactory factory,
    final Coordinate coordinate,
    final double angle,
    final double lengthBackward,
    final double lengthForward) {
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
    final GeometryFactory factory,
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
      final GeometryFactory factory = GeometryFactory.getFactory(lines.get(0));
      final LineString[] lineArray = new LineString[lines.size()];
      lines.toArray(lineArray);
      return factory.createMultiLineString(lineArray);
    }
  }

  public static LineString createParallelLineString(
    final LineString line,
    final int orientation,
    final double distance) {
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final CoordinateSequence coordinates = line.getCoordinateSequence();
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
    final CoordinateSequence newCoords = DoubleCoordinatesListFactory.create(newCoordinates);
    return factory.createLineString(newCoords);
  }

  public static Polygon createPolygon(
    final GeometryFactory factory,
    final List<Coordinate> coordinates) {
    final LinearRing shell = createLinearRing(factory, coordinates);
    return factory.createPolygon(shell, null);
  }

  public static Polygon createPolygon(
    final MultiLineString multiLine) {
    final GeometryFactory factory = GeometryFactory.getFactory(multiLine);
    final Coordinate[] coordinates = getMergeLine(multiLine).getCoordinates();
    final LinearRing linearRing = factory.createLinearRing(coordinates);
    final Polygon polygon = factory.createPolygon(linearRing, null);
    return polygon;

  }

  public static Geometry difference2DZ(
    final LineString line,
    final Geometry geometry) {
    final Geometry difference = line.difference(geometry);
    if (difference instanceof LineString) {
      final LineString lineDiff = (LineString)difference;
      final Coordinate c0 = lineDiff.getCoordinateN(0);
      if (Double.isNaN(c0.z)) {
        addElevation(c0, line);
      }
      final Coordinate cN = lineDiff.getCoordinateN(lineDiff.getNumPoints() - 1);
      if (Double.isNaN(cN.z)) {
        addElevation(cN, line);
      }

    }
    difference.setUserData(line.getUserData());
    return difference;
  }

  public static double distance(
    final Coordinate coordinate,
    final Geometry geometry) {
    final GeometryFactory factory = GeometryFactory.getFactory(geometry);
    final Point point = factory.createPoint(coordinate);
    return point.distance(geometry);
  }

  public static double distance(
    final Coordinate coordinate,
    final LineString line) {
    return distance(coordinate, line, 0.0);
  }

  public static double distance(
    final Coordinate coordinate,
    final LineString line,
    final double tolerance) {
    final CoordinateSequence coordinates = line.getCoordinateSequence();
    double minDistance = Double.MAX_VALUE;
    final double x = coordinate.x;
    final double y = coordinate.y;
    double x1 = coordinates.getOrdinate(0, 0);
    double y1 = coordinates.getOrdinate(0, 1);
    for (int i = 1; i < coordinates.size(); i++) {
      final double x2 = coordinates.getOrdinate(i, 0);
      final double y2 = coordinates.getOrdinate(i, 1);
      final double distance = LineSegmentUtil.distance(x1, y1, x2, y2, x, y);
      if (distance < minDistance) {
        if (distance <= tolerance) {
          return tolerance;
        } else {
          minDistance = distance;
        }
      }
      x1 = x2;
      y1 = y2;
    }
    return minDistance;
  }

  public static boolean equalsExact3D(
    final Geometry geometry1,
    final Geometry geometry2) {
    if ((geometry1 instanceof LineString) && (geometry2 instanceof LineString)) {
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
    }
    return false;
  }

  public static boolean equalsExact3D(
    final GeometryCollection collection1,
    final GeometryCollection collection2) {
    if (collection1.getNumGeometries() != collection2.getNumGeometries()) {
      return false;
    } else {
      for (int i = 0; i < collection1.getNumGeometries(); i++) {
        final Geometry geometry1 = collection1.getGeometryN(i);
        final Geometry geometry2 = collection2.getGeometryN(i);
        if (!equalsExact3D(geometry1, geometry2)) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean equalsExact3D(
    final LineString line1,
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

  public static boolean equalsExact3D(
    final Point point1,
    final Point point2) {
    final Coordinate coordinate1 = point1.getCoordinate();
    final Coordinate coordinate2 = point2.getCoordinate();

    return (coordinate1.x == coordinate2.x) && (coordinate1.y == coordinate2.y)
      && equalsZ(coordinate1.z, coordinate2.z);
  }

  public static boolean equalsZ(
    final double z1,
    final double z2) {
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

  private static CoordinateSequence get2DCoordinates(
    final CoordinateSequence coordinateSequence) {
    final int numCoords = coordinateSequence.size();
    final CoordinateSequence coordinates = new DoubleCoordinatesList(numCoords,
      2);
    for (int i = 0; i < numCoords; i++) {
      final double x = coordinateSequence.getX(i);
      final double y = coordinateSequence.getY(i);
      coordinates.setOrdinate(i, 0, x);
      coordinates.setOrdinate(i, 1, y);
    }
    return coordinates;
  }

  public static Geometry get2DGeometry(
    final Geometry geometry) {
    final GeometryFactory factory = GeometryFactory.getFactory(geometry);
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return factory.createPoint(get2DCoordinates(point.getCoordinateSequence()));
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return factory.createLineString(get2DCoordinates(line.getCoordinateSequence()));
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

  public static LinearRing get2DGeometry(
    final LinearRing ring) {
    final CoordinateSequence coordinates = ring.getCoordinateSequence();
    final CoordinateSequence coordinates2d = get2DCoordinates(coordinates);
    final GeometryFactory factory = GeometryFactory.getFactory(ring);
    return factory.createLinearRing(coordinates2d);
  }

  public static List<Coordinate> getCoordinateList(
    final LineString line) {
    return new ArrayList<Coordinate>(Arrays.asList(line.getCoordinates()));

  }

  public static Set<Coordinate> getCoordinateSet(
    final LineString line) {
    final Set<Coordinate> coordinates = new LinkedHashSet<Coordinate>();
    final CoordinateSequence sequence = line.getCoordinateSequence();
    for (int i = 0; i < sequence.size(); i++) {
      final Coordinate coordinate = sequence.getCoordinate(i);
      coordinates.add(coordinate);
    }
    return coordinates;
  }

  /**
   * Get the coordinate where two lines cross, or null if they don't cross.
   * 
   * @param line1 The first line.
   * @param line2 The second line
   * @return The coordinate or null if they don't cross
   */
  public static Coordinate getCrossingIntersection(
    final LineString line1,
    final LineString line2) {
    final RobustLineIntersector intersector = new RobustLineIntersector();

    final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
    final CoordinateSequence coordinates2 = line2.getCoordinateSequence();
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

  public static double getElevation(
    final Coordinate coordinate,
    final Coordinate c0,
    final Coordinate c1) {
    final double fraction = coordinate.distance(c0) / c0.distance(c1);
    final double z = c0.z + (c1.z - c0.z) * (fraction);
    return z;
  }

  public static double getElevation(
    final LineSegment line,
    final Coordinate coordinate) {
    final Coordinate c0 = line.p0;
    final Coordinate c1 = line.p1;
    return getElevation(coordinate, c0, c1);
  }

  public static Geometry getGeometries(
    final GeometryFactory factory,
    final List<Coordinate> coords,
    final Set<Coordinate> intersectCoords) {
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

  public static DataObject getGeometryFeature(
    final Geometry geometry) {
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
    final Geometry geometry,
    final String name) {
    final Map<String, Object> map = getGeometryProperties(geometry);
    return (T)map.get(name);
  }

  public static Collection<LineString> getLines(
    final MultiLineString multiLine) {
    final List<LineString> lines = new ArrayList<LineString>();
    for (int i = 0; i < multiLine.getNumGeometries(); i++) {
      lines.add((LineString)multiLine.getGeometryN(i));
    }
    return lines;
  }

  public static List<LineSegment> getLineSegments(
    final CoordinateSequence coords) {
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

  public static List<LineSegment> getLineSegments(
    final LineString line) {
    final CoordinateSequence coords = line.getCoordinateSequence();
    return getLineSegments(coords);
  }

  public static LineString getMatchingLines(
    final LineString line1,
    final LineString line2,
    final double maxDistance) {
    final List<Coordinate> newCoords = new ArrayList<Coordinate>();
    final CoordinateSequence coords1 = line1.getCoordinateSequence();
    final CoordinateSequence coords2 = line1.getCoordinateSequence();
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

  public static LineString getMergeLine(
    final MultiLineString multiLineString) {
    final Collection<LineString> lineStrings = getMergedLines(multiLineString);
    final int numLines = lineStrings.size();
    if (numLines == 1) {
      return lineStrings.iterator().next();
    } else {
      return null;
    }
  }

  public static double getMiddleAngle(
    final double lastAngle,
    final double angle,
    final int orientation) {
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

  public static Geometry getMitredBuffer(
    final Geometry geometry,
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

  public static Polygon getMitredBuffer(
    final LineSegment segment,
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
    final GeometryFactory factory = new GeometryFactory();
    final LinearRing exteriorRing = factory.createLinearRing(coords);
    return factory.createPolygon(exteriorRing, null);
  }

  public static Polygon getMitredBuffer(
    final LineString lineString,
    final double distance) {
    final LineStringMitredBuffer visitor = new LineStringMitredBuffer(distance);
    visitLineSegments(lineString.getCoordinateSequence(), visitor);
    return visitor.getBuffer();
  }

  public static Polygon getMitredBuffer(
    final Polygon polygon,
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

  public static double[] getMRange(
    final CoordinateSequence coordinates) {
    return getOrdinateRange(coordinates, 3);
  }

  public static double[] getOrdinateRange(
    final CoordinateSequence coordinates,
    final int ordinateIndex) {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
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

  public static double[] getZRange(
    final CoordinateSequence coordinates) {
    return getOrdinateRange(coordinates, 2);
  }

  /**
   * Insert the coordinate at the specified index into the line, returning the
   * new line.
   * 
   * @param line The line.
   * @param index The index to insert the coordinate.
   * @param coordinate The coordinate.
   */
  public static LineString insert(
    final LineString line,
    final int index,
    final Coordinate coordinate) {
    final CoordinatesList coords = CoordinatesListUtil.get(line.getCoordinateSequence());
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
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final LineString newLine = factory.createLineString(newCoords);
    return newLine;
  }

  public static Geometry intersection2DZ(
    final LineString line,
    final Geometry geometry) {
    final Geometry intersection = line.intersection(geometry);
    if (intersection instanceof LineString) {
      final LineString lineDiff = (LineString)intersection;
      addElevation(line, lineDiff);

    } else {
      if (intersection instanceof MultiLineString) {
        for (int i = 0; i < intersection.getNumGeometries(); i++) {
          final LineString lineDiff = (LineString)intersection.getGeometryN(i);
          addElevation(line, lineDiff);
        }
      }
    }
    intersection.setUserData(line.getUserData());
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
  public static Geometry intersection2DZFloating(
    final LineString line1,
    final LineString line2) {
    final GeometryFactory factory = new GeometryFactory();
    final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
    final LineString line1Floating = factory.createLineString(coordinates1);
    final CoordinateSequence coordinates2 = line2.getCoordinateSequence();
    final LineString line2Floating = factory.createLineString(coordinates2);
    return JtsGeometryUtil.intersection2DZ(line1Floating, line2Floating);
  }

  public static boolean intersects(
    final Geometry geometry1,
    final Geometry geometry2) {
    final int srid2 = geometry2.getSRID();
    final Geometry projectedGeometry1 = GeometryProjectionUtil.perform(
      geometry1, srid2);
    return projectedGeometry1.intersects(geometry2);
  }

  public static boolean intersectsLinearly(
    final LineString line1,
    final LineString line2) {
    final IntersectionMatrix matrix = line1.relate(line2);
    return matrix.get(0, 0) == Dimension.L;
  }

  public static boolean isAlmostParallel(
    final LineString line,
    final LineString matchLine,
    final double maxDistance) {
    final CoordinateSequence coords = line.getCoordinateSequence();
    final CoordinateSequence matchCoords = line.getCoordinateSequence();
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

  public static boolean isBothWithinDistance(
    final LineString line1,
    final LineString line2,
    final double maxDistance) {
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
  public static boolean isCCW(
    final CoordinateSequence ring) {
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

  public static boolean isLessThanDistance(
    final Coordinate coordinate,
    final LineString line,
    final double maxDistance) {
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final Point point = factory.createPoint(coordinate);
    final double distance = line.distance(point);
    return distance < maxDistance;
  }

  public static boolean isLessThanDistance(
    final LineString line1,
    final LineString line2,
    final double maxDistance) {
    final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
    for (int i = 0; i < coordinates1.size(); i++) {
      final Coordinate coordinate = coordinates1.getCoordinate(i);
      if (!isLessThanDistance(coordinate, line2, maxDistance)) {
        return false;
      }
    }
    return true;
  }

  public static boolean isWithinDistance(
    final Coordinate coordinate,
    final LineString line,
    final double maxDistance) {
    final GeometryFactory factory = GeometryFactory.getFactory(line);
    final Point point = factory.createPoint(coordinate);
    final double distance = line.distance(point);
    return distance <= maxDistance;
  }

  public static boolean isWithinDistance(
    final LineString line1,
    final LineString line2,
    final double maxDistance) {
    final CoordinateSequence coordinates1 = line1.getCoordinateSequence();
    for (int i = 0; i < coordinates1.size(); i++) {
      final Coordinate coordinate = coordinates1.getCoordinate(i);
      if (!isWithinDistance(coordinate, line2, maxDistance)) {
        return false;
      }
    }
    return true;
  }

  public static void makePrecise(
    final PrecisionModel precision,
    final Geometry geometry) {
    geometry.apply(new CoordinateSequenceFilter() {
      public void filter(
        final CoordinateSequence coordinates,
        final int index) {
        for (int i = 0; i < coordinates.getDimension(); i++) {
          final double ordinate = coordinates.getOrdinate(index, i);
          final double preciseOrdinate = precision.makePrecise(ordinate);
          coordinates.setOrdinate(index, i, preciseOrdinate);
        }
      }

      public boolean isDone() {
        return false;
      }

      public boolean isGeometryChanged() {
        return true;
      }

    });
  }

  public static <T extends Geometry> T makePreciseCopy(
    final PrecisionModel precisionModel,
    final T geometry) {
    final T newGeometry = (T)geometry.clone();
    makePrecise(precisionModel, newGeometry);
    return newGeometry;
  }

  public static Coordinate offset(
    final Coordinate coordinate,
    final double angle,
    final double distance) {
    final double newX = coordinate.x + distance * Math.cos(angle);
    final double newY = coordinate.y + distance * Math.sin(angle);
    final Coordinate newCoordinate = new Coordinate(newX, newY);
    return newCoordinate;

  }

  public static LineSegment offset(
    final LineSegment line,
    final double distance,
    final int orientation) {
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

  public static Coordinate processIntersection(
    final Set<Coordinate> intersectCoords,
    final Coordinate intersectCoord,
    final List<Coordinate> coords1,
    final ListIterator<Coordinate> iterator1,
    final int index1,
    final Coordinate previousCoord1,
    final Coordinate currentCoord1,
    final List<Coordinate> coords2,
    final ListIterator<Coordinate> iterator2,
    final int index2,
    final Coordinate previousCoord2,
    final Coordinate currentCoord2,
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

  public static Polygon reversePolygon(
    final Polygon polygon) {
    final GeometryFactory factory = GeometryFactory.getFactory(polygon);
    final LineString exteriorRing = polygon.getExteriorRing();
    final CoordinateSequence oldCoordinates = exteriorRing.getCoordinateSequence();
    final CoordinatesList newCoordinates = CoordinateSequenceUtil.reverse(oldCoordinates);
    final LinearRing shell = factory.createLinearRing(newCoordinates);
    return factory.createPolygon(shell, null);
  }

  public static void setCoordinate(
    final CoordinateSequence coordinates,
    final int i,
    final Coordinate coordinate) {
    coordinates.setOrdinate(i, 0, coordinate.x);
    coordinates.setOrdinate(i, 1, coordinate.y);
    if (coordinates.getDimension() > 2) {
      coordinates.setOrdinate(i, 2, coordinate.z);
    }

  }

  public static void setGeometryFeature(
    final Geometry geometry,
    final DataObject feature) {
    setGeometryProperty(geometry, FEATURE_PROPERTY, feature);
  }

  @SuppressWarnings("unchecked")
  public static void setGeometryProperty(
    final Geometry geometry,
    final CharSequence name,
    final Object value) {
    Object userData = geometry.getUserData();
    if (!(userData instanceof Map)) {
      userData = new TreeMap<Object, Object>();
      geometry.setUserData(userData);
    }
    final Map<Object, Object> map = (Map<Object, Object>)userData;
    map.put(name.toString(), value);

  }

  public static List<LineString> split(
    final LineString line,
    final int segmentIndex,
    final Coordinate coordinate) {
    final List<LineString> lines = new ArrayList<LineString>();
    final boolean containsCoordinate = coordinate.equals(line.getCoordinateN(segmentIndex));
    final CoordinateSequence coords = line.getCoordinateSequence();
    final int dimension = coords.getDimension();
    int coords1Size;
    int coords2Size = coords.size() - segmentIndex;
    if (containsCoordinate) {
      coords1Size = segmentIndex + 1;
      coords2Size = coords.size() - segmentIndex;
    } else {
      coords1Size = segmentIndex + 2;
      coords2Size = coords.size() - segmentIndex;
    }
    final CoordinateSequence coords1 = new DoubleCoordinatesList(coords1Size,
      dimension);
    CoordinateSequenceUtil.copy(coords, 0, coords1, 0, segmentIndex + 1);

    final CoordinateSequence coords2 = new DoubleCoordinatesList(coords2Size,
      dimension);
    if (!containsCoordinate) {
      setCoordinate(coords1, coords1Size - 1, coordinate);
      setCoordinate(coords2, 0, coordinate);
      if (coords1.getDimension() > 2) {
        final Coordinate previousCoord = coords1.getCoordinate(segmentIndex);
        final Coordinate nextCoord = coords.getCoordinate(segmentIndex + 1);
        final double z = getElevation(coordinate, previousCoord, nextCoord);
        coords1.setOrdinate(coords1Size - 1, 2, z);
        coords2.setOrdinate(0, 2, z);
      }

      CoordinateSequenceUtil.copy(coords, segmentIndex + 1, coords2, 1,
        coords2.size() - 1);
    } else {
      CoordinateSequenceUtil.copy(coords, segmentIndex, coords2, 0,
        coords2.size());
    }

    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);

    if (coords1Size > 1) {
      final LineString line1 = geometryFactory.createLineString(coords1);
      if (line1.getLength() > 0) {
        lines.add(line1);
      }
    }

    if (coords2Size > 1) {
      final LineString line2 = geometryFactory.createLineString(coords2);
      if (line2.getLength() > 0) {
        lines.add(line2);
      }
    }
    return lines;
  }

  public static List<Geometry> splitWhereCross(
    final LineString line1,
    final LineString line2) {
    final RobustLineIntersector intersector = new RobustLineIntersector();
    final List<Geometry> geometries = new ArrayList<Geometry>();

    final List<Coordinate> coords1 = new LinkedList<Coordinate>(
      Arrays.asList(line1.getCoordinates()));
    final List<Coordinate> coords2 = new LinkedList<Coordinate>(
      Arrays.asList(line2.getCoordinates()));
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
      geometries.add(getGeometries(GeometryFactory.getFactory(line1), coords1, intersectCoords));
      geometries.add(getGeometries(GeometryFactory.getFactory(line2), coords2, intersectCoords));
    }

    return geometries;

  }

  public static boolean startAndEndEqual(
    final LineString geometry1,
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

  public static CoordinateSequence to2D(
    final CoordinateSequence coordinates) {
    final CoordinateSequence newCoordinates = new DoubleCoordinatesList(
      coordinates.size(), 2);
    for (int i = 0; i < coordinates.size(); i++) {
      newCoordinates.setOrdinate(i, 0, coordinates.getOrdinate(i, 0));
      newCoordinates.setOrdinate(i, 1, coordinates.getOrdinate(i, 1));
    }
    return newCoordinates;
  }

  public static Geometry to2D(
    final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return to2D(point);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint point = (MultiPoint)geometry;
      return to2D(point);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return to2D(line);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return to2D(polygon);
    } else {
      return null;
    }
  }

  public static LinearRing to2D(
    final LinearRing geometry) {
    final GeometryFactory factory =GeometryFactory.getFactory(geometry);
    final CoordinateSequence coordinates = geometry.getCoordinateSequence();
    final CoordinateSequence newCoordinates = to2D(coordinates);
    final LinearRing newGeometry = factory.createLinearRing(newCoordinates);
    newGeometry.setUserData(geometry.getUserData());
    return newGeometry;
  }

  public static LineString to2D(
    final LineString geometry) {
    final GeometryFactory factory = GeometryFactory.getFactory(geometry);
    final CoordinateSequence coordinates = geometry.getCoordinateSequence();
    final CoordinateSequence newCoordinates = to2D(coordinates);
    final LineString newGeometry = factory.createLineString(newCoordinates);
    newGeometry.setUserData(geometry.getUserData());
    return newGeometry;
  }

  public static MultiPoint to2D(
    final MultiPoint geometry) {
    final GeometryFactory factory = GeometryFactory.getFactory(geometry);
    final Point[] points = new Point[geometry.getNumGeometries()];
    for (int i = 0; i < points.length; i++) {
      final Point point = (Point)geometry.getGeometryN(i);
      points[i] = to2D(point);
    }
    final MultiPoint newGeometry = factory.createMultiPoint(points);
    newGeometry.setUserData(geometry.getUserData());
    return newGeometry;
  }

  public static Point to2D(
    final Point geometry) {
    final GeometryFactory factory = GeometryFactory.getFactory(geometry);
    final CoordinateSequence coordinates = geometry.getCoordinateSequence();
    final CoordinateSequence newCoordinates = to2D(coordinates);
    final Point newGeometry = factory.createPoint(newCoordinates);
    newGeometry.setUserData(geometry.getUserData());
    return newGeometry;
  }

  public static Geometry to2D(
    final Polygon geometry) {

    final GeometryFactory factory = GeometryFactory.getFactory(geometry);

    final LinearRing shell = to2D((LinearRing)geometry.getExteriorRing());
    final LinearRing[] holes = new LinearRing[geometry.getNumInteriorRing()];
    for (int i = 0; i < holes.length; i++) {
      final LinearRing hole = (LinearRing)geometry.getInteriorRingN(i);
      holes[i] = to2D(hole);
    }
    final Polygon newGeometry = factory.createPolygon(shell, holes);
    newGeometry.setUserData(geometry.getUserData());
    return newGeometry;
  }

  /**
   * @param line1 The line to match
   * @param line2 The line to compare the start of with the other line
   * @return
   */
  public static boolean touchesAtEnd(
    final LineString line1,
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
  public static boolean touchesAtStart(
    final LineString line1,
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

  public static void visitLineSegments(
    final CoordinateSequence coords,
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

  public static Geometry unionAll(
    List<Geometry> geometries) {
    GeometryFactory.toGeometryArray(geometries);
    final GeometryFactory factory = GeometryFactory.getFactory(geometries.get(0));
    final Geometry geometry = factory.createGeometry(geometries);
    if (geometry.getNumGeometries() == 1) {
      return geometry;
    } else {
      return geometry.union();
    }
  }
}
