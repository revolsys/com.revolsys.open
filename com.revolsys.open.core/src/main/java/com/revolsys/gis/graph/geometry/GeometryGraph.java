package com.revolsys.gis.graph.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.collection.Visitor;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.comparator.EdgeAttributeValueComparator;
import com.revolsys.gis.graph.linestring.EdgeLessThanDistance;
import com.revolsys.gis.graph.visitor.NodeLessThanDistanceOfCoordinatesVisitor;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.jts.geom.segment.LineSegment;
import com.revolsys.jts.geom.segment.LineSegmentDoubleGF;
import com.revolsys.jts.operation.linemerge.LineMerger;

public class GeometryGraph extends Graph<LineSegment> {

  private final List<Point> points = new ArrayList<Point>();

  private final List<Geometry> geometries = new ArrayList<Geometry>();

  private final List<Point> startPoints = new ArrayList<Point>();

  private BoundingBox boundingBox;

  private double maxDistance;

  public GeometryGraph(final Geometry geometry) {
    this(geometry.getGeometryFactory());
    addGeometry(geometry);
  }

  public GeometryGraph(final GeometryFactory geometryFactory) {
    super(false);
    setGeometryFactory(geometryFactory);
    this.boundingBox = new BoundingBoxDoubleGf(geometryFactory);
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
    this.startPoints.add(new PointDouble(points.getPoint(0), 2));
    int index = 0;
    for (LineSegment lineSegment : points.segments()) {
      lineSegment = (LineSegment)lineSegment.clone();
      final Point from = lineSegment.getPoint(0);
      final Point to = lineSegment.getPoint(1);
      final Edge<LineSegment> edge = addEdge(lineSegment, from, to);
      attributes.put("segmentIndex", index++);
      edge.setProperties(attributes);
    }
  }

  public void addGeometry(Geometry geometry) {
    geometry = getGeometryFactory().geometry(geometry);
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();

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

    this.boundingBox = this.boundingBox.expandToInclude(geometry);
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
  @SuppressWarnings("rawtypes")
  public Geometry getBoundaryIntersection(final LineString line) {
    final List<Point> pointIntersections = new ArrayList<Point>();
    final List<LineString> lineIntersections = new ArrayList<LineString>();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = getBoundingBox(line);
    if (boundingBox.intersects(this.boundingBox)) {
      final LineString points = line;
      final int numPoints = points.getVertexCount();
      final Point fromPoint = points.getPoint(0);
      final Point toPoint = points.getPoint(numPoints - 1);

      Point previousPoint = fromPoint;
      for (int i = 1; i < numPoints; i++) {
        final Point nextPoint = points.getPoint(i);
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
            if (line2.distance(point) < this.maxDistance) {
              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                for (final Node<LineSegment> node : NodeLessThanDistanceOfCoordinatesVisitor
                  .getNodes(this, point, this.maxDistance)) {
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
      return geometryFactory.multiPoint(pointIntersections);
    } else {
      final LineMerger merger = new LineMerger();
      merger.add(lineIntersections);
      final Collection mergedLineStrings = merger.getMergedLineStrings();
      final MultiLineString multiLine = geometryFactory.multiLineString(mergedLineStrings);
      if (pointIntersections.isEmpty()) {
        return multiLine;
      } else {
        final MultiPoint multiPoint = geometryFactory.multiPoint(pointIntersections);
        return multiPoint.union(multiLine);
      }
    }
  }

  public BoundingBox getBoundingBox(final Geometry geometry) {
    if (geometry == null) {
      return BoundingBox.EMPTY;
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
    final EdgeAttributeValueComparator<LineSegment> comparator = new EdgeAttributeValueComparator<LineSegment>(
      "geometryIndex", "partIndex", "segmentIndex");
    final List<Geometry> geometries = new ArrayList<Geometry>(this.points);
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = geometryFactory.getAxisCount();
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
            final LineString line = geometryFactory.lineString(points);
            geometries.add(line);
          }
          points = new ArrayList<>();
          points.add(lineSegment.getPoint(0));
          points.add(lineSegment.getPoint(1));
        }
        if (points.size() > 1) {
          final int toDegree = toNode.getDegree();
          if (toDegree != 2) {
            final LineString line = geometryFactory.lineString(points);
            geometries.add(line);
            points = new ArrayList<>();
            points.add(toNode);
          }
        }
        previousNode = toNode;
      }
    }
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
    if (boundingBox.intersects(this.boundingBox)) {
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
            if (line2.distance(point) < maxDistance) {

              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                for (final Node<LineSegment> node : NodeLessThanDistanceOfCoordinatesVisitor
                  .getNodes(this, point, maxDistance)) {
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
    final Visitor<Edge<LineSegment>> visitor = new InvokeMethodVisitor<Edge<LineSegment>>(this,
      "removeDuplicateLineEdges");
    final Comparator<Edge<LineSegment>> comparator = new EdgeAttributeValueComparator<LineSegment>(
      "geometryIndex", "partIndex", "segmentIndex");
    visitEdges(comparator, visitor);
  }

  /**
   * Remove duplicate edges, edges must be processed in order of the index
   * attribute.
   *
   * @param edge1
   * @return
   */
  public boolean removeDuplicateLineEdges(final Edge<LineSegment> edge) {
    if (isLineString(edge)) {
      final Node<LineSegment> fromNode = edge.getFromNode();

      final Node<LineSegment> toNode = edge.getToNode();

      final Collection<Edge<LineSegment>> edges = fromNode.getEdgesTo(toNode);
      final int numDuplicates = edges.size();
      if (numDuplicates > 1) {
        edges.remove(edge);
        for (final Edge<LineSegment> removeEdge : edges) {
          if (isLineString(removeEdge)) {
            removeEdge.remove();
          }
        }
      }
    }
    return true;
  }

}
