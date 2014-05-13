package com.revolsys.gis.model.coordinates.list;

import java.util.ArrayList;
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

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.linestring.LineStringGraph;
import com.revolsys.gis.model.coordinates.CoordinatesListCoordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.jts.algorithm.RobustDeterminant;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.util.MathUtil;

public class CoordinatesListUtil {
  public static final String COORDINATE_DISTANCE = "coordinateDistance";

  public static final String COORDINATE_INDEX = "coordinateIndex";

  public static final String SEGMENT_DISTANCE = "segmentDistance";

  public static final String SEGMENT_INDEX = "segmentIndex";

  public static double angle(final CoordinatesList points, final int i1,
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

  public static int append(final int axisCount, final LineString source,
    final int sourceIndex, final double[] targetCoordinates,
    final int targetIndex, final int vertexCount) {
    int coordIndex = targetIndex;
    double previousX;
    double previousY;
    if (targetIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = targetCoordinates[(targetIndex - 1) * axisCount];
      previousY = targetCoordinates[(targetIndex - 1) * axisCount + 1];
    }
    for (int i = 0; i < vertexCount; i++) {
      final double x = source.getX(sourceIndex + i);
      final double y = source.getY(sourceIndex + i);
      if (x != previousX || y != previousY) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double coordinate = source.getCoordinate(i, axisIndex);
          targetCoordinates[coordIndex * axisCount + axisIndex] = coordinate;
        }
        coordIndex++;
      }
      previousX = x;
      previousY = y;
    }
    return coordIndex;
  }

  public static int appendReverse(final int axisCount, final LineString source,
    final int sourceStartIndex, final double[] targetCoordinates,
    final int targetStartIndex, final int vertexCount) {
    int coordIndex = targetStartIndex;
    final int sourceVertexCount = source.getVertexCount();
    double previousX;
    double previousY;
    if (targetStartIndex == 0) {
      previousX = Double.NaN;
      previousY = Double.NaN;
    } else {
      previousX = targetCoordinates[(targetStartIndex - 1) * axisCount];
      previousY = targetCoordinates[(targetStartIndex - 1) * axisCount + 1];
    }
    for (int i = 0; i < vertexCount; i++) {
      final int sourceIndex = sourceVertexCount - (sourceStartIndex + i);
      final double x = source.getX(sourceIndex);
      final double y = source.getY(sourceIndex);
      if (x != previousX || y != previousY) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double coordinate = source.getCoordinate(i, axisIndex);
          targetCoordinates[coordIndex * axisCount + axisIndex] = coordinate;
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
  public static boolean containsWithinTolerance(final CoordinatesList points1,
    final CoordinatesList points2, final double tolerance) {

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
    for (final Edge<LineSegment> edge : graph2.getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (!graph1.hasEdgeBetween(fromNode, toNode)) {
        return false;
      }
    }
    return true;
  }

  public static boolean equals2dCoordinate(final CoordinatesList coordinates,
    final int index, final double x, final double y) {
    return coordinates.getX(index) == x && coordinates.getY(index) == y;
  }

  public static boolean equals2dCoordinates(final CoordinatesList coordinates,
    final int index1, final int index2) {
    return coordinates.getX(index1) == coordinates.getValue(index2, 0)
      && coordinates.getY(index1) == coordinates.getValue(index2, 1);
  }

  public static boolean equalWithinTolerance(final CoordinatesList points1,
    final CoordinatesList points2, final double tolerance) {
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
    final CoordinatesList points, final Coordinates point) {
    final Map<String, Number> result = new HashMap<String, Number>();
    result.put(SEGMENT_INDEX, -1);
    result.put(COORDINATE_INDEX, -1);
    result.put(COORDINATE_DISTANCE, Double.MAX_VALUE);
    result.put(SEGMENT_DISTANCE, Double.MAX_VALUE);
    double closestSegmentDistance = Double.MAX_VALUE;
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      points);
    if (iterator.hasNext()) {
      LineSegment segment = iterator.next();
      double closestCoordinateDistance = segment.get(0).distance(point);
      result.put(COORDINATE_INDEX, 0);
      result.put(COORDINATE_DISTANCE, closestCoordinateDistance);
      if (closestCoordinateDistance == 0) {
        result.put(SEGMENT_INDEX, 0);
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
          } else if (currentCoordinateDistance < closestCoordinateDistance) {
            result.put(COORDINATE_INDEX, i);
            result.put(COORDINATE_DISTANCE, currentCoordinateDistance);
            closestCoordinateDistance = currentCoordinateDistance;
          }
          final double segmentCoordinateDistance = segment.distance(point);
          if (segmentCoordinateDistance == 0) {
            result.put(SEGMENT_INDEX, i - 1);
            result.put(SEGMENT_DISTANCE, 0.0);
            return result;
          } else if (segmentCoordinateDistance <= closestSegmentDistance) {
            result.put(SEGMENT_DISTANCE, segmentCoordinateDistance);
            result.put(SEGMENT_INDEX, i - 1);
            closestSegmentDistance = segmentCoordinateDistance;
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

  public static CoordinatesList get(final Geometry geometry) {
    if (geometry == null) {
      return null;
    } else if (geometry instanceof Point) {
      return ((Point)geometry).getCoordinatesList();
    } else if (geometry instanceof LineString) {
      return ((LineString)geometry).getCoordinatesList();
    } else if (geometry instanceof Polygon) {
      final Polygon polygon = (Polygon)geometry;
      return get(polygon);
    } else if (geometry instanceof MultiPoint) {
      final MultiPoint multiPoint = (MultiPoint)geometry;
      return get(multiPoint);
    } else if (geometry.getGeometryCount() > 0) {
      return get(geometry.getGeometry(0));
    } else {
      return null;
    }
  }

  public static CoordinatesList get(final LineString line) {
    if (line == null) {
      return null;
    } else {
      return line.getCoordinatesList();
    }
  }

  public static Coordinates get(final LineString line, final int i) {
    final CoordinatesList points = get(line);
    return points.get(i);
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
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry subGeometry = geometry.getGeometry(i);
        if (subGeometry instanceof Point) {
          pointsList.add(((Point)subGeometry).getCoordinatesList());
        } else if (subGeometry instanceof LineString) {
          pointsList.add(get((LineString)subGeometry));
        } else if (subGeometry instanceof Polygon) {
          final Polygon polygon = (Polygon)subGeometry;
          final LineString exteriorRing = polygon.getExteriorRing();
          pointsList.add(get(exteriorRing));
          for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
            final LineString ring = polygon.getInteriorRing(j);
            pointsList.add(get(ring));
          }
        }
      }
    }
    return pointsList;
  }

  public static BoundingBox getBoundingBox(
    final GeometryFactory geometryFactory, final CoordinatesList points) {
    BoundingBox boundingBox = new Envelope(geometryFactory);
    for (final Coordinates point : points) {
      boundingBox = boundingBox.expand(point);
    }
    return boundingBox;
  }

  public static Coordinates[] getCoordinateArray(final Geometry geometry) {
    return getCoordinates(geometry, geometry.getVertexCount());
  }

  public static Coordinates[] getCoordinates(final Geometry g,
    final int vertexCount) {
    final List<Coordinates> coordinates = new ArrayList<>();
    final int i = 0;
    for (final Vertex vertex : g.vertices()) {
      if (i > vertexCount) {
        break;
      }
      coordinates.add(vertex.cloneCoordinates());
    }
    return coordinates.toArray(new Coordinates[coordinates.size()]);
  }

  private static Set<Coordinates> getCoordinatesSet2d(
    final CoordinatesList points) {
    final Set<Coordinates> pointSet = new TreeSet<Coordinates>();
    for (final Coordinates point : new InPlaceIterator(points)) {
      pointSet.add(new DoubleCoordinates(point, 2));
    }
    return pointSet;
  }

  public static List<List<CoordinatesList>> getParts(final Geometry geometry,
    final boolean clockwise) {
    final List<List<CoordinatesList>> partsList = new ArrayList<List<CoordinatesList>>();
    if (geometry != null) {
      for (int i = 0; i < geometry.getGeometryCount(); i++) {
        final Geometry part = geometry.getGeometry(i);
        if (!part.isEmpty()) {
          final List<CoordinatesList> pointsList = getAll(part);
          if (part instanceof Polygon) {
            if (pointsList.size() > 0 && !part.isEmpty()) {
              boolean partClockwise = clockwise;
              for (int j = 0; j < pointsList.size(); j++) {
                CoordinatesList points = pointsList.get(j);
                final boolean ringClockwise = !points.isCounterClockwise();
                if (ringClockwise != partClockwise) {
                  points = points.reverse();
                }
                pointsList.set(j, points);
                if (j == 0) {
                  partClockwise = !clockwise;
                }
              }
            }
          }
          partsList.add(pointsList);
        }
      }
    }
    return partsList;
  }

  public static List<CoordinatesList> intersection(
    final GeometryFactory geometryFactory, final CoordinatesList points1,
    final CoordinatesList points2, final double maxDistance) {

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
    final List<Coordinates> currentCoordinates = new ArrayList<>();
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
              points1.getAxisCount(), currentCoordinates);
            intersections.add(points);
            currentCoordinates.clear();
          }
        }
        previousNode = nextNode;
      }

    } while (previousNode != null && !endPoint.equals2d(startPoint));
    if (currentCoordinates.size() > 0) {
      final CoordinatesList points = new DoubleCoordinatesList(
        points1.getAxisCount(), currentCoordinates);
      intersections.add(points);
    }
    return intersections;
  }

  public static boolean isPointOnLine(final Coordinates coordinate,
    final CoordinatesList points, final double tolerance) {
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

  public static boolean isPointOnLine(final GeometryFactory precisionModel,
    final CoordinatesList points, final Coordinates point) {
    final CoordinatesListCoordinates lineStart = new CoordinatesListCoordinates(
      points);
    if (point.equals2d(lineStart)) {
      return true;
    }
    final CoordinatesListCoordinates lineEnd = new CoordinatesListCoordinates(
      points);
    for (int i = 1; i < points.size(); i++) {
      lineStart.setIndex(i - 1);
      lineEnd.setIndex(i);
      if (point.equals2d(lineEnd)) {
        return true;
      }
      if (LineSegmentUtil.isPointOnLine(precisionModel, lineStart, lineEnd,
        point)) {
        return true;
      }
    }

    return false;
  }

  public static boolean isWithinDistanceOfPoints(final Coordinates point,
    final CoordinatesList points, final double maxDistance) {
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

  /**
   * Only move the node if there is one of them
   * 
   * @param graph2
   * @param maxDistance
   * @param node1
   * @return
   */
  public static <T> boolean movePointsWithinTolerance(
    final Map<Coordinates, Coordinates> movedNodes, final Graph<T> graph2,
    final double maxDistance, final Node<T> node1) {
    final Graph<T> graph1 = node1.getGraph();
    final List<Node<T>> nodes2 = graph2.findNodes(node1, maxDistance);
    if (nodes2.size() == 1) {
      final Node<T> node2 = nodes2.get(0);
      if (graph1.findNode(node2) == null) {
        final GeometryFactory precisionModel = graph1.getPrecisionModel();
        final Coordinates midPoint = LineSegmentUtil.midPoint(precisionModel,
          node1, node2);
        if (!node1.equals2d(midPoint)) {
          if (movedNodes != null) {
            movedNodes.put(node1.cloneCoordinates(), midPoint);
          }
          node1.move(midPoint);
        }
        if (!node2.equals2d(midPoint)) {
          if (movedNodes != null) {
            movedNodes.put(node2.cloneCoordinates(), midPoint);
          }
          node2.move(midPoint);
        }
      }
    }
    return true;
  }

  public static int orientationIndex(final CoordinatesList ring,
    final int index1, final int index2, final int index) {
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
  public static int orientationIndex(final double x1, final double y1,
    final double x2, final double y2, final double x, final double y) {
    // travelling along p1->p2, turn counter clockwise to get to q return 1,
    // travelling along p1->p2, turn clockwise to get to q return -1,
    // p1, p2 and q are colinear return 0.
    final double dx1 = x2 - x1;
    final double dy1 = y2 - y1;
    final double dx2 = x - x2;
    final double dy2 = y - y2;
    return RobustDeterminant.signOfDet2x2(dx1, dy1, dx2, dy2);
  }

  public static CoordinatesList removeRepeatedPoints(
    final CoordinatesList points) {
    final int axisCount = points.getAxisCount();
    final List<Double> coordinates = new ArrayList<Double>();
    double x = points.getX(0);
    double y = points.getY(0);
    coordinates.add(x);
    coordinates.add(y);
    for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
      coordinates.add(points.getValue(0, axisIndex));
    }
    for (int i = 0; i < points.size(); i++) {
      final double x1 = points.getX(i);
      final double y1 = points.getY(i);
      if (x != x1 || y != y1) {
        coordinates.add(x1);
        coordinates.add(y1);
        for (int axisIndex = 2; axisIndex < axisCount; axisIndex++) {
          coordinates.add(points.getValue(i, axisIndex));
        }
        x = x1;
        y = y1;
      }
    }
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  public static void setCoordinates(final double[] coordinates,
    final int axisCount, final int i, final Coordinates point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = point.getValue(axisIndex);
      coordinates[i * axisCount + axisIndex] = value;
    }
  }

  public static void setCoordinates(final double[] coordinates,
    final int axisCount, final int i, final CoordinatesList points, final int j) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = points.getValue(j, axisIndex);
      coordinates[i * axisCount + axisIndex] = value;
    }
  }

  public static void setCoordinates(final double[] coordinates,
    final int axisCount, final int offset, final CoordinatesList points,
    final int vertexIndex, final int vertexCount) {
    for (int i = 0; i < vertexCount; i++) {
      setCoordinates(coordinates, axisCount, i, points, vertexIndex + i);
    }
  }

  public static void setCoordinates(final double[] coordinates,
    final int axisCount, final int i, final double... point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value;
      if (axisIndex < point.length) {
        value = point[axisIndex];
      } else {
        value = Double.NaN;
      }
      coordinates[i * axisCount + axisIndex] = value;
    }
  }

  public static void setCoordinates(final GeometryFactory geometryFactory,
    final double[] coordinates, final int axisCount, final int vertexIndex,
    final Coordinates point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      double value = point.getValue(axisIndex);
      value = geometryFactory.makePrecise(axisIndex, value);
      coordinates[vertexIndex * axisCount + axisIndex] = value;
    }
  }

  public static double signedArea(final CoordinatesList ring) {
    final int n = ring.size();
    if (n < 3) {
      return 0.0;
    } else {
      double sum = 0.0;
      double bx = ring.getX(0);
      double by = ring.getY(0);
      for (int i = 1; i < n; i++) {
        final double cx = ring.getX(i);
        final double cy = ring.getY(i);
        sum += (bx + cx) * (cy - by);
        bx = cx;
        by = cy;
      }
      return -sum / 2.0;
    }
  }

  public static <V extends Coordinates> List<LineString> split(
    final LineString line, Collection<V> splitPoints, final double maxDistance) {
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
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(line);
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
          final LineString newLine = geometryFactory.lineString(newPoints);
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
              final Coordinates[] coordinateArray = {
                startPoint, point
              };
              newPoints = new DoubleCoordinatesList(points.getAxisCount(),
                coordinateArray);
            } else {
              newPoints = CoordinatesListUtil.subList(points, startPoint,
                startIndex, index - startIndex + 1, point);
            }
            final LineString newLine = geometryFactory.lineString(newPoints);
            lines.add(newLine);
            startPoint = point;
            startIndex = index + 1;
          }
        }
      }
      final CoordinatesList newPoints = CoordinatesListUtil.subList(points,
        startPoint, startIndex);
      final LineString newLine = geometryFactory.lineString(newPoints);
      lines.add(newLine);

      return lines;
    } else {
      return Collections.singletonList(line);
    }
  }

  public static CoordinatesList subList(final CoordinatesList points,
    final Coordinates startPoint, final int start) {
    final int axisCount = points.getAxisCount();
    final int length = points.size() - start;
    int vertexCount = length;
    int startIndex = 0;
    boolean startEqual = false;
    if (startPoint != null) {
      startEqual = startPoint.equals2d(points.get(start));
      if (!startEqual) {
        vertexCount++;
        startIndex++;
      }
    }

    final double[] coordinates = new double[vertexCount * axisCount];

    if (!startEqual && startPoint != null) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, startPoint);
    }
    for (int i = start; i < start + length; i++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, startIndex++,
        points.get(i));
    }

    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  public static CoordinatesList subList(final CoordinatesList points,
    final Coordinates startPoint, final int start, final int length,
    final Coordinates endPoint) {
    final int axisCount = points.getAxisCount();
    int vertexCount = length;
    int startIndex = 0;
    int lastIndex = length;
    boolean startEqual = false;
    boolean endEqual = false;
    if (startPoint != null) {
      final Coordinates p1 = points.get(start);
      startEqual = startPoint.equals2d(p1);
      if (!startEqual) {
        vertexCount++;
        lastIndex++;
        startIndex++;
      }
    }
    if (endPoint != null) {
      final Coordinates pointsEnd = points.get(start + length - 1);
      endEqual = endPoint.equals2d(pointsEnd);
      if (!endEqual) {
        vertexCount++;
      }
    }
    final double[] coordinates = new double[vertexCount * axisCount];

    if (!startEqual && startPoint != null) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, startPoint);
    }

    for (int i = start; i < start + length; i++) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, startIndex++,
        points.get(i));
    }

    if (!endEqual && endPoint != null) {
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, lastIndex,
        endPoint);
    }

    return new DoubleCoordinatesList(axisCount, coordinates);
  }

}
