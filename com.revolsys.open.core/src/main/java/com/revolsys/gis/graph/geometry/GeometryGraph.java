package com.revolsys.gis.graph.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.comparator.EdgeAttributeValueComparator;
import com.revolsys.gis.graph.linestring.EdgeLessThanDistance;
import com.revolsys.gis.graph.visitor.NodeLessThanDistanceOfCoordinatesVisitor;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListIndexLineSegmentIterator;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleListCoordinatesList;
import com.revolsys.gis.model.geometry.LineSegment;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryGraph extends Graph<LineSegment> {

  private final List<Point> points = new ArrayList<Point>();

  private final List<Geometry> geometries = new ArrayList<Geometry>();

  private final List<Coordinates> startPoints = new ArrayList<Coordinates>();

  private final BoundingBox boundingBox;

  private double maxDistance;

  public GeometryGraph(final Geometry geometry) {
    this(GeometryFactory.getFactory(geometry));
    addGeometry(geometry);
  }

  public GeometryGraph(final GeometryFactory geometryFactory) {
    super(false);
    setGeometryFactory(geometryFactory);
    boundingBox = new BoundingBox(geometryFactory);
    final double scaleXY = getGeometryFactory().getScaleXY();
    if (scaleXY > 0) {
      maxDistance = 1 / scaleXY;
    } else {
      maxDistance = 0;
    }
  }

  private void addEdges(final CoordinatesList points,
    final Map<String, Object> attributes) {
    startPoints.add(new DoubleCoordinates(points.get(0), 2));
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      getGeometryFactory(), points);
    int index = 0;
    for (final LineSegment lineSegment : iterator) {
      final Coordinates from = lineSegment.get(0);
      final Coordinates to = lineSegment.get(1);
      final Edge<LineSegment> edge = addEdge(lineSegment, from, to);
      attributes.put("segmentIndex", index++);
      edge.setAttributes(attributes);
    }
  }

  public void addGeometry(Geometry geometry) {
    geometry = getGeometryFactory().createGeometry(geometry);
    final Map<String, Object> properties = new LinkedHashMap<String, Object>();

    final int geometryIndex = geometries.size();
    properties.put("geometryIndex", geometryIndex);
    geometries.add(geometry);
    for (int partIndex = 0; partIndex < geometry.getNumGeometries(); partIndex++) {
      properties.put("partIndex", partIndex);
      final Geometry part = geometry.getGeometryN(partIndex);
      if (part instanceof Point) {
        final Point point = (Point)part;
        points.add(point);
      } else if (part instanceof LineString) {
        final LineString line = (LineString)part;
        final CoordinatesList points = CoordinatesListUtil.get(line);
        properties.put("type", "LineString");
        addEdges(points, properties);
      } else if (part instanceof Polygon) {
        final Polygon polygon = (Polygon)part;
        final List<CoordinatesList> rings = CoordinatesListUtil.getAll(polygon);
        int ringIndex = 0;
        for (final CoordinatesList ring : rings) {
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

    final BoundingBox boundingBox = getBoundingBox(geometry);
    this.boundingBox.expandToInclude(boundingBox);
  }

  @Override
  protected LineSegment clone(final LineSegment segment, final LineString line) {
    return new LineSegment(line);
  }

  public BoundingBox getBoundingBox(final Geometry geometry) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(geometry);
    boundingBox.expandBy(maxDistance);
    return boundingBox;
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final LineSegment object = getEdgeObject(edgeId);
    if (object == null) {
      return null;
    } else {
      final LineString line = object.getLine();
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
    final List<Geometry> geometries = new ArrayList<Geometry>(points);
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int numAxis = geometryFactory.getNumAxis();
    DoubleListCoordinatesList points = new DoubleListCoordinatesList(numAxis);
    Node<LineSegment> previousNode = null;
    for (final Edge<LineSegment> edge : getEdges(comparator)) {
      final LineSegment lineSegment = edge.getObject();
      if (lineSegment.getLength() > 0) {
        final Node<LineSegment> fromNode = edge.getFromNode();
        final Node<LineSegment> toNode = edge.getToNode();
        if (previousNode == null) {
          points.addAll(lineSegment);
        } else if (fromNode == previousNode) {
          if (edge.getLength() > 0) {
            points.add(toNode);
          }
        } else {
          if (points.size() > 1) {
            final LineString line = geometryFactory.createLineString(points);
            geometries.add(line);
          }
          points = new DoubleListCoordinatesList(numAxis);
          points.addAll(lineSegment);
        }
        if (points.size() > 1) {
          final int toDegree = toNode.getDegree();
          if (toDegree != 2) {
            final LineString line = geometryFactory.createLineString(points);
            geometries.add(line);
            points = new DoubleListCoordinatesList(numAxis);
            points.add(toNode);
          }
        }
        previousNode = toNode;
      }
    }
    if (points.size() > 1) {
      final LineString line = geometryFactory.createLineString(points);
      geometries.add(line);
    }
    return geometryFactory.createGeometry(geometries);
  }

  /**
   * Get the intersection between the line and the boundary of this geometry.
   * 
   * @param line
   * @return
   */
  public Geometry getBoundaryIntersection(final LineString line) {
    final List<Geometry> intersections = new ArrayList<Geometry>();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final BoundingBox boundingBox = getBoundingBox(line);
    if (boundingBox.intersects(this.boundingBox)) {
      final CoordinatesList points = CoordinatesListUtil.get(line);
      final int numPoints = points.size();
      final Coordinates fromPoint = points.get(0);
      final Coordinates toPoint = points.get(numPoints - 1);

      Coordinates previousPoint = fromPoint;
      for (int i = 1; i < numPoints; i++) {
        final Coordinates nextPoint = points.get(i);
        final LineSegment line1 = new LineSegment(getGeometryFactory(),
          previousPoint, nextPoint);
        final List<Edge<LineSegment>> edges = EdgeLessThanDistance.getEdges(
          this, line1, maxDistance);
        for (final Edge<LineSegment> edge2 : edges) {
          final LineSegment line2 = edge2.getObject();
          final CoordinatesList segmentIntersection = line1.getIntersection(line2);
          final int numIntersections = segmentIntersection.size();
          if (numIntersections == 1) {
            final Coordinates intersection = segmentIntersection.get(0);
            if (intersection.equals(fromPoint) || intersection.equals(toPoint)) {
              // Point intersection, make sure it's not at the start
              final Node<LineSegment> node = findNode(intersection);
              if (node == null) {
                intersections.add(geometryFactory.createPoint(intersection));
              } else {
                final int degree = node.getDegree();
                if (isStartPoint(node)) {
                  if (degree > 2) {
                    // Intersection not at the start/end of the other line,
                    // taking
                    // into account loops
                    intersections.add(geometryFactory.createPoint(intersection));
                  }
                } else if (degree > 1) {
                  // Intersection not at the start/end of the other line
                  intersections.add(geometryFactory.createPoint(intersection));
                }
              }
            } else {
              // Intersection not at the start/end of the line
              intersections.add(geometryFactory.createPoint(intersection));
            }
          } else if (numIntersections == 2) {
            intersections.add(geometryFactory.createLineString(segmentIntersection));
          }
          for (final Coordinates point : line1) {
            if (line2.distance(point) < maxDistance) {
              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                for (final Node<LineSegment> node : NodeLessThanDistanceOfCoordinatesVisitor.getNodes(
                  this, point, maxDistance)) {
                  final int degree = node.getDegree();
                  if (isStartPoint(node)) {
                    if (degree > 2) {
                      // Intersection not at the start/end of the other line,
                      // taking
                      // into account loops
                      intersections.add(geometryFactory.createPoint(point));
                    }
                  } else if (degree > 1) {
                    // Intersection not at the start/end of the other line
                    intersections.add(geometryFactory.createPoint(point));
                  }
                }
              } else {
                // Intersection not at the start/end of the line
                intersections.add(geometryFactory.createPoint(point));
              }
            }
          }

        }
        previousPoint = nextPoint;
      }
    }
    return geometryFactory.createGeometry(intersections).union();
  }

  public boolean intersects(final LineString line) {
    final BoundingBox boundingBox = BoundingBox.getBoundingBox(line);
    final double scaleXY = getGeometryFactory().getScaleXY();
    double maxDistance = 0;
    if (scaleXY > 0) {
      maxDistance = 1 / scaleXY;
    }
    boundingBox.expandBy(maxDistance);
    if (boundingBox.intersects(this.boundingBox)) {
      final CoordinatesList points = CoordinatesListUtil.get(line);
      final int numPoints = points.size();
      final Coordinates fromPoint = points.get(0);
      final Coordinates toPoint = points.get(numPoints - 1);

      Coordinates previousPoint = fromPoint;
      for (int i = 1; i < numPoints; i++) {
        final Coordinates nextPoint = points.get(i);
        final LineSegment line1 = new LineSegment(previousPoint, nextPoint);
        final List<Edge<LineSegment>> edges = EdgeLessThanDistance.getEdges(
          this, line1, maxDistance);
        for (final Edge<LineSegment> edge2 : edges) {
          final LineSegment line2 = edge2.getObject();
          final CoordinatesList intersections = line1.getIntersection(line2);
          final int numIntersections = intersections.size();
          for (int j = 0; j < numIntersections; j++) {
            final Coordinates intersection = intersections.get(j);
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
          for (final Coordinates point : line1) {
            if (line2.distance(point) < maxDistance) {

              if (point.equals(fromPoint) || point.equals(toPoint)) {
                // Point intersection, make sure it's not at the start
                for (final Node<LineSegment> node : NodeLessThanDistanceOfCoordinatesVisitor.getNodes(
                  this, point, maxDistance)) {
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
    if ("LineString".equals(edge.getAttribute("type"))) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isStartPoint(final Coordinates coordinates) {
    return startPoints.contains(coordinates);
  }

  public void removeDuplicateLineEdges() {
    final Visitor<Edge<LineSegment>> visitor = new InvokeMethodVisitor<Edge<LineSegment>>(
      this, "removeDuplicateLineEdges");
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
