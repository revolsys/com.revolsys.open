package com.revolsys.gis.graph.linestring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.comparator.NodeDistanceComparator;
import com.revolsys.gis.graph.filter.NodeCoordinatesFilter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.filter.PointOnLineSegment;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.util.NoOp;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringGraph extends Graph<LineSegment> {
  private GeometryFactory geometryFactory;

  private CoordinatesPrecisionModel precisionModel;

  private CoordinatesList points;

  public LineStringGraph(final LineString lineString) {
    this.points = CoordinatesListUtil.get(lineString);
    geometryFactory = GeometryFactory.getFactory(lineString);
    precisionModel = geometryFactory.getCoordinatesPrecisionModel();
    CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      geometryFactory, points);
    for (LineSegment lineSegment : iterator) {
      add(lineSegment, lineSegment.getLine());
    }
  }

  public LineStringGraph(final CoordinatesList points) {
    this.points = points;
    CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      points);
    for (LineSegment lineSegment : iterator) {
      add(lineSegment, lineSegment.getLine());
    }
  }

  public Geometry getSelfIntersections() {
    Set<Coordinates> intersectionPoints = new HashSet<Coordinates>();
    for (Coordinates point : points) {
      Node<LineSegment> node = getNode(point);
      if (node.getDegree() > 2 || hasTouchingEdges(node)) {
        intersectionPoints.add(point);
      }
    }
    for (Edge<LineSegment> edge1 : getEdges()) {
      LineSegment lineSegment1 = edge1.getObject();
      List<Edge<LineSegment>> edges = getEdges(edge1);
      for (Edge<LineSegment> edge2 : edges) {
        if (edge1 != edge2) {
          LineSegment lineSegment2 = edge2.getObject();
          List<Coordinates> intersections = lineSegment1.intersection(
            precisionModel, lineSegment2);
          for (Coordinates intersection : intersections) {
            if (!lineSegment1.contains(intersection)
              && !lineSegment2.contains(intersection)) {
              intersectionPoints.add(intersection);
            }
          }
        }
      }
    }
    return geometryFactory.createMultiPoint(intersectionPoints);
  }

  public boolean hasTouchingEdges(Node<LineSegment> node) {
    List<Edge<LineSegment>> edges = findEdges(node, precisionModel.getScaleXY());
    for (Edge<LineSegment> edge : edges) {
      Coordinates lineStart = edge.getFromNode();
      Coordinates lineEnd = edge.getToNode();
      if (LineSegmentUtil.isPointOnLineMiddle(precisionModel, lineStart,
        lineEnd, node)) {
        return true;
      }
    }
    return false;
  }

  public Map<Edge<LineSegment>, List<Node<LineSegment>>> getPointsOnEdges(
    LineStringGraph graph1, double tolerance) {
    Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = new HashMap<Edge<LineSegment>, List<Node<LineSegment>>>();
    for (Edge<LineSegment> edge : getEdges()) {
      Node<LineSegment> fromNode = edge.getFromNode();
      Node<LineSegment> toNode = edge.getToNode();
      LineSegment lineSegment = edge.getObject();
      final PointOnLineSegment coordinatesFilter = new PointOnLineSegment(
        lineSegment, tolerance);
      final NodeCoordinatesFilter<LineSegment> nodeFilter = new NodeCoordinatesFilter<LineSegment>(
        coordinatesFilter);
      final NodeDistanceComparator<LineSegment> comparator = new NodeDistanceComparator<LineSegment>(
        fromNode);
      List<Node<LineSegment>> nodes = graph1.getNodes(nodeFilter, comparator);
      for (Iterator<Node<LineSegment>> iterator = nodes.iterator(); iterator.hasNext();) {
        Node<LineSegment> node = iterator.next();
        if (node.equals2d(fromNode)) {
          iterator.remove();
        } else if (node.equals2d(toNode)) {
          iterator.remove();
        }
      }
      pointsOnEdge1.put(edge, nodes);
    }
    return pointsOnEdge1;
  }

  public void splitEdges(
    Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1) {
    for (Entry<Edge<LineSegment>, List<Node<LineSegment>>> entry : pointsOnEdge1.entrySet()) {
      Edge<LineSegment> edge = entry.getKey();
      List<Node<LineSegment>> nodes = entry.getValue();
      splitEdge(edge, nodes);
    }
  }

  public List<Edge<LineSegment>> splitEdge(final Edge<LineSegment> edge,
    final Collection<Node<LineSegment>> nodes) {
    final List<Edge<LineSegment>> newEdges = new ArrayList<Edge<LineSegment>>();
    if (!edge.isRemoved()) {
      Node<LineSegment> fromNode = edge.getFromNode();
      Node<LineSegment> toNode = edge.getToNode();
      final CoordinatesDistanceComparator comparator = new CoordinatesDistanceComparator(
        fromNode);
      Set<Coordinates> newPoints = new TreeSet<Coordinates>(comparator);
      for (Coordinates point : nodes) {
        newPoints.add(point);
      }
      newPoints.add(toNode);

      Coordinates previousPoint = fromNode;
      for (Coordinates point : newPoints) {
        LineSegment lineSegment = new LineSegment(previousPoint, point);
        Edge<LineSegment> newEdge = add(lineSegment, lineSegment.getLine());
        newEdges.add(newEdge);
        previousPoint = point;
      }

      remove(edge);
    }
    return newEdges;
  }

}
