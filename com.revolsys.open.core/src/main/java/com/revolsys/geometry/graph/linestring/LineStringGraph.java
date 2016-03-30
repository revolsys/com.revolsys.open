package com.revolsys.geometry.graph.linestring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.revolsys.comparator.CollectionComparator;
import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.comparator.EdgeAttributeValueComparator;
import com.revolsys.geometry.graph.comparator.NodeDistanceComparator;
import com.revolsys.geometry.graph.filter.EdgeObjectFilter;
import com.revolsys.geometry.graph.filter.NodeCoordinatesFilter;
import com.revolsys.geometry.graph.visitor.NodeLessThanDistanceOfCoordinatesVisitor;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.comparator.CoordinatesDistanceComparator;
import com.revolsys.geometry.model.coordinates.filter.CrossingLineSegmentFilter;
import com.revolsys.geometry.model.coordinates.filter.PointOnLineSegment;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

public class LineStringGraph extends Graph<LineSegment> {
  private static final String INDEX = "INDEX";

  private static final CollectionComparator<Integer> INDEX_COMPARATOR = new CollectionComparator<Integer>();

  public static Edge<LineSegment> getFirstEdge(final Collection<Edge<LineSegment>> edges) {
    final Iterator<Edge<LineSegment>> iterator = edges.iterator();
    if (iterator.hasNext()) {
      Edge<LineSegment> edge = iterator.next();
      List<Integer> index = edge.getProperty(INDEX);
      while (iterator.hasNext()) {
        final Edge<LineSegment> edge2 = iterator.next();
        final List<Integer> index2 = edge2.getProperty(INDEX);
        if (INDEX_COMPARATOR.compare(index, index2) > 0) {
          edge = edge2;
          index = index2;
        }
      }
      return edge;
    } else {
      return null;
    }
  }

  private BoundingBox envelope;

  private Point fromPoint;

  private GeometryFactory geometryFactory;

  private LineString points;

  public LineStringGraph(final GeometryFactory geometryFactory, final LineString line) {
    super(false);
    setGeometryFactory(geometryFactory);
    setLineString(line);
  }

  public LineStringGraph(final LineString points) {
    super(false);
    setGeometryFactory(GeometryFactory.DEFAULT);
    setPoints(points);
  }

  @Override
  protected LineSegment clone(final LineSegment object, final LineString line) {
    return new LineSegmentDoubleGF(line);
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final LineSegment segment = getEdgeObject(edgeId);
    if (segment == null) {
      return null;
    } else {
      return segment;
    }
  }

  public LineString getLine() {
    final Set<Edge<LineSegment>> processedEdges = new HashSet<Edge<LineSegment>>();
    final List<Point> newPoints = new ArrayList<>();
    Node<LineSegment> previousNode = findNode(this.fromPoint);
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
    } while (previousNode != null && !previousNode.equals(2, this.fromPoint));
    return this.geometryFactory.lineString(newPoints);
  }

  public List<LineString> getLines() {
    removeDuplicateEdges();
    final EdgeAttributeValueComparator<LineSegment> comparator = new EdgeAttributeValueComparator<LineSegment>(
      "INDEX");
    final List<LineString> lines = new ArrayList<LineString>();
    final int axisCount = this.geometryFactory.getAxisCount();
    List<Point> points = new ArrayList<>();
    Node<LineSegment> previousNode = null;
    for (final Edge<LineSegment> edge : getEdges(comparator)) {
      final LineSegment lineSegment = edge.getObject();
      if (lineSegment.getLength() > 0) {
        final Node<LineSegment> fromNode = edge.getFromNode();
        final Node<LineSegment> toNode = edge.getToNode();
        if (previousNode == null) {
          points.add(lineSegment.getPoint(0));
          points.add(lineSegment.getPoint(1));
        } else if (fromNode == previousNode) {
          if (edge.getLength() > 0) {
            points.add(toNode);
          }
        } else {
          if (points.size() > 1) {
            final LineString line = this.geometryFactory.lineString(points);
            lines.add(line);
          }
          points = new ArrayList<>();
          points.add(lineSegment.getPoint(0));
          points.add(lineSegment.getPoint(1));
        }
        if (points.size() > 1) {
          final int toDegree = toNode.getDegree();
          if (toDegree != 2) {
            final LineString line = this.geometryFactory.lineString(points);
            lines.add(line);
            points = new ArrayList<>();
            points.add(toNode);
          }
        }
        previousNode = toNode;
      }
    }
    if (points.size() > 1) {
      final LineString line = this.geometryFactory.lineString(points);
      lines.add(line);
    }
    return lines;
  }

  public Map<Edge<LineSegment>, List<Node<LineSegment>>> getPointsOnEdges(
    final Graph<LineSegment> graph1, final double tolerance) {
    final Map<Edge<LineSegment>, List<Node<LineSegment>>> pointsOnEdge1 = new HashMap<Edge<LineSegment>, List<Node<LineSegment>>>();
    for (final Edge<LineSegment> edge : getEdges()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      final LineSegment lineSegment = edge.getObject();
      final PointOnLineSegment coordinatesFilter = new PointOnLineSegment(lineSegment, tolerance);
      final NodeCoordinatesFilter<LineSegment> nodeFilter = new NodeCoordinatesFilter<LineSegment>(
        coordinatesFilter);
      final NodeDistanceComparator<LineSegment> comparator = new NodeDistanceComparator<LineSegment>(
        fromNode);
      final List<Node<LineSegment>> nodes = graph1.getNodes(nodeFilter, comparator);
      for (final Iterator<Node<LineSegment>> iterator = nodes.iterator(); iterator.hasNext();) {
        final Node<LineSegment> node = iterator.next();
        if (node.equals(2, fromNode)) {
          iterator.remove();
        } else if (node.equals(2, toNode)) {
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
    final Set<Point> intersectionPoints = new HashSet<Point>();
    for (int i = 0; i < this.points.getVertexCount(); i++) {
      final Point point = this.points.getPoint(i);
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
          final Geometry intersections = ((LineSegment)lineSegment1.convertGeometry(getGeometryFactory()))
            .getIntersection(lineSegment2);
          for (final Point intersection : intersections.vertices()) {
            if (!lineSegment1.isEndPoint(intersection) && !lineSegment2.isEndPoint(intersection)) {
              intersectionPoints.add(intersection);
            }
          }
        }
      }
    }
    return this.geometryFactory.multiPoint(intersectionPoints);
  }

  /**
   * Get the z-value for the point if it is at a node or on an edge.
   *
   * @param point The point to get the z-value for.
   * @return The z-value or Double.NaN.
   */
  public double getZ(final Point point) {
    final Node<LineSegment> node = findNode(point);
    if (node == null) {
      final double maxDistance = this.geometryFactory.getScaleXY() / 1000;
      for (final Edge<LineSegment> edge : findEdges(point, maxDistance)) {
        final LineSegment segment = edge.getObject();
        if (segment.isPointOnLineMiddle(point, maxDistance)) {
          final double elevation = segment.getElevation(point);
          return this.geometryFactory.makeZPrecise(elevation);
        }
      }
      return Double.NaN;
    } else {
      return node.getZ();
    }
  }

  public boolean hasTouchingEdges(final Node<LineSegment> node) {
    final GeometryFactory precisionModel = getPrecisionModel();
    final List<Edge<LineSegment>> edges = findEdges(node, precisionModel.getScaleXY());
    for (final Edge<LineSegment> edge : edges) {
      final Point lineStart = edge.getFromNode();
      final Point lineEnd = edge.getToNode();
      if (LineSegmentUtil.isPointOnLineMiddle(precisionModel, lineStart, lineEnd, node)) {
        return true;
      }
    }
    return false;
  }

  public boolean intersects(final LineString line) {
    BoundingBox envelope = line.getBoundingBox();
    final double scaleXY = this.geometryFactory.getScaleXY();
    double maxDistance = 0;
    if (scaleXY > 0) {
      maxDistance = 1 / scaleXY;
    }
    envelope = envelope.expand(maxDistance);
    if (envelope.intersects(this.envelope)) {
      final LineString points = line;
      final int numPoints = points.getVertexCount();
      final Point fromPoint = points.getPoint(0);
      final Point toPoint = points.getPoint(numPoints - 1);

      Point previousPoint = fromPoint;
      for (int i = 1; i < numPoints; i++) {
        final Point nextPoint = points.getPoint(i);
        final LineSegment line1 = new LineSegmentDoubleGF(previousPoint, nextPoint);
        final List<Edge<LineSegment>> edges = EdgeLessThanDistance.getEdges(this, line1,
          maxDistance);
        for (final Edge<LineSegment> edge2 : edges) {
          final LineSegment line2 = edge2.getObject();
          final Geometry intersections = line1.getIntersection(line2);
          for (final Point intersection : intersections.vertices()) {
            if (intersection.equals(fromPoint) || intersection.equals(toPoint)) {
              // Point intersection, make sure it's not at the start
              final Node<LineSegment> node = findNode(intersection);
              final int degree = node.getDegree();
              if (node.equals(2, this.fromPoint)) {
                if (degree > 2) {
                  // Intersection not at the start/end of the other line, taking
                  // into account loops
                  return true;
                }
              } else if (degree > 1) {
                // Intersection not at the start/end of the other line
                return true;
              }
            } else {
              // Intersection not at the start/end of the line
              return true;
            }
          }
          for (final Point point : line1.vertices()) {
            if (line2.distance(point) < maxDistance) {

              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                for (final Node<LineSegment> node : NodeLessThanDistanceOfCoordinatesVisitor
                  .getNodes(this, point, maxDistance)) {
                  final int degree = node.getDegree();
                  if (node.equals(2, this.fromPoint)) {
                    if (degree > 2) {
                      // Intersection not at the start/end of the other line,
                      // taking
                      // into account loops
                      return true;
                    }
                  } else if (degree > 1) {
                    // Intersection not at the start/end of the other line
                    return true;
                  }
                }
              } else {
                // Intersection not at the start/end of the line
                return true;
              }
            }
          }

        }
        previousPoint = nextPoint;
      }
    }
    return false;
  }

  public boolean isSimple() {
    for (final Node<LineSegment> node : getNodes()) {
      if (node.getDegree() > 2) {
        return false;
      }
    }

    for (final Edge<LineSegment> edge : getEdges()) {
      final LineSegment line = edge.getObject();
      final EdgeObjectFilter<LineSegment> filter = new EdgeObjectFilter<LineSegment>(
        new LineSegmentIntersectingFilter(line));
      final List<Edge<LineSegment>> edges = getEdges(filter, line.getBoundingBox());
      for (final Edge<LineSegment> edge2 : edges) {
        final LineSegment line2 = edge2.getObject();
        final Geometry intersections = line.getIntersection(line2);
        if (intersections instanceof LineSegment) {
          return false;
        } else if (intersections instanceof Point) {
          if (edge.getCommonNodes(edge2).isEmpty()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public void nodeMoved(final Node<LineSegment> node, final Node<LineSegment> newNode) {
    if (this.fromPoint.equals(2, node)) {
      this.fromPoint = new PointDouble(newNode);
    }
  }

  private void removeDuplicateEdges() {
    final Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<>(INDEX);
    forEachEdge((edge) -> {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();

      final Collection<Edge<LineSegment>> edges = fromNode.getEdgesTo(toNode);
      final int duplicateCount = edges.size();
      if (duplicateCount > 1) {
        edges.remove(edge);
        Edge.remove(edges);
      }
    } , comparator);
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
    setPrecisionModel(geometryFactory);
  }

  private void setLineString(final LineString lineString) {
    final LineString points = lineString;
    setPoints(points);
  }

  private void setPoints(final LineString points) {
    this.points = points;
    int index = 0;
    for (final LineSegment lineSegment : points.segments()) {
      final Point from = lineSegment.getPoint(0);
      final Point to = lineSegment.getPoint(1);
      final Edge<LineSegment> edge = addEdge((LineSegment)lineSegment.clone(), from, to);

      edge.setProperty(INDEX, Arrays.asList(index++));
    }
    this.fromPoint = new PointDouble(points.getPoint(0));
    this.envelope = CoordinatesListUtil.getBoundingBox(this.geometryFactory, points);
  }

  public void splitCrossingEdges() {
    final Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<LineSegment>(
      INDEX);
    forEachEdge((edge) -> {
      final LineSegment line1 = edge.getObject();
      final Predicate<LineSegment> lineFilter = new CrossingLineSegmentFilter(line1);
      final Predicate<Edge<LineSegment>> filter = new EdgeObjectFilter<LineSegment>(lineFilter);
      final List<Edge<LineSegment>> edges = getEdges(filter, line1.getBoundingBox());

      if (!edges.isEmpty()) {
        final List<Point> points = new ArrayList<Point>();
        for (final Edge<LineSegment> edge2 : edges) {
          final LineSegment line2 = edge2.getObject();
          final Geometry intersections = line1.getIntersection(line2);
          if (intersections instanceof Point) {
            final Point intersection = new PointDouble((Point)intersections);
            points.add(intersection);
            edge2.split(intersection);
          }
        }
        if (!points.isEmpty()) {
          edge.splitEdge(points);
        }
      }
    } , comparator);
  }

  @Override
  public <V extends Point> List<Edge<LineSegment>> splitEdge(final Edge<LineSegment> edge,
    final Collection<V> nodes) {
    final List<Edge<LineSegment>> newEdges = new ArrayList<Edge<LineSegment>>();
    if (!edge.isRemoved()) {
      final Node<LineSegment> fromNode = edge.getFromNode();
      final Node<LineSegment> toNode = edge.getToNode();
      final CoordinatesDistanceComparator comparator = new CoordinatesDistanceComparator(fromNode);
      final Set<Point> newPoints = new TreeSet<Point>(comparator);
      for (final Point point : nodes) {
        newPoints.add(point);
      }
      newPoints.add(toNode);

      final List<Integer> index = edge.getProperty(INDEX);
      int i = 0;
      Point previousPoint = fromNode;
      for (final Point point : newPoints) {
        final LineSegment lineSegment = new LineSegmentDoubleGF(previousPoint, point);
        final Edge<LineSegment> newEdge = addEdge(lineSegment, previousPoint, point);
        final List<Integer> newIndecies = new ArrayList<Integer>(index);
        newIndecies.add(i++);
        newEdge.setProperty(INDEX, newIndecies);
        newEdges.add(newEdge);
        previousPoint = point;
      }

      remove(edge);
    }
    return newEdges;
  }

  public <V extends Point> void splitEdges(final Map<Edge<LineSegment>, List<V>> pointsOnEdge1) {
    for (final Entry<Edge<LineSegment>, List<V>> entry : pointsOnEdge1.entrySet()) {
      final Edge<LineSegment> edge = entry.getKey();
      final List<V> nodes = entry.getValue();
      splitEdge(edge, nodes);
    }
  }

  public void splitEdgesCloseToNodes() {
    double distance = 0;
    final double scaleXY = this.geometryFactory.getScaleXY();
    if (scaleXY > 0) {
      distance = 1 / scaleXY;
    }
    for (final Node<LineSegment> node : getNodes()) {
      final List<Edge<LineSegment>> edges = findEdges(node, distance);
      edges.removeAll(node.getEdges());
      if (!edges.isEmpty()) {
        for (final Edge<LineSegment> edge : edges) {
          final LineSegment line = edge.getObject();
          if (line.isPointOnLineMiddle(node, distance)) {
            edge.split(node);
          }
        }
      }
    }
  }

}
