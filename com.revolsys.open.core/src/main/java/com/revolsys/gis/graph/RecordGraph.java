package com.revolsys.gis.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.data.filter.RecordGeometryFilter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.Records;
import com.revolsys.data.record.property.DirectionalAttributes;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

public class RecordGraph extends Graph<Record> {

  public static <T extends Geometry> Filter<Edge<Record>> getEdgeFilter(
    final Filter<T> geometryFilter) {
    final Filter<Record> objectFilter = new RecordGeometryFilter<T>(geometryFilter);
    final EdgeObjectFilter<Record> edgeFilter = new EdgeObjectFilter<Record>(objectFilter);
    return edgeFilter;
  }

  public RecordGraph() {
    super(false);
  }

  public RecordGraph(final Collection<? extends Record> objects) {
    addEdges(objects);
  }

  public Edge<Record> addEdge(final Record object) {
    final LineString line = object.getGeometryValue();
    return addEdge(object, line);
  }

  public List<Edge<Record>> addEdges(final Collection<? extends Record> objects) {
    final List<Edge<Record>> edges = new ArrayList<Edge<Record>>();
    for (final Record object : objects) {
      final Edge<Record> edge = addEdge(object);
      edges.add(edge);
    }
    return edges;
  }

  /**
   * Clone the object, setting the line property to the new value.
   *
   * @param object The object to clone.
   * @param line The line.
   * @return The new object.
   */
  @Override
  protected Record clone(final Record object, final LineString line) {
    if (object == null) {
      return null;
    } else {
      return Records.copy(object, line);
    }
  }

  public Edge<Record> getEdge(final Record record) {
    final LineString line = record.getGeometryValue();
    final Point fromPoint = line.getPoint(0);
    final Node<Record> fromNode = findNode(fromPoint);
    if (fromNode != null) {
      for (final Edge<Record> edge : fromNode.getEdges()) {
        if (edge.getObject() == record) {
          return edge;
        }
      }
    }
    return null;
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final Record object = getEdgeObject(edgeId);
    if (object == null) {
      return null;
    } else {
      final LineString line = object.getGeometryValue();
      return line;
    }
  }

  @Override
  public Node<Record> getNode(final Point point) {
    return super.getNode(point);
  }

  public List<Record> getObjects(final Collection<Integer> edgeIds) {
    final List<Record> objects = new ArrayList<Record>();
    for (final Integer edgeId : edgeIds) {
      final Edge<Record> edge = getEdge(edgeId);
      final Record object = edge.getObject();
      objects.add(object);
    }
    return objects;
  }

  /**
   * Get the type name for the edge.
   *
   * @param edge The edge.
   * @return The type name.
   */
  @Override
  public String getTypeName(final Edge<Record> edge) {
    final Record object = edge.getObject();
    if (object == null) {
      return null;
    } else {
      final RecordDefinition recordDefinition = object.getRecordDefinition();
      final String typePath = recordDefinition.getPath();
      return typePath;
    }
  }

  public boolean hasEdge(final Record object) {
    final LineString line = object.getGeometryValue();
    final Point fromPoint = line.getPoint(0);
    final Point toPoint = line.getPoint(-1);
    final Node<Record> fromNode = findNode(fromPoint);
    final Node<Record> toNode = findNode(toPoint);
    if (fromNode != null && toNode != null) {
      final Collection<Edge<Record>> edges = Node.getEdgesBetween(fromNode, toNode);
      for (final Edge<Record> edge : edges) {
        final LineString updateLine = edge.getLine();
        if (updateLine.equals(line)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Edge<Record> merge(final Node<Record> node, final Edge<Record> edge1,
    final Edge<Record> edge2) {
    final Record object1 = edge1.getObject();
    final Record object2 = edge2.getObject();
    final Record mergedObject = DirectionalAttributes.merge(node, object1, object2);
    final Edge<Record> mergedEdge = addEdge(mergedObject);
    remove(edge1);
    remove(edge2);
    return mergedEdge;
  }

  public List<Edge<Record>> splitEdges(final Point point, final double distance) {
    final List<Edge<Record>> edges = new ArrayList<Edge<Record>>();
    for (final Edge<Record> edge : findEdges(point, distance)) {
      final LineString line = edge.getLine();
      final List<Edge<Record>> splitEdges = edge.split(new PointDouble(point));
      DirectionalAttributes.edgeSplitAttributes(line, point, splitEdges);
      edges.addAll(splitEdges);
    }
    return edges;
  }

}
