package com.revolsys.geometry.graph.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.revolsys.geometry.graph.Edge;
import com.revolsys.geometry.graph.Graph;
import com.revolsys.geometry.graph.Node;
import com.revolsys.geometry.graph.comparator.EdgeAttributeValueComparator;
import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.graph.linestring.EdgeLessThanDistance;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Punctual;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;
import com.revolsys.geometry.model.util.BoundingBoxEditor;

public class GeometryGraph extends Graph<LineSegment> {

  private final BoundingBoxEditor boundingBox;

  private final List<Geometry> geometries = new ArrayList<>();

  private double maxDistance;

  private final List<Point> points = new ArrayList<>();

  private final List<Point> startPoints = new ArrayList<>();

  public GeometryGraph(final Geometry geometry) {
    this(geometry.getGeometryFactory());
    addGeometry(geometry);
  }

  public GeometryGraph(final GeometryFactory geometryFactory) {
    super(false);
    setGeometryFactory(geometryFactory);
    this.boundingBox = geometryFactory.bboxEditor();
    final double scaleXY = getGeometryFactory().getScaleXY();
    if (scaleXY > 0) {
      this.maxDistance = 1 / scaleXY;
    } else {
      this.maxDistance = 0;
    }
  }

  public void addEdge(final Node<LineSegment> fromNode, final Node<LineSegment> toNode) {
    final LineSegment lineSegment = new LineSegmentDoubleGF(fromNode, toNode);
    addEdge(lineSegment, fromNode, toNode);
  }

  private void addEdges(final LineString points, final Map<String, Object> attributes) {
    this.startPoints.add(points.getPoint(0).newPoint2D());
    int index = 0;
    for (LineSegment lineSegment : points.segments()) {
      lineSegment = (LineSegment)lineSegment.clone();
      final double fromX = lineSegment.getX(0);
      final double fromY = lineSegment.getY(0);
      final double toX = lineSegment.getX(1);
      final double toY = lineSegment.getY(1);
      final Edge<LineSegment> edge = addEdge(lineSegment, fromX, fromY, toX, toY);
      attributes.put("segmentIndex", index++);
      edge.setProperties(attributes);
    }
  }

  public void addGeometry(Geometry geometry) {
    geometry = getGeometryFactory().geometry(geometry);
    final Map<String, Object> properties = new LinkedHashMap<>();

    final int geometryIndex = this.geometries.size();
    properties.put("geometryIndex", geometryIndex);
    this.geometries.add(geometry);
    for (int partIndex = 0; partIndex < geometry.getGeometryCount(); partIndex++) {
      properties.put("partIndex", partIndex);
      final Geometry part = geometry.getGeometry(partIndex);
      if (part instanceof Point) {
        final Point point = (Point)part;
        this.points.add(point);
      } else if (part instanceof LineString) {
        final LineString line = (LineString)part;
        final LineString points = line;
        properties.put("type", "LineString");
        addEdges(points, properties);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        int ringIndex = 0;
        for (final LinearRing ring : polygon.rings()) {
          properties.put("ringIndex", ringIndex++);
          if (ringIndex == 0) {
            properties.put("type", "PolygonShell");
          } else {
            properties.put("type", "PolygonHole");
          }
          addEdges(ring, properties);
        }
        properties.remove("ringIndex");
      }
    }

    this.boundingBox.addBbox(geometry);
  }

  @Override
  protected LineSegment clone(final LineSegment segment, final LineString line) {
    return new LineSegmentDoubleGF(line);
  }

  /**
   * Get the intersection between the line and the boundary of this geometry.
   *
   * @param line
   * @return
   */
  public Geometry getBoundaryIntersection(final LineString line) {
    final List<Point> pointIntersections = new ArrayList<>();
    final List<LineString> lineIntersections = new ArrayList<>();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = getBoundingBox(line);
    if (boundingBox.bboxIntersects(this.boundingBox)) {
      final LineString points = line;
      final int vertexCount = points.getVertexCount();
      final Point fromPoint = points.getPoint(0);
      final Point toPoint = points.getPoint(vertexCount - 1);

      Point previousPoint = fromPoint;
      for (int vertexIndex = 1; vertexIndex < vertexCount; vertexIndex++) {
        final Point nextPoint = points.getPoint(vertexIndex);
        final LineSegment line1 = new LineSegmentDoubleGF(getGeometryFactory(), previousPoint,
          nextPoint);
        final List<Edge<LineSegment>> edges = EdgeLessThanDistance.getEdges(this, line1,
          this.maxDistance);
        for (final Edge<LineSegment> edge2 : edges) {
          final LineSegment line2 = edge2.getObject();
          final Geometry segmentIntersection = line1.getIntersection(line2);
          if (segmentIntersection instanceof Point) {
            final Point intersection = (Point)segmentIntersection;
            if (intersection.equals(fromPoint) || intersection.equals(toPoint)) {
              // Point intersection, make sure it's not at the start
              final Node<LineSegment> node = findNode(intersection);
              if (node == null) {
                pointIntersections.add(geometryFactory.point(intersection));
              } else {
                final int degree = node.getDegree();
                if (isStartPoint(node)) {
                  if (degree > 2) {
                    // Intersection not at the start/end of the other line,
                    // taking
                    // into account loops
                    pointIntersections.add(geometryFactory.point(intersection));
                  }
                } else if (degree > 1) {
                  // Intersection not at the start/end of the other line
                  pointIntersections.add(geometryFactory.point(intersection));
                }
              }
            } else {
              // Intersection not at the start/end of the line
              pointIntersections.add(geometryFactory.point(intersection));
            }
          } else if (segmentIntersection instanceof LineSegment) {
            lineIntersections.add((LineSegment)segmentIntersection);
          }
          for (final Point point : line1.vertices()) {
            if (line2.distancePoint(point) < this.maxDistance) {
              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                final double maxDistance1 = this.maxDistance;
                for (final Node<LineSegment> node : this.getNodes(point, maxDistance1)) {
                  final int degree = node.getDegree();
                  if (isStartPoint(node)) {
                    if (degree > 2) {
                      // Intersection not at the start/end of the other line,
                      // taking
                      // into account loops
                      pointIntersections.add(geometryFactory.point(point));
                    }
                  } else if (degree > 1) {
                    // Intersection not at the start/end of the other line
                    pointIntersections.add(geometryFactory.point(point));
                  }
                }
              } else {
                // Intersection not at the start/end of the line
                pointIntersections.add(geometryFactory.point(point));
              }
            }
          }

        }
        previousPoint = nextPoint;
      }
    }
    if (lineIntersections.isEmpty()) {
      return geometryFactory.punctual(pointIntersections);
    } else {
      final List<LineString> mergedLines = LineMerger.merge(lineIntersections);
      final Lineal multiLine = geometryFactory.lineal(mergedLines);
      if (pointIntersections.isEmpty()) {
        return multiLine;
      } else {
        final Punctual multiPoint = geometryFactory.punctual(pointIntersections);
        return multiPoint.union(multiLine);
      }
    }
  }

  public BoundingBox getBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return BoundingBox.empty();
    } else {
      BoundingBox boundingBox = geometry.getBoundingBox();
      boundingBox = boundingBox.expand(this.maxDistance);
      return boundingBox;
    }
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final LineSegment object = getEdgeObject(edgeId);
    if (object == null) {
      return null;
    } else {
      final LineString line = object;
      return line;
    }
  }

  /**
   * Only currently works for lines and points.
   *
   * @return
   */

  public Geometry getGeometry() {
    removeDuplicateLineEdges();
    final EdgeAttributeValueComparator<LineSegment> comparator = new EdgeAttributeValueComparator<>(
      "geometryIndex", "partIndex", "segmentIndex");
    final List<Geometry> geometries = new ArrayList<>(this.points);
    final GeometryFactory geometryFactory = getGeometryFactory();
    final List<Point> points = new ArrayList<>();
    final Consumer<Edge<LineSegment>> action = new Consumer<Edge<LineSegment>>() {
      private Node<LineSegment> previousNode = null;

      @Override
      public void accept(final Edge<LineSegment> edge) {
        final LineSegment lineSegment = edge.getObject();
        if (lineSegment.getLength() > 0) {
          final Node<LineSegment> fromNode = edge.getFromNode();
          final Node<LineSegment> toNode = edge.getToNode();
          if (this.previousNode == null) {
            points.add(lineSegment.getPoint(0));
            points.add(lineSegment.getPoint(1));
          } else if (fromNode == this.previousNode) {
            if (edge.getLength() > 0) {
              points.add(toNode);
            }
          } else {
            if (points.size() > 1) {
              final LineString line = geometryFactory.lineString(points);
              geometries.add(line);
            }
            points.clear();
            ;
            points.add(lineSegment.getPoint(0));
            points.add(lineSegment.getPoint(1));
          }
          if (points.size() > 1) {
            final int toDegree = toNode.getDegree();
            if (toDegree != 2) {
              final LineString line = geometryFactory.lineString(points);
              geometries.add(line);
              points.clear();
              ;
              points.add(toNode);
            }
          }
          this.previousNode = toNode;
        }
      }
    };
    forEachEdge(comparator, action);
    if (points.size() > 1) {
      final LineString line = geometryFactory.lineString(points);
      geometries.add(line);
    }
    return geometryFactory.geometry(geometries);
  }

  public boolean intersects(final LineString line) {
    BoundingBox boundingBox = line.getBoundingBox();
    final double scaleXY = getGeometryFactory().getScaleXY();
    double maxDistance = 0;
    if (scaleXY > 0) {
      maxDistance = 1 / scaleXY;
    }
    boundingBox = boundingBox.expand(maxDistance);
    if (boundingBox.bboxIntersects(this.boundingBox)) {
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
              if (isStartPoint(node)) {
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
            if (line2.distancePoint(point) < maxDistance) {

              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                final double maxDistance1 = maxDistance;
                for (final Node<LineSegment> node : this.getNodes(point, maxDistance1)) {
                  final int degree = node.getDegree();
                  if (isStartPoint(node)) {
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

  private boolean isLineString(final Edge<LineSegment> edge) {
    if ("LineString".equals(edge.getProperty("type"))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isStartPoint(final Point coordinates) {
    return this.startPoints.contains(coordinates);
  }

  public void removeDuplicateLineEdges() {
    final Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<>(
      "geometryIndex", "partIndex", "segmentIndex");
    forEachEdge(comparator, (edge) -> {
      if (isLineString(edge)) {
        final Node<LineSegment> fromNode = edge.getFromNode();
        final Node<LineSegment> toNode = edge.getToNode();

        final Collection<Edge<LineSegment>> edges = fromNode.getEdgesTo(toNode);
        final int duplicateCount = edges.size();
        if (duplicateCount > 1) {
          edges.remove(edge);
          for (final Edge<LineSegment> removeEdge : edges) {
            if (isLineString(removeEdge)) {
              removeEdge.remove();
            }
          }
        }
      }
    });
  }

}
