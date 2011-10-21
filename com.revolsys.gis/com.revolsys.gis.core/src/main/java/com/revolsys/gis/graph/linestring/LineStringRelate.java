package com.revolsys.gis.graph.linestring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.coordinates.list.DoubleListCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.LineString;

public class LineStringRelate {
  private LineString line1;

  private LineString line2;

  private LineStringGraph graph1;

  private Coordinates fromCoordinates1;

  private Coordinates fromCoordinates2;

  private LineStringGraph graph2;

  private Coordinates toCoordinates1;

  private Coordinates toCoordinates2;

  public LineStringRelate(LineString line1, LineString line2) {
    this.line1 = line1;
    this.line2 = line2;
    GeometryFactory geometryFactory = GeometryFactory.getFactory(line1);
    graph1 = new LineStringGraph(geometryFactory, line1);
    graph2 = new LineStringGraph(geometryFactory, line2);

    final Map<Coordinates, Coordinates> movedNodes = new HashMap<Coordinates, Coordinates>();
    final InvokeMethodVisitor<Node<LineSegment>> moveNodesVisitor = new InvokeMethodVisitor<Node<LineSegment>>(
      CoordinatesListUtil.class, "movePointsWithinTolerance", movedNodes,
      graph2, 1);
    graph1.visitNodes(moveNodesVisitor);
    graph2.visitNodes(moveNodesVisitor);

    final int i = 0;
    fromCoordinates1 = getMovedCoordinate(movedNodes, line1, i);
    fromCoordinates2 = getMovedCoordinate(movedNodes, line2, i);
    toCoordinates1 = getMovedCoordinate(movedNodes, line1,
      line1.getNumPoints() - 1);
    toCoordinates2 = getMovedCoordinate(movedNodes, line2,
      line2.getNumPoints() - 1);
  }

  public Coordinates getMovedCoordinate(
    final Map<Coordinates, Coordinates> movedNodes, LineString line, final int i) {
    Coordinates coordinates = CoordinatesListUtil.get(line, i);
    if (movedNodes.containsKey(coordinates)) {
      return movedNodes.get(coordinates);
    } else {
      return coordinates;
    }
  }

  public void splitEdgesCloseToNodes(double maxDistance) {
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = graph1.getPointsOnEdges(
      graph2, maxDistance);
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge2 = graph2.getPointsOnEdges(
      graph1, maxDistance);
    graph1.splitEdges(pointsOnEdge1);
    graph2.splitEdges(pointsOnEdge2);
  }

  public List<CoordinatesList> getIntersection() {
    final List<CoordinatesList> intersections = new ArrayList<CoordinatesList>();
    final CoordinatesList points1 = CoordinatesListUtil.get(line1);
    final DoubleListCoordinatesList currentCoordinates = new DoubleListCoordinatesList(
      points1.getNumAxis());
    Node<LineSegment> previousNode = graph1.getNode(fromCoordinates1);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else if (outEdges.size() > 1) {
        throw new IllegalArgumentException("Cannot handle overlaps\n" + line1
          + "\n " + line2);
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

    } while (previousNode != null && !previousNode.equals2d(fromCoordinates1));
    if (currentCoordinates.size() > 0) {
      final CoordinatesList points = new DoubleCoordinatesList(
        currentCoordinates);
      intersections.add(points);
    }
    return intersections;
  }

  public boolean isEqual() {
    if (graph1.getEdgeCount() == graph2.getEdgeCount()) {
      for (Edge<LineSegment> edge : graph1.edges()) {
        if (!graph2.hasEdge(edge)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  public boolean isContained() {
    return isContains(graph2, graph1);
  }

  public boolean isContains() {
    return isContains(graph1, graph2);
  }

  private boolean isContains(Graph<LineSegment> graph1,
    Graph<LineSegment> graph2) {
    for (final Edge<LineSegment> edge : graph2.edges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      if (!graph1.hasEdgeBetween(fromNode, toNode)) {
        return false;
      }
    }
    return true;
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
}
