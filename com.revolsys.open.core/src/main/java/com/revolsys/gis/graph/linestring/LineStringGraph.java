package com.revolsys.gis.graph.linestring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.collection.Visitor;
import com.revolsys.filter.Filter;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.attribute.NodeAttributes;
import com.revolsys.gis.graph.comparator.EdgeAttributeValueComparator;
import com.revolsys.gis.graph.comparator.NodeDistanceComparator;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.graph.filter.NodeCoordinatesFilter;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.CoordinatesPrecisionModel;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.gis.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.gis.model.coordinates.filter.PointOnLineSegment;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleListCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.gis.model.geometry.filter.CrossingLineSegmentFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class LineStringGraph extends Graph<LineSegment> {
  private static final String INDEX = "INDEX";

  public static Edge<LineSegment> getFirstEdge(
    final Collection<Edge<LineSegment>> edges) {
    final Iterator<Edge<LineSegment>> iterator = edges.iterator();
    if (iterator.hasNext()) {
      Edge<LineSegment> edge = iterator.next();
      Integer index = edge.getAttribute(INDEX);
      while (iterator.hasNext()) {
        final Edge<LineSegment> edge2 = iterator.next();
        final Integer index2 = edge2.getAttribute(INDEX);
        if (index2 < index) {
          edge = edge2;
          index = index2;
        }
      }
      return edge;
    } else {
      return null;
    }
  }

  private GeometryFactory geometryFactory;

  private CoordinatesList points;

  private Coordinates fromPoint;

  private Coordinates toPoint;

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

  public LineString getLine() {
    final Set<Edge<LineSegment>> processedEdges = new HashSet<Edge<LineSegment>>();
    final DoubleListCoordinatesList newPoints = new DoubleListCoordinatesList(
      points.getNumAxis());
    Node<LineSegment> previousNode = findNode(fromPoint);
    newPoints.add(previousNode);
    do {
      final List<Edge<LineSegment>> outEdges = previousNode.getOutEdges();
      outEdges.removeAll(processedEdges);
      if (outEdges.isEmpty()) {
        previousNode = null;
      } else {
        final Edge<LineSegment> edge = getFirstEdge(outEdges);
        processedEdges.add(edge);
        final Node<LineSegment> nextNode = edge.getToNode();

        newPoints.add(nextNode);

        previousNode = nextNode;
      }
    } while (previousNode != null && !previousNode.equals2d(fromPoint));
    return geometryFactory.createLineString(newPoints);
  }

  public List<LineString> getLines() {
    removeDuplicateEdges();
    EdgeAttributeValueComparator<LineSegment> comparator = new EdgeAttributeValueComparator<LineSegment>(
      "INDEX");
    List<LineString> lines = new ArrayList<LineString>();
    int numAxis = geometryFactory.getNumAxis();
    DoubleListCoordinatesList points = new DoubleListCoordinatesList(numAxis);
    Node<LineSegment> previousNode = null;
    for (Edge<LineSegment> edge : getEdges(comparator)) {
      LineSegment lineSegment = edge.getObject();
      if (lineSegment.getLength() > 0) {
        Node<LineSegment> fromNode = edge.getFromNode();
        Node<LineSegment> toNode = edge.getToNode();
        if (previousNode == null) {
          points.addAll(lineSegment);
        } else if (fromNode == previousNode) {
          if (edge.getLength() > 0) {
            points.add(toNode);
          }
        } else {
          if (points.size() > 1) {
            LineString line = geometryFactory.createLineString(points);
            lines.add(line);
          }
          points = new DoubleListCoordinatesList(numAxis);
          points.addAll(lineSegment);
        }
        if (points.size() > 1) {
          int toDegree = toNode.getDegree();
          if (toDegree != 2) {
            LineString line = geometryFactory.createLineString(points);
            lines.add(line);
            points = new DoubleListCoordinatesList(numAxis);
            points.add(toNode);
          }
        }
        previousNode = toNode;
      }
    }
    if (points.size() > 1) {
      LineString line = geometryFactory.createLineString(points);
      lines.add(line);
    }
    return lines;
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
          final CoordinatesList intersections = lineSegment1.getIntersection(
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

  /**
   * Get the z-value for the point if it is at a node or on an edge.
   * 
   * @param point The point to get the z-value for.
   * @return The z-value or Double.NaN.
   */
  public double getZ(final Coordinates point) {
    final Node<LineSegment> node = findNode(point);
    if (node == null) {
      final double maxDistance = geometryFactory.getScaleXY() / 1000;
      for (final Edge<LineSegment> edge : findEdges(point, maxDistance)) {
        final LineSegment line = edge.getObject();
        final Coordinates lineStart = line.get(0);
        final Coordinates lineEnd = line.get(1);
        if (LineSegmentUtil.isPointOnLineMiddle(lineStart, lineEnd, point,
          maxDistance)) {
          final double elevation = LineSegmentUtil.getElevation(lineStart,
            lineEnd, point);
          return geometryFactory.makeZPrecise(elevation);
        }
      }
      return Double.NaN;
    } else {
      return node.getZ();
    }
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

  public boolean isSimple() {
    for (final Node<LineSegment> node : nodes()) {
      if (node.getDegree() > 2) {
        return false;
      }
    }

    for (final Edge<LineSegment> edge : getEdges()) {
      final LineSegment line = edge.getObject();
      final EdgeObjectFilter<LineSegment> filter = new EdgeObjectFilter<LineSegment>(
        new LineSegmentIntersectingFilter(line));
      final List<Edge<LineSegment>> edges = getEdges(filter, line.getEnvelope());
      for (final Edge<LineSegment> edge2 : edges) {
        final LineSegment line2 = edge2.getObject();
        final CoordinatesList intersections = line.getIntersection(line2);
        if (intersections.size() == 2) {
          return false;
        } else if (intersections.size() == 1) {
          if (edge.getCommonNodes(edge2).isEmpty()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public void nodeMoved(
    final Node<LineSegment> node,
    final Node<LineSegment> newNode) {
    if (fromPoint.equals2d(node)) {
      fromPoint = new DoubleCoordinates(newNode);
    }
  }

  private void removeDuplicateEdges() {
    final Visitor<Edge<LineSegment>> visitor = new InvokeMethodVisitor<Edge<LineSegment>>(
      this, "removeDuplicateEdges");
    Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<LineSegment>(
      INDEX);
    visitEdges(comparator, visitor);
  }

  /**
   * Remove duplicate edges, edges must be processed in order of the index
   * attribute.
   * 
   * @param edge1
   * @return
   */
  public boolean removeDuplicateEdges(final Edge<LineSegment> edge) {
    final Node<LineSegment> fromNode = edge.getFromNode();

    final Node<LineSegment> toNode = edge.getToNode();

    final Collection<Edge<LineSegment>> edges = fromNode.getEdgesTo(toNode);
    int numDuplicates = edges.size();
    if (numDuplicates > 1) {
      edges.remove(edge);
      Edge.remove(edges);
    }
    return true;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    setPrecisionModel(geometryFactory);
  }

  public void setIndex(
    final Edge<LineSegment> edge,
    final List<Edge<LineSegment>> splitEdges) {
    for (final Edge<LineSegment> splitEdge : splitEdges) {
      setIndex(edge, splitEdge);
    }
  }

  public void setIndex(
    final Edge<LineSegment> edge,
    final Edge<LineSegment> newEdge) {
    final Integer index = edge.getAttribute(INDEX);
    newEdge.setAttribute(INDEX, index);
  }

  private void setLineString(final LineString lineString) {
    final CoordinatesList points = CoordinatesListUtil.get(lineString);
    setPoints(points);
  }

  private void setPoints(final CoordinatesList points) {
    this.points = points;
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      geometryFactory, points);
    int index = 0;
    for (final LineSegment lineSegment : iterator) {
      final Edge<LineSegment> edge = add(lineSegment, lineSegment.getLine());
      edge.setAttribute(INDEX, index++);
    }
    fromPoint = new DoubleCoordinates(points.get(0));
    toPoint = new DoubleCoordinates(points.get(points.size() - 1));
  }

  public void splitCrossingEdges() {
    Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<LineSegment>(
      INDEX);
    final Visitor<Edge<LineSegment>> visitor = new InvokeMethodVisitor<Edge<LineSegment>>(
      this, "splitCrossingEdges");
    visitEdges(comparator, visitor);
  }

  public boolean splitCrossingEdges(final Edge<LineSegment> edge1) {
    final LineSegment line1 = edge1.getObject();
    final Filter<LineSegment> lineFilter = new CrossingLineSegmentFilter(line1);
    final Filter<Edge<LineSegment>> filter = new EdgeObjectFilter<LineSegment>(
      lineFilter);
    final List<Edge<LineSegment>> edges = getEdges(filter, line1.getEnvelope());

    if (!edges.isEmpty()) {
      final List<Coordinates> points = new ArrayList<Coordinates>();
      for (final Edge<LineSegment> edge2 : edges) {
        final LineSegment line2 = edge2.getObject();
        final CoordinatesList intersections = line1.getIntersection(line2);
        if (intersections.size() == 1) {
          final Coordinates intersection = intersections.get(0);
          points.add(intersection);
          final List<Edge<LineSegment>> splitEdges = edge2.split(intersection);
          setIndex(edge2, splitEdges);
        }
      }
      if (!points.isEmpty()) {
        final List<Edge<LineSegment>> splitEdges = edge1.split(points);
        setIndex(edge1, splitEdges);
      }
    }
    return true;
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
        setIndex(edge, newEdge);
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

  public void splitEdgesCloseToNodes() {
    double distance = 0;
    final double scaleXY = geometryFactory.getScaleXY();
    if (scaleXY > 0) {
      distance = 1 / scaleXY;
    }
    for (final Node<LineSegment> node : nodes()) {
      final List<Edge<LineSegment>> edges = findEdges(node, distance);
      edges.removeAll(node.getEdges());
      if (!edges.isEmpty()) {
        for (final Edge<LineSegment> edge : edges) {
          final LineSegment line = edge.getObject();
          if (line.isPointOnLineMiddle(node, distance)) {
            final List<Edge<LineSegment>> splitEdges = edge.split(node);
            setIndex(edge, splitEdges);
          }
        }
      }
    }
  }

}
