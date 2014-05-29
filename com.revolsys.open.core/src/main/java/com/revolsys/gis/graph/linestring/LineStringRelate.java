package com.revolsys.gis.graph.linestring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.segment.LineSegment;

public class LineStringRelate {
  private final LineString line1;

  private final LineString line2;

  private final LineStringGraph graph1;

  private final Point fromPoint1;

  private final Point fromPoint2;

  private final LineStringGraph graph2;

  private final Point toPoint1;

  private final Point toPoint2;

  public LineStringRelate(final LineString line1, final LineString line2) {
    this(line1, line2, 1);
  }

  public LineStringRelate(final LineString line1, final LineString line2,
    final double tolerance) {
    this.line1 = line1;
    this.line2 = line2;
    final GeometryFactory geometryFactory = line1.getGeometryFactory();
    graph1 = new LineStringGraph(geometryFactory, line1);
    graph2 = new LineStringGraph(geometryFactory, line2);

    final Map<Point, Point> movedNodes = new HashMap<Point, Point>();
    final InvokeMethodVisitor<Node<LineSegment>> moveNodesVisitor1 = new InvokeMethodVisitor<Node<LineSegment>>(
      graph2, "movePointsWithinTolerance", movedNodes, tolerance);
    graph1.visitNodes(moveNodesVisitor1);
    final InvokeMethodVisitor<Node<LineSegment>> moveNodesVisitor2 = new InvokeMethodVisitor<Node<LineSegment>>(
      graph1, "movePointsWithinTolerance", movedNodes, tolerance);
    graph2.visitNodes(moveNodesVisitor2);

    final int i = 0;
    fromPoint1 = getMovedCoordinate(movedNodes, line1, i);
    fromPoint2 = getMovedCoordinate(movedNodes, line2, i);
    toPoint1 = getMovedCoordinate(movedNodes, line1, line1.getVertexCount() - 1);
    toPoint2 = getMovedCoordinate(movedNodes, line2, line2.getVertexCount() - 1);
  }

  public Graph<LineSegment> getGraph1() {
    return graph1;
  }

  public Graph<LineSegment> getGraph2() {
    return graph2;
  }

  public LineString getLine1() {
    return line1;
  }

  public LineString getLine2() {
    return line2;
  }

  public Point getMovedCoordinate(final Map<Point, Point> movedNodes,
    final LineString line, final int i) {
    final Point coordinates = line.getVertex(i);
    if (movedNodes.containsKey(coordinates)) {
      return movedNodes.get(coordinates);
    } else {
      return coordinates;
    }
  }

  public MultiLineString getOverlap() {
    final List<List<Point>> intersections = new ArrayList<>();
    final PointList points1 = line1;
    final List<Point> currentCoordinates = new ArrayList<>();
    Node<LineSegment> previousNode = graph1.getNode(fromPoint1);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else if (outEdges.size() > 1) {
        System.err.println("Cannot handle overlaps\n" + getLine1() + "\n "
          + getLine2());
        final GeometryFactory factory = line1.getGeometryFactory();
        return factory.multiLineString();
      } else {
        final Edge<LineSegment> edge = outEdges.get(0);
        final LineSegment line = edge.getObject();
        final Node<LineSegment> nextNode = edge.getToNode();
        if (graph2.hasEdgeBetween(previousNode, nextNode)) {
          if (currentCoordinates.size() == 0) {
            currentCoordinates.add(line.getPoint(0));
          }
          currentCoordinates.add(line.getPoint(1));
        } else {
          if (currentCoordinates.size() > 0) {
            final List<Point> points = new ArrayList<>();
            intersections.add(points);
            currentCoordinates.clear();
          }
        }
        previousNode = nextNode;
      }

    } while (previousNode != null && !previousNode.equals(2,fromPoint1));
    if (currentCoordinates.size() > 0) {
      final List<Point> points = new ArrayList<>();
      intersections.add(points);
    }
    final GeometryFactory factory = line1.getGeometryFactory();
    return factory.multiLineString(intersections);
  }

  public LineString getRelateLine1() {
    return graph1.getLine();
  }

  public LineString getRelateLine2() {
    return graph2.getLine();
  }

  public boolean isContained() {
    return isContains(graph2, graph1);
  }

  public boolean isContains() {
    return isContains(graph1, graph2);
  }

  private boolean isContains(final Graph<LineSegment> graph1,
    final Graph<LineSegment> graph2) {
    for (final Edge<LineSegment> edge : graph2.getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (!graph1.hasEdgeBetween(fromNode, toNode)) {
        return false;
      }
    }
    return true;
  }

  public boolean isEndOverlaps(final double maxDistance) {
    if (isOverlaps()) {
      boolean overlaps = false;
      final boolean from1Within = isWithin2(fromPoint1, maxDistance);
      final boolean to1Within = isWithin2(toPoint1, 1);
      if (from1Within != to1Within) {
        final boolean from2Within = isWithin1(fromPoint2, 1);
        final boolean to2Within = isWithin1(toPoint2, 1);
        if (from2Within != to2Within) {
          overlaps = true;
        }
      }
      if (overlaps) {
        final MultiLineString intersection = getOverlap();
        if (intersection.getGeometryCount() == 1) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isEqual() {
    if (graph1.getEdgeCount() == graph2.getEdgeCount()) {
      for (final Edge<LineSegment> edge : graph1.getEdges()) {
        if (!graph2.hasEdge(edge)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean isOverlaps() {
    final LineStringGraph g1 = graph1;
    final LineStringGraph g2 = graph2;
    if (g1.getEdgeCount() <= g2.getEdgeCount()) {
      return isOverlaps(g1, g2);
    } else {
      return isOverlaps(g2, g1);
    }
  }

  private boolean isOverlaps(final LineStringGraph graph1,
    final LineStringGraph graph2) {
    for (final Edge<LineSegment> edge : graph1.getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (graph2.hasEdgeBetween(fromNode, toNode)) {
        return true;
      }
    }
    return false;
  }

  private boolean isWithin(final LineStringGraph graph, final Point fromPoint,
    final Point toPoint, final Point point, final double maxDistance) {
    if (point.distance(fromPoint) < maxDistance) {
      return false;
    } else if (point.distance(toPoint) < maxDistance) {
      return false;
    } else {
      if (!graph.findNodes(point, maxDistance).isEmpty()) {
        return true;
      }
      final List<Edge<LineSegment>> edges = graph.findEdges(point, maxDistance);
      for (final Edge<LineSegment> edge : edges) {
        final LineSegment line = edge.getObject();
        if (line.intersects(point, maxDistance)) {
          return true;
        }
      }

    }
    return false;
  }

  public boolean isWithin1(final Point point, final double maxDistance) {
    return isWithin(graph1, fromPoint1, toPoint1, point, maxDistance);
  }

  public boolean isWithin2(final Point point, final double maxDistance) {
    return isWithin(graph2, fromPoint2, toPoint2, point, maxDistance);
  }

  public void splitEdgesCloseToNodes(final double maxDistance) {
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = graph1.getPointsOnEdges(
      graph2, maxDistance);
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge2 = graph2.getPointsOnEdges(
      graph1, maxDistance);
    graph1.splitEdges(pointsOnEdge1);
    graph2.splitEdges(pointsOnEdge2);
  }
}
