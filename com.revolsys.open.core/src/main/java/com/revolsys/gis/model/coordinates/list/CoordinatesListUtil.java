package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.linestring.LineStringGraph;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.util.MathUtil;
import com.vividsolutions.jts.algorithm.RobustDeterminant;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class CoordinatesListUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static void addElevation(
    final CoordinatesPrecisionModel precisionModel,
    final Coordinates coordinate,
    final CoordinatesList line) {
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final CoordinatesListCoordinates previousCoordinate = new CoordinatesListCoordinates(
      points, 0);
    final CoordinatesListCoordinates currentCoordinate = new CoordinatesListCoordinates(
      points, 0);
    for (int i = 1; i < points.size(); i++) {
      currentCoordinate.next();

      if (LineSegmentUtil.isPointOnLine(precisionModel, previousCoordinate,
        currentCoordinate, coordinate)) {
        LineSegmentUtil.addElevation(precisionModel, previousCoordinate,
          currentCoordinate, coordinate);
        return;
      }
      previousCoordinate.next();
    }

  }

  public static List<Coordinates> get(Point... points) {
    List<Coordinates> coordinatesList = new ArrayList<Coordinates>();
    for (Point point : points) {
      Coordinates coordinates = CoordinatesUtil.get(point);
      coordinatesList.add(coordinates);
    }
    return coordinatesList;
  }

  public static List<Coordinates> get(List<Point> points) {
    List<Coordinates> coordinatesList = new ArrayList<Coordinates>();
    for (Point point : points) {
      Coordinates coordinates = CoordinatesUtil.get(point);
      coordinatesList.add(coordinates);
    }
    return coordinatesList;
  }

  public static double angle(
    final CoordinatesList points,
    final int i1,
    final int i2) {
    final double x1 = points.getX(i1);
    final double y1 = points.getY(i1);
    final double x2 = points.getX(i2);
    final double y2 = points.getY(i2);
    final double angle = MathUtil.angle(x1, y1, x2, y2);
    return angle;
  }

  public static double angleToNext(final CoordinatesList points, final int i) {
    final double x1 = points.getX(i);
    final double y1 = points.getY(i);
    double x2;
    double y2;
    int j = i + 1;
    do {
      x2 = points.getX(j);
      y2 = points.getY(j);
      j++;
    } while (x1 == x2 && y1 == y2 && j < points.size());
    final double angle = MathUtil.angle(x1, y1, x2, y2);
    return angle;
  }

  public static double angleToPrevious(final CoordinatesList points, final int i) {
    if (i > 0) {
      final double x1 = points.getX(i);
      final double y1 = points.getY(i);
      double x2;
      double y2;
      int j = i - 1;
      do {
        x2 = points.getX(j);
        y2 = points.getY(j);
        j--;
      } while (x1 == x2 && y1 == y2 && j > -1);
      final double angle = MathUtil.angle(x1, y1, x2, y2);
      return angle;
    } else {
      throw new IllegalArgumentException(
        "Index must be > 0 to calculate previous angle");
    }
  }

  public static int append(
    final CoordinatesList src,
    final CoordinatesList dest,
    final int startIndex) {
    int coordIndex = startIndex;
    final int srcDimension = src.getDimension();
    final int destDimension = dest.getDimension();
    final int dimension = Math.min(srcDimension, destDimension);
    final int srcSize = src.size();
    final int destSize = dest.size();
    double previousX;
    double previousY;
    if (startIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = dest.getX(startIndex - 1);
      previousY = dest.getY(startIndex - 1);
    }
    for (int i = 0; i < srcSize && coordIndex < destSize; i++) {
      final double x = src.getX(i);
      final double y = src.getY(i);
      if (x != previousX || y != previousY) {
        dest.setValue(coordIndex, 0, x);
        dest.setValue(coordIndex, 1, y);
        for (int d = 2; d < dimension; d++) {
          final double ordinate = src.getValue(i, d);
          dest.setValue(coordIndex, d, ordinate);
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  public static int appendReversed(
    final CoordinatesList src,
    final CoordinatesList dest,
    final int startIndex) {
    int coordIndex = startIndex;
    final int srcDimension = src.getDimension();
    final int destDimension = dest.getDimension();
    final int dimension = Math.min(srcDimension, destDimension);
    final int srcSize = src.size();
    final int destSize = dest.size();
    double previousX;
    double previousY;
    if (startIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = dest.getX(startIndex - 1);
      previousY = dest.getY(startIndex - 1);
    }
    for (int i = srcSize - 1; i > -1 && coordIndex < destSize; i--) {
      final double x = src.getX(i);
      final double y = src.getY(i);
      if (x != previousX || y != previousY) {
        dest.setValue(coordIndex, 0, x);
        dest.setValue(coordIndex, 1, y);
        for (int d = 2; d < dimension; d++) {
          final double ordinate = src.getValue(i, d);
          dest.setValue(coordIndex, d, ordinate);
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  /**
   * <p>
   * Check within a given tolerance that the LINESTRING defined by points2 is
   * contained within the points1.
   * </p>
   * <p>
   * The algorithm is as follows:
   * <ol>
   * <li>Find all coordinates from points2 that are within the tolerance from
   * the line segments of points1.</li>
   * <li>Find all coordinates from points1 that are within the tolerance from
   * the line segments of points2.</li>
   * <li>Split all the line sgements of points1 that were matched in step 1.</li>
   * <li>Split all the line sgements of points2 that were matched in step 2.</li>
   * <li>Line is contained if all line segments from point2 have matching lines
   * in points1.</li>
   * </ol>
   * 
   * @param points1
   * @param points2
   * @param tolerance
   * @return
   */
  public static boolean containsWithinTolerance(
    final CoordinatesList points1,
    final CoordinatesList points2,
    final double tolerance) {

    final LineStringGraph graph1 = new LineStringGraph(points1);
    final LineStringGraph graph2 = new LineStringGraph(points2);
    graph1.visitNodes(new InvokeMethodVisitor<Node<LineSegment>>(
      CoordinatesListUtil.class, "movePointsWithinTolerance", null, graph2,
      tolerance));
    graph2.visitNodes(new InvokeMethodVisitor<Node<LineSegment>>(
      CoordinatesListUtil.class, "movePointsWithinTolerance", null, graph1,
      tolerance));

    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = graph1.getPointsOnEdges(
      graph2, tolerance);
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge2 = graph2.getPointsOnEdges(
      graph1, tolerance);
    graph1.splitEdges(pointsOnEdge1);
    graph2.splitEdges(pointsOnEdge2);
    for (final Edge<LineSegment> edge : graph2.edges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (!graph1.hasEdgeBetween(fromNode, toNode)) {
        return false;
      }
    }
    return true;
  }

  public static CoordinatesList create(
    final int numAxis,
    final Coordinates... coordinateArray) {
    return create(Arrays.asList(coordinateArray), numAxis);
  }

  public static CoordinatesList create(
    final List<Coordinates> coordinateArray,
    final int numAxis) {
    CoordinatesList coordinatesList = new DoubleCoordinatesList(
      coordinateArray.size(), numAxis);
    int i = 0;
    for (final Coordinates coordinates : coordinateArray) {
      if (coordinates != null) {
        for (int j = 0; j < coordinates.getNumAxis(); j++) {
          coordinatesList.setValue(i, j, coordinates.getValue(j));
        }
        i++;
      }
    }
    if (i < coordinatesList.size()) {
      coordinatesList = coordinatesList.subList(0, i);
    }
    return coordinatesList;
  }

  public static boolean equals2dCoordinate(
    final CoordinatesList coordinates,
    final int index,
    final double x,
    final double y) {
    return coordinates.getX(index) == x && coordinates.getY(index) == y;
  }

  public static boolean equals2dCoordinates(
    final CoordinatesList coordinates,
    final int index1,
    final int index2) {
    return coordinates.getX(index1) == coordinates.getOrdinate(index2, 0)
      && coordinates.getY(index1) == coordinates.getOrdinate(index2, 1);
  }

  public static boolean equalWithinTolerance(
    final CoordinatesList points1,
    final CoordinatesList points2,
    final double tolerance) {
    final Set<Coordinates> pointSet1 = getCoordinatesSet2d(points1);
    final Set<Coordinates> pointSet2 = new TreeSet<Coordinates>();
    for (int i = 0; i < points2.size() - 1; i++) {
      final Coordinates point2 = points2.get(i);
      if (pointSet1.contains(point2)) {
        pointSet1.remove(point2);
      } else if (isWithinDistanceOfPoints(point2, points1, tolerance)) {
        pointSet1.remove(point2);
      } else {
        pointSet2.add(new DoubleCoordinates(point2, 2));
      }
    }
    for (final Iterator<Coordinates> iterator1 = pointSet1.iterator(); iterator1.hasNext();) {
      final Coordinates point1 = iterator1.next();
      if (isWithinDistanceOfPoints(point1, points2, tolerance)) {
        iterator1.remove();
        pointSet2.remove(point1);
      }
    }
    for (final Iterator<Coordinates> iterator1 = pointSet1.iterator(); iterator1.hasNext();) {
      final Coordinates point1 = iterator1.next();
      if (isPointOnLine(point1, points2, tolerance)) {
        iterator1.remove();
      } else {
        return false;
      }
    }
    for (final Iterator<Coordinates> iterator2 = pointSet2.iterator(); iterator2.hasNext();) {
      final Coordinates point2 = iterator2.next();
      if (isPointOnLine(point2, points1, tolerance)) {
        iterator2.remove();
      } else {
        return false;
      }
    }
    return true;
  }

  public static Map<String, Number> findClosestSegmentAndCoordinate(
    final CoordinatesList points,
    final Coordinates point) {
    final Map<String, Number> result = new HashMap<String, Number>();
    result.put(SEGMENT_INDEX, -1);
    result.put(COORDINATE_INDEX, -1);
    result.put(COORDINATE_DISTANCE, Double.MAX_VALUE);
    result.put(SEGMENT_DISTANCE, Double.MAX_VALUE);
    double closestDistance = Double.MAX_VALUE;
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      points);
    if (iterator.hasNext()) {
      LineSegment segment = iterator.next();
      final double previousCoordinateDistance = segment.get(0).distance(point);
      if (previousCoordinateDistance == 0) {
        result.put(SEGMENT_INDEX, 0);
        result.put(COORDINATE_INDEX, 0);
        result.put(COORDINATE_DISTANCE, 0.0);
        result.put(SEGMENT_DISTANCE, 0.0);
      } else {
        int i = 1;

        while (segment != null) {
          final double currentCoordinateDistance = segment.get(1).distance(
            point);
          if (currentCoordinateDistance == 0) {
            result.put(SEGMENT_INDEX, i);
            result.put(COORDINATE_INDEX, i);
            result.put(COORDINATE_DISTANCE, 0.0);
            result.put(SEGMENT_DISTANCE, 0.0);
            return result;
          }
          final double distance = segment.distance(point);
          if (distance == 0) {
            result.put(SEGMENT_INDEX, i - 1);
            result.put(SEGMENT_DISTANCE, 0.0);
            if (previousCoordinateDistance < currentCoordinateDistance) {
              result.put(COORDINATE_INDEX, i - 1);
              result.put(COORDINATE_DISTANCE, previousCoordinateDistance);
            } else {
              result.put(COORDINATE_INDEX, i);
              result.put(COORDINATE_DISTANCE, currentCoordinateDistance);
            }
            return result;
          } else if (distance < closestDistance) {
            result.put(SEGMENT_DISTANCE, distance);
            closestDistance = distance;
            result.put(SEGMENT_INDEX, i - 1);
            if (previousCoordinateDistance < currentCoordinateDistance) {
              result.put(COORDINATE_INDEX, i - 1);
              result.put(COORDINATE_DISTANCE, previousCoordinateDistance);
            } else {
              result.put(COORDINATE_INDEX, i);
              result.put(COORDINATE_DISTANCE, currentCoordinateDistance);
            }
          }
          if (iterator.hasNext()) {
            i++;
            segment = iterator.next();
          } else {
            segment = null;
          }
        }
      }
    }
    return result;
  }

  public static final CoordinatesList get(
    final CoordinateSequence coordinateSequence) {
    if (coordinateSequence instanceof CoordinatesList) {
      return (CoordinatesList)coordinateSequence;
    } else {
      return new CoordinateSequenceCoordinateList(coordinateSequence);
    }

  }

  public static CoordinatesList get(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else if (geometry instanceof Point) {
      return get((Point)geometry);
    } else if (geometry instanceof LineString) {
      return get((LineString)geometry);
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return get(polygon);
    } else if (geometry.getNumGeometries() > 0) {
      return get(geometry.getGeometryN(0));
    } else {
      return null;
    }
  }

  public static CoordinatesList get(final LineString line) {
    return get(line.getCoordinateSequence());
  }

  public static Coordinates get(final LineString line, final int i) {
    final CoordinatesList points = get(line);
    return points.get(i);
  }

  public static CoordinatesList get(final Point point) {
    return get(point.getCoordinateSequence());
  }

  private static CoordinatesList get(final Polygon polygon) {
    if (polygon == null) {
      return null;
    } else {
      return get(polygon.getExteriorRing());
    }
  }

  public static List<CoordinatesList> getAll(final Geometry geometry) {
    final List<CoordinatesList> pointsList = new ArrayList<CoordinatesList>();
    if (geometry != null) {
      for (int i = 0; i < geometry.getNumGeometries(); i++) {
        final Geometry subGeometry = geometry.getGeometryN(i);
        if (subGeometry instanceof Point) {
          pointsList.add(get((Point)subGeometry));
        } else if (subGeometry instanceof LineString) {
          pointsList.add(get((LineString)subGeometry));
        } else if (subGeometry instanceof Polygon) {
          final Polygon polygon = (Polygon)subGeometry;
          final LineString exteriorRing = polygon.getExteriorRing();
          pointsList.add(get(exteriorRing));
          for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
            final LineString ring = polygon.getInteriorRingN(j);
            pointsList.add(get(ring));
          }
        }
      }
    }
    return pointsList;
  }

  private static Set<Coordinates> getCoordinatesSet2d(
    final CoordinatesList points) {
    final Set<Coordinates> pointSet = new TreeSet<Coordinates>();
    for (final Coordinates point : new InPlaceIterator(points)) {
      pointSet.add(new DoubleCoordinates(point, 2));
    }
    return pointSet;
  }

  public static List<CoordinatesList> intersection(
    final GeometryFactory geometryFactory,
    final CoordinatesList points1,
    final CoordinatesList points2,
    final double maxDistance) {

    final LineStringGraph graph1 = new LineStringGraph(points1);
    graph1.setPrecisionModel(geometryFactory);
    final LineStringGraph graph2 = new LineStringGraph(points2);
    graph2.setPrecisionModel(geometryFactory);
    final Map<Coordinates, Coordinates> movedNodes = new HashMap<Coordinates, Coordinates>();
    graph1.visitNodes(new InvokeMethodVisitor<Node<LineSegment>>(
      CoordinatesListUtil.class, "movePointsWithinTolerance", movedNodes,
      graph2, maxDistance));
    graph2.visitNodes(new InvokeMethodVisitor<Node<LineSegment>>(
      CoordinatesListUtil.class, "movePointsWithinTolerance", movedNodes,
      graph1, maxDistance));

    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = graph1.getPointsOnEdges(
      graph2, maxDistance);
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge2 = graph2.getPointsOnEdges(
      graph1, maxDistance);
    graph1.splitEdges(pointsOnEdge1);
    graph2.splitEdges(pointsOnEdge2);
    Coordinates startPoint = points1.get(0);
    if (movedNodes.containsKey(startPoint)) {
      startPoint = movedNodes.get(startPoint);
    }
    Coordinates endPoint = points1.get(points1.size() - 1);
    if (movedNodes.containsKey(endPoint)) {
      endPoint = movedNodes.get(endPoint);
    }
    final List<CoordinatesList> intersections = new ArrayList<CoordinatesList>();
    final PointArrayCoordinatesList currentCoordinates = new PointArrayCoordinatesList(
      points1.getNumAxis());
    Node<LineSegment> previousNode = graph1.getNode(startPoint);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else if (outEdges.size() > 1) {
        throw new IllegalArgumentException("Cannot handle overlaps\n" + points1
          + "\n " + points2);
      } else {
        final Edge<LineSegment> edge = outEdges.get(0);
        final LineSegment line = edge.getObject();
        final Node<LineSegment> nextNode = edge.getToNode();
        if (graph2.hasEdgeBetween(previousNode, nextNode)) {
          if (currentCoordinates.size() == 0) {
            currentCoordinates.add(line.get(0));
          }
          currentCoordinates.add(line.get(1));
        } else {
          if (currentCoordinates.size() > 0) {
            final CoordinatesList points = new DoubleCoordinatesList(
              currentCoordinates);
            intersections.add(points);
            currentCoordinates.clear();
          }
        }
        previousNode = nextNode;
      }

    } while (previousNode != null && !endPoint.equals2d(startPoint));
    if (currentCoordinates.size() > 0) {
      final CoordinatesList points = new DoubleCoordinatesList(
        currentCoordinates);
      intersections.add(points);
    }
    return intersections;
  }

  public static boolean isCCW(final CoordinatesList ring) {
    // # of points without closing endpoint
    final int nPts = ring.size() - 1;

    // find highest point
    double hiPtX = ring.getX(0);
    double hiPtY = ring.getY(0);
    int hiIndex = 0;
    for (int i = 1; i <= nPts; i++) {
      final double x = ring.getX(i);
      final double y = ring.getY(i);
      if (y > hiPtY) {
        hiPtX = x;
        hiPtY = y;
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
    } while (equals2dCoordinate(ring, iPrev, hiPtX, hiPtY) && iPrev != hiIndex);

    // find distinct point after highest point
    int iNext = hiIndex;
    do {
      iNext = (iNext + 1) % nPts;
    } while (equals2dCoordinate(ring, iNext, hiPtX, hiPtY) && iNext != hiIndex);

    /**
     * This check catches cases where the ring contains an A-B-A configuration
     * of points. This can happen if the ring does not contain 3 distinct points
     * (including the case where the input array has fewer than 4 elements), or
     * it contains coincident line segments.
     */
    if (equals2dCoordinate(ring, iPrev, hiPtX, hiPtY)
      || equals2dCoordinate(ring, iNext, hiPtX, hiPtY)
      || equals2dCoordinates(ring, iPrev, iNext)) {
      return false;
    }

    final int disc = orientationIndex(ring, iPrev, hiIndex, iNext);

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
      isCCW = (ring.getOrdinate(iPrev, 0) > ring.getOrdinate(iPrev, 1));
    } else {
      // if area is positive, points are ordered CCW
      isCCW = (disc > 0);
    }
    return isCCW;
  }

  public static boolean isPointOnLine(
    final Coordinates coordinate,
    final CoordinatesList points,
    final double tolerance) {
    final CoordinatesListCoordinates previousCoordinate = new CoordinatesListCoordinates(
      points, 0);
    final CoordinatesListCoordinates currentCoordinate = new CoordinatesListCoordinates(
      points, 0);
    for (int i = 1; i < points.size(); i++) {
      currentCoordinate.next();

      if (LineSegmentUtil.isPointOnLine(previousCoordinate, currentCoordinate,
        coordinate, tolerance)) {
        return true;
      }
      previousCoordinate.next();
    }
    return false;
  }

  public static boolean isWithinDistanceOfPoints(
    final Coordinates point,
    final CoordinatesList points,
    final double maxDistance) {
    for (final Coordinates point2 : new InPlaceIterator(points)) {
      if (point.distance(point2) < maxDistance) {
        return true;
      }
    }
    return false;
  }

  public static double length2d(final CoordinatesList points) {
    double length = 0;
    final int size = points.size();
    if (size > 1) {
      double x1 = points.getX(0);
      double y1 = points.getY(0);
      for (int i = 1; i < size; i++) {
        final double x2 = points.getX(i);
        final double y2 = points.getY(i);
        length += MathUtil.distance(x1, y1, x2, y2);
        x1 = x2;
        y1 = y2;
      }
    }
    return length;
  }

  public static CoordinatesList merge(
    final Coordinates point,
    final CoordinatesList coordinates1,
    final CoordinatesList coordinates2) {
    final int dimension = Math.max(coordinates1.getDimension(),
      coordinates2.getDimension());
    final int maxSize = coordinates1.size() + coordinates2.size();
    final CoordinatesList coordinates = new DoubleCoordinatesList(maxSize,
      dimension);

    int numCoords = 0;
    final Coordinates coordinates1Start = coordinates1.get(0);
    final Coordinates coordinates1End = coordinates1.get(coordinates1.size() - 1);
    final Coordinates coordinates2Start = coordinates2.get(0);
    final Coordinates coordinates2End = coordinates2.get(coordinates2.size() - 1);
    if (coordinates1Start.equals2d(coordinates2End)
      && coordinates1Start.equals2d(point)) {
      numCoords = append(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates2Start.equals2d(coordinates1End)
      && coordinates2Start.equals2d(point)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = append(coordinates2, coordinates, numCoords);
    } else if (coordinates1Start.equals2d(coordinates2Start)
      && coordinates1Start.equals2d(point)) {
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates1End.equals2d(coordinates2End)
      && coordinates1End.equals2d(point)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + coordinates1
        + "\n" + coordinates2);

    }
    return trim(coordinates, numCoords);
  }

  public static CoordinatesList merge(
    final CoordinatesList coordinates1,
    final CoordinatesList coordinates2) {
    final int dimension = Math.max(coordinates1.getDimension(),
      coordinates2.getDimension());
    final int maxSize = coordinates1.size() + coordinates2.size();
    final CoordinatesList coordinates = new DoubleCoordinatesList(maxSize,
      dimension);

    int numCoords = 0;
    final Coordinates coordinates1Start = coordinates1.get(0);
    final Coordinates coordinates1End = coordinates1.get(coordinates1.size() - 1);
    final Coordinates coordinates2Start = coordinates2.get(0);
    final Coordinates coordinates2End = coordinates2.get(coordinates2.size() - 1);
    if (coordinates1Start.equals2d(coordinates2End)) {
      numCoords = append(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates2Start.equals2d(coordinates1End)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = append(coordinates2, coordinates, numCoords);
    } else if (coordinates1Start.equals2d(coordinates2Start)) {
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
      numCoords = append(coordinates1, coordinates, numCoords);
    } else if (coordinates1End.equals2d(coordinates2End)) {
      numCoords = append(coordinates1, coordinates, numCoords);
      numCoords = appendReversed(coordinates2, coordinates, numCoords);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + coordinates1
        + "\n" + coordinates2);

    }
    return trim(coordinates, numCoords);
  }

  public static CoordinatesList merge(
    final List<CoordinatesList> coordinatesList) {
    final Iterator<CoordinatesList> iterator = coordinatesList.iterator();
    if (!iterator.hasNext()) {
      return null;
    } else {
      CoordinatesList coordinates = iterator.next();
      while (iterator.hasNext()) {
        final CoordinatesList nextCoordinates = iterator.next();
        coordinates = merge(coordinates, nextCoordinates);
      }
      return coordinates;
    }
  }

  /**
   * Only move the node if there is one of them
   * 
   * @param graph2
   * @param maxDistance
   * @param node1
   * @return
   */
  public static <T> boolean movePointsWithinTolerance(
    final Map<Coordinates, Coordinates> movedNodes,
    final Graph<T> graph2,
    final double maxDistance,
    final Node<T> node1) {
    final Graph<T> graph1 = node1.getGraph();
    final List<Node<T>> nodes2 = graph2.findNodes(node1, maxDistance);
    if (nodes2.size() == 1) {
      final Node<T> node2 = nodes2.get(0);
      if (graph1.findNode(node2) == null) {
        final CoordinatesPrecisionModel precisionModel = graph1.getPrecisionModel();
        final Coordinates midPoint = LineSegmentUtil.midPoint(precisionModel,
          node1, node2);
        if (!node1.equals2d(midPoint)) {
          if (movedNodes != null) {
            movedNodes.put(node1.clone(), midPoint);
          }
          node1.move(midPoint);
        }
        if (!node2.equals2d(midPoint)) {
          if (movedNodes != null) {
            movedNodes.put(node2.clone(), midPoint);
          }
          node2.move(midPoint);
        }
      }
    }
    return true;
  }

  public static int orientationIndex(
    final CoordinatesList ring,
    final int index1,
    final int index2,
    final int index) {
    return orientationIndex(ring.getX(index1), ring.getY(index1),
      ring.getX(index2), ring.getY(index2), ring.getX(index), ring.getY(index));
  }

  /**
   * Returns the index of the direction of the point <code>q</code> relative to
   * a vector specified by <code>p1-p2</code>.
   * 
   * @param p1 the origin point of the vector
   * @param p2 the final point of the vector
   * @param q the point to compute the direction to
   * @return 1 if q is counter-clockwise (left) from p1-p2
   * @return -1 if q is clockwise (right) from p1-p2
   * @return 0 if q is collinear with p1-p2
   */
  public static int orientationIndex(
    final double x1,
    final double y1,
    final double x2,
    final double y2,
    final double x,
    final double y) {
    // travelling along p1->p2, turn counter clockwise to get to q return 1,
    // travelling along p1->p2, turn clockwise to get to q return -1,
    // p1, p2 and q are colinear return 0.
    final double dx1 = x2 - x1;
    final double dy1 = y2 - y1;
    final double dx2 = x - x2;
    final double dy2 = y - y2;
    return RobustDeterminant.signOfDet2x2(dx1, dy1, dx2, dy2);
  }

  public static final CoordinatesList parse(
    final String value,
    final String separator,
    final int numAxis) {
    final String[] values = value.split(separator);
    final double[] coordinates = new double[values.length];
    for (int i = 0; i < values.length; i++) {
      final String string = values[i];
      coordinates[i] = Double.parseDouble(string);
    }
    return new DoubleCoordinatesList(numAxis, coordinates);
  }

  public static CoordinatesList parse(
    final String value,
    final String decimal,
    String coordSeperator,
    String toupleSeperator) {

    toupleSeperator = toupleSeperator.replaceAll("\\\\", "\\\\\\\\");
    toupleSeperator = toupleSeperator.replaceAll("\\.", "\\\\.");
    final Pattern touplePattern = Pattern.compile("\\s*" + toupleSeperator
      + "\\s*");
    final String[] touples = touplePattern.split(value);

    coordSeperator = coordSeperator.replaceAll("\\\\", "\\\\\\\\");
    coordSeperator = coordSeperator.replaceAll("\\.", "\\\\.");
    final Pattern coordinatePattern = Pattern.compile("\\s*" + coordSeperator
      + "\\s*");

    int numAxis = 0;
    final List<double[]> listOfCoordinateArrays = new ArrayList<double[]>();
    if (touples.length == 0) {
      return null;
    } else {
      for (final String touple : touples) {
        final String[] values = coordinatePattern.split(touple);
        if (values.length > 0) {
          final double[] coordinates = MathUtil.toDoubleArray(values);
          numAxis = Math.max(numAxis, coordinates.length);
          listOfCoordinateArrays.add(coordinates);
        }
      }
    }

    return toCoordinateList(numAxis, listOfCoordinateArrays);
  }

  public static <V extends Coordinates> List<LineString> split(
    final LineString line,
    Collection<V> splitPoints,
    final double maxDistance) {
    splitPoints = new ArrayList<V>(splitPoints);
    final List<LineString> lines = new ArrayList<LineString>();
    final CoordinatesList points = CoordinatesListUtil.get(line);
    final Set<Integer> splitVertices = new TreeSet<Integer>();
    final Set<Integer> splitIndexes = new TreeSet<Integer>();

    for (final Iterator<V> iter = splitPoints.iterator(); iter.hasNext();) {
      final Coordinates node = iter.next();
      final double distance = points.distance(0, node);
      if (distance < maxDistance) {
        iter.remove();
      }
    }
    final Map<Coordinates, Double> pointDistanceMap = new HashMap<Coordinates, Double>();
    final Map<Coordinates, Integer> pointSegment = new HashMap<Coordinates, Integer>();

    for (int i = 1; i < points.size() && !splitPoints.isEmpty(); i++) {
      for (final Iterator<V> nodeIter = splitPoints.iterator(); nodeIter.hasNext();) {
        final Coordinates point = nodeIter.next();
        final double pointDistance = points.distance(i, point);
        if (pointDistance < maxDistance) {
          if (i < points.size() - 1) {
            splitVertices.add(i);
            splitIndexes.add(i);
          }
          pointDistanceMap.remove(point);
          pointSegment.remove(point);
          nodeIter.remove();
        } else {
          final int segmentIndex = i - 1;
          final double x = point.getX();
          final double y = point.getY();
          final double x1 = points.getX(segmentIndex);
          final double y1 = points.getY(segmentIndex);
          final double x2 = points.getX(i);
          final double y2 = points.getY(i);
          final double segmentDistance = LineSegmentUtil.distance(x1, y1, x2,
            y2, x, y);
          if (segmentDistance == 0) {
            pointDistanceMap.put(point, segmentDistance);
            pointSegment.put(point, segmentIndex);
            nodeIter.remove();
          } else {
            final double projectionFactor = LineSegmentUtil.projectionFactor(
              x1, y1, x2, y2, x, y);
            if (projectionFactor >= 0.0 && projectionFactor <= 1.0) {
              final Double closestDistance = pointDistanceMap.get(point);
              if (closestDistance == null) {
                pointSegment.put(point, segmentIndex);
                pointDistanceMap.put(point, segmentDistance);
              } else if (closestDistance.compareTo(segmentDistance) > 0) {
                pointSegment.put(point, segmentIndex);
                pointDistanceMap.put(point, segmentDistance);
              }
            }
          }
        }
      }
    }
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
    final Map<Integer, Set<Coordinates>> segmentSplitPoints = new TreeMap<Integer, Set<Coordinates>>();
    for (final Entry<Coordinates, Integer> entry : pointSegment.entrySet()) {
      final Coordinates splitPoint = entry.getKey();
      final Integer index = entry.getValue();
      Set<Coordinates> splitNodes = segmentSplitPoints.get(index);
      if (splitNodes == null) {
        final Coordinates point = points.get(index);
        splitNodes = new TreeSet<Coordinates>(
          new CoordinatesDistanceComparator(point));
        segmentSplitPoints.put(index, splitNodes);
        splitIndexes.add(index);
      }
      splitNodes.add(splitPoint);
      splitPoints.remove(splitPoint);
    }
    if (splitPoints.isEmpty()) {
      int startIndex = 0;
      Coordinates startPoint = null;
      for (final Integer index : splitIndexes) {
        if (splitVertices.contains(index)) {
          final CoordinatesList newPoints = CoordinatesListUtil.subList(points,
            startPoint, startIndex, index - startIndex + 1, null);
          final LineString newLine = geometryFactory.createLineString(newPoints);
          lines.add(newLine);
          startPoint = null;
          startIndex = index;
        }
        final Set<Coordinates> splitNodes = segmentSplitPoints.get(index);
        if (splitNodes != null) {
          for (final Coordinates splitPoint : splitNodes) {
            Coordinates point = splitPoint;
            final double splitPointZ = splitPoint.getZ();
            if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
              if (splitPointZ == 0 || Double.isNaN(splitPointZ)) {
                final Coordinates p1 = points.get(index);
                final Coordinates p2 = points.get(index + 1);
                final double z = LineSegmentUtil.getElevation(p1, p2, point);
                point = new DoubleCoordinates(point.getX(), point.getY(), z);
              }
            }

            final CoordinatesList newPoints;
            if (startIndex > index) {
              newPoints = CoordinatesListUtil.create(points.getNumAxis(),
                startPoint, point);
            } else {
              newPoints = CoordinatesListUtil.subList(points, startPoint,
                startIndex, index - startIndex + 1, point);
            }
            final LineString newLine = geometryFactory.createLineString(newPoints);
            lines.add(newLine);
            startPoint = point;
            startIndex = index + 1;
          }
        }
      }
      final CoordinatesList newPoints = CoordinatesListUtil.subList(points,
        startPoint, startIndex);
      final LineString newLine = geometryFactory.createLineString(newPoints);
      lines.add(newLine);

      return lines;
    } else {
      return Collections.singletonList(line);
    }
  }

  public static CoordinatesList subList(
    final CoordinatesList points,
    final Coordinates startPoint,
    final int start) {
    final int dimension = points.getNumAxis();
    final int length = points.size() - start;
    int size = length;
    int startIndex = 0;
    boolean startEqual = false;
    if (startPoint != null) {
      startEqual = startPoint.equals2d(points.get(start));
      if (!startEqual) {
        size++;
        startIndex++;
      }
    }

    final CoordinatesList newPoints = new DoubleCoordinatesList(size, dimension);

    if (!startEqual && startPoint != null) {
      newPoints.setPoint(0, startPoint);
    }

    points.copy(start, newPoints, startIndex, dimension, length);

    return newPoints;
  }

  public static CoordinatesList subList(
    final CoordinatesList points,
    final Coordinates startPoint,
    final int start,
    final int length,
    final Coordinates endPoint) {
    final int dimension = points.getNumAxis();
    int size = length;
    int startIndex = 0;
    int lastIndex = length;
    boolean startEqual = false;
    boolean endEqual = false;
    if (startPoint != null) {
      startEqual = startPoint.equals2d(points.get(start));
      if (!startEqual) {
        size++;
        lastIndex++;
        startIndex++;
      }
    }
    if (endPoint != null) {
      endEqual = endPoint.equals2d(points.get(start + length - 1));
      if (!endEqual) {
        size++;
      }
    }
    final CoordinatesList newPoints = new DoubleCoordinatesList(size, dimension);

    if (!startEqual && startPoint != null) {
      newPoints.setPoint(0, startPoint);
    }

    points.copy(start, newPoints, startIndex, dimension, length);

    if (!endEqual && endPoint != null) {
      newPoints.setPoint(lastIndex, endPoint);
    }

    return newPoints;
  }

  public static CoordinatesList toCoordinateList(
    final int numAxis,
    final List<double[]> listOfCoordinateArrays) {
    final CoordinatesList points = new DoubleCoordinatesList(
      listOfCoordinateArrays.size(), numAxis);
    for (int i = 0; i < listOfCoordinateArrays.size(); i++) {
      final double[] coordinates = listOfCoordinateArrays.get(i);
      for (int j = 0; j < coordinates.length; j++) {
        final double value = coordinates[j];
        points.setValue(i, j, value);
      }
    }
    return points;
  }

  public static CoordinatesList trim(
    final CoordinatesList coordinates,
    final int length) {
    if (length == coordinates.size()) {
      return coordinates;
    } else {
      return coordinates.subList(0, length);
    }
  }

}
