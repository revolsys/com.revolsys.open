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
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.filter.PointOnLineSegment;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleListCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringGraph extends Graph<LineSegment> {
  private GeometryFactory geometryFactory;

  private CoordinatesList points;

  private Coordinates fromPoint;

  public LineStringGraph(final CoordinatesList points) {
    setGeometryFactory(GeometryFactory.getFactory());
    setPoints(points);
  }

  public LineStringGraph(final GeometryFactory geometryFactory,
    final LineString line) {
    setGeometryFactory(geometryFactory);
    setLineString(line);
  }

  public LineStringGraph(final LineString lineString) {
    setGeometryFactory(GeometryFactory.getFactory(lineString));
    setLineString(lineString);
  }

  @Override
  protected LineSegment clone(final LineSegment object, final LineString line) {
    return new LineSegment(line);
  }

  /**
   * Get the z-value for the point if it is at a node or on an edge.
   * 
   * @param point The point to get the z-value for.
   * @return The z-value or Double.NaN.
   */
  public double getZ(Coordinates point) {
    Node<LineSegment> node = findNode(point);
    if (node == null) {
      double maxDistance = geometryFactory.getScaleXY() / 1000;
      for (Edge<LineSegment> edge : findEdges(point, maxDistance)) {
        LineSegment line = edge.getObject();
        Coordinates lineStart = line.get(0);
        Coordinates lineEnd = line.get(1);
        if (LineSegmentUtil.isPointOnLineMiddle(lineStart, lineEnd, point,
          maxDistance)) {
          double elevation = LineSegmentUtil.getElevation(lineStart, lineEnd,
            point);
          return geometryFactory.makeZPrecise(elevation);
        }
      }
      return Double.NaN;
    } else {
      return node.getZ();
    }
  }

  public LineString getLine() {
    final DoubleListCoordinatesList newPoints = new DoubleListCoordinatesList(
      points.getNumAxis());
    Node<LineSegment> previousNode = getNode(fromPoint);
    newPoints.add(previousNode);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else if (outEdges.size() > 1) {
        System.err.println("Cannot handle overlaps " + points);
        return geometryFactory.createLineString(points);
      } else {
        final Edge<LineSegment> edge = outEdges.get(0);
        final LineSegment line = edge.getObject();
        final Node<LineSegment> nextNode = edge.getToNode();

        newPoints.add(line.get(1));

        previousNode = nextNode;
      }

    } while (previousNode != null && !previousNode.equals2d(fromPoint));
    return geometryFactory.createLineString(newPoints);
  }

  public Map<Edge<LineSegment>, List<Node<LineSegment>>> getPointsOnEdges(
    final Graph<LineSegment> graph1,
    final double tolerance) {
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = new HashMap<Edge<LineSegment>, List<Node<LineSegment>>>();
    for (final Edge<LineSegment> edge : getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      final LineSegment lineSegment = edge.getObject();
      final PointOnLineSegment coordinatesFilter = new PointOnLineSegment(
        lineSegment, tolerance);
      final NodeCoordinatesFilter<LineSegment> nodeFilter = new NodeCoordinatesFilter<LineSegment>(
        coordinatesFilter);
      final NodeDistanceComparator<LineSegment> comparator = new NodeDistanceComparator<LineSegment>(
        fromNode);
      final List<Node<LineSegment>> nodes = graph1.getNodes(nodeFilter,
        comparator);
      for (final Iterator<Node<LineSegment>> iterator = nodes.iterator(); iterator.hasNext();) {
        final Node<LineSegment> node = iterator.next();
        if (node.equals2d(fromNode)) {
          iterator.remove();
        } else if (node.equals2d(toNode)) {
          iterator.remove();
        }
      }
      if (!nodes.isEmpty()) {
        pointsOnEdge1.put(edge, nodes);
      }
    }
    return pointsOnEdge1;
  }

  public Geometry getSelfIntersections() {
    final CoordinatesPrecisionModel precisionModel = getPrecisionModel();
    final Set<Coordinates> intersectionPoints = new HashSet<Coordinates>();
    for (final Coordinates point : points) {
      final Node<LineSegment> node = getNode(point);
      if (node.getDegree() > 2 || hasTouchingEdges(node)) {
        intersectionPoints.add(point);
      }
    }
    for (final Edge<LineSegment> edge1 : getEdges()) {
      final LineSegment lineSegment1 = edge1.getObject();
      final List<Edge<LineSegment>> edges = getEdges(edge1);
      for (final Edge<LineSegment> edge2 : edges) {
        if (edge1 != edge2) {
          final LineSegment lineSegment2 = edge2.getObject();
          final List<Coordinates> intersections = lineSegment1.intersection(
            precisionModel, lineSegment2);
          for (final Coordinates intersection : intersections) {
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

  public boolean hasTouchingEdges(final Node<LineSegment> node) {
    final CoordinatesPrecisionModel precisionModel = getPrecisionModel();
    final List<Edge<LineSegment>> edges = findEdges(node,
      precisionModel.getScaleXY());
    for (final Edge<LineSegment> edge : edges) {
      final Coordinates lineStart = edge.getFromNode();
      final Coordinates lineEnd = edge.getToNode();
      if (LineSegmentUtil.isPointOnLineMiddle(precisionModel, lineStart,
        lineEnd, node)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void nodeMoved(
    final Node<LineSegment> node,
    final Node<LineSegment> newNode) {
    if (fromPoint.equals2d(node)) {
      fromPoint = new DoubleCoordinates(newNode);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    setPrecisionModel(geometryFactory);
  }

  private void setLineString(final LineString lineString) {
    final CoordinatesList points = CoordinatesListUtil.get(lineString);
    setPoints(points);
  }

  private void setPoints(final CoordinatesList points) {
    this.points = points;
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      geometryFactory, points);
    for (final LineSegment lineSegment : iterator) {
      add(lineSegment, lineSegment.getLine());
    }
    fromPoint = points.get(0);

  }

  @Override
  public <V extends Coordinates> List<Edge<LineSegment>> splitEdge(
    final Edge<LineSegment> edge,
    final Collection<V> nodes) {
    final List<Edge<LineSegment>> newEdges = new ArrayList<Edge<LineSegment>>();
    if (!edge.isRemoved()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      final CoordinatesDistanceComparator comparator = new CoordinatesDistanceComparator(
        fromNode);
      final Set<Coordinates> newPoints = new TreeSet<Coordinates>(comparator);
      for (final Coordinates point : nodes) {
        newPoints.add(point);
      }
      newPoints.add(toNode);

      Coordinates previousPoint = fromNode;
      for (final Coordinates point : newPoints) {
        final LineSegment lineSegment = new LineSegment(previousPoint, point);
        final Edge<LineSegment> newEdge = add(lineSegment,
          lineSegment.getLine());
        newEdges.add(newEdge);
        previousPoint = point;
      }

      remove(edge);
    }
    return newEdges;
  }

  public <V extends Coordinates> void splitEdges(
    final Map<Edge<LineSegment>, List<V>> pointsOnEdge1) {
    for (final Entry<Edge<LineSegment>, List<V>> entry : pointsOnEdge1.entrySet()) {
      final Edge<LineSegment> edge = entry.getKey();
      final List<V> nodes = entry.getValue();
      splitEdge(edge, nodes);
    }
  }

}
