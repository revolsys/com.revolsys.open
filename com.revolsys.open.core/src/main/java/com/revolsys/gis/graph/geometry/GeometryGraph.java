package com.revolsys.gis.graph.geometry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.InvokeMethodVisitor;
import com.revolsys.collection.Visitor;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.graph.Edge;
import com.revolsys.gis.graph.Graph;
import com.revolsys.gis.graph.Node;
import com.revolsys.gis.graph.comparator.EdgeAttributeValueComparator;
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

  public GeometryGraph(final Geometry geometry) {
    this(GeometryFactory.getFactory(geometry));
    addGeometry(geometry);
  }

  public GeometryGraph(final GeometryFactory geometryFactory) {
    setGeometryFactory(geometryFactory);
  }

  public boolean isStartPoint(Coordinates coordinates) {
    return startPoints.contains(coordinates);
  }

  private void addEdges(final CoordinatesList points,
    final Map<String, Object> attributes) {
    startPoints.add(new DoubleCoordinates(points.get(0), 2));
    final CoordinatesListIndexLineSegmentIterator iterator = new CoordinatesListIndexLineSegmentIterator(
      getGeometryFactory(), points);
    int index = 0;
    for (final LineSegment lineSegment : iterator) {
      final Edge<LineSegment> edge = add(lineSegment, lineSegment.getLine());
      attributes.put("segmentIndex", index++);
      edge.addAttributes(attributes);
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
        for (Edge<LineSegment> removeEdge : edges) {
          if (isLineString(removeEdge)) {
            removeEdge.remove();
          }
        }
      }
    }
    return true;
  }

  private boolean isLineString(Edge<LineSegment> edge) {
    if ("LineString".equals(edge.getAttribute("type"))) {
      return true;
    } else {
      return false;
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
    GeometryFactory geometryFactory = getGeometryFactory();
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

  @Override
  protected LineSegment clone(final LineSegment segment, final LineString line) {
    return new LineSegment(line);
  }

  @Override
  protected Edge<LineSegment> addMerged(LineSegment mergedObject,
    LineString newLine) {
    LineString mergedLine = mergedObject.getLine();
    return add(mergedObject, mergedLine);
  }
}
