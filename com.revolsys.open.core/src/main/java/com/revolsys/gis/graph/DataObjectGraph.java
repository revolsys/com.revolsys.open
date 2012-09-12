package com.revolsys.gis.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.filter.DataObjectGeometryFilter;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class DataObjectGraph extends Graph<DataObject> {

  public static <T extends Geometry> Filter<Edge<DataObject>> getEdgeFilter(
    final Filter<T> geometryFilter) {
    final Filter<DataObject> objectFilter = new DataObjectGeometryFilter<T>(
      geometryFilter);
    final EdgeObjectFilter<DataObject> edgeFilter = new EdgeObjectFilter<DataObject>(
      objectFilter);
    return edgeFilter;
  }

  public DataObjectGraph() {
    super(false);
  }

  public Edge<DataObject> addEdge(final DataObject object) {
    final LineString line = object.getGeometryValue();
    return addEdge(object, line);
  }

  public List<Edge<DataObject>> addEdges(final Collection<DataObject> objects) {
    List<Edge<DataObject>> edges = new ArrayList<Edge<DataObject>>();
    for (final DataObject object : objects) {
      Edge<DataObject> edge = addEdge(object);
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
  protected DataObject clone(final DataObject object, final LineString line) {
    if (object == null) {
      return null;
    } else {
      return DataObjectUtil.copy(object, line);
    }
  }

  @Override
  public LineString getEdgeLine(final int edgeId) {
    final DataObject object = getEdgeObject(edgeId);
    if (object == null) {
      return null;
    } else {
      final LineString line = object.getGeometryValue();
      return line;
    }
  }

  public List<DataObject> getObjects(final Collection<Integer> edgeIds) {
    final List<DataObject> objects = new ArrayList<DataObject>();
    for (final Integer edgeId : edgeIds) {
      final Edge<DataObject> edge = getEdge(edgeId);
      final DataObject object = edge.getObject();
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
  public String getTypeName(final Edge<DataObject> edge) {
    final DataObject object = edge.getObject();
    if (object == null) {
      return null;
    } else {
      final DataObjectMetaData metaData = object.getMetaData();
      final String typePath = metaData.getPath();
      return typePath;
    }
  }

  public boolean hasEdge(final DataObject object) {
    final LineString line = object.getGeometryValue();
    final Coordinates fromPoint = LineStringUtil.getFromPoint(line);
    final Coordinates toPoint = LineStringUtil.getToPoint(line);
    final Node<DataObject> fromNode = findNode(fromPoint);
    final Node<DataObject> toNode = findNode(toPoint);
    if (fromNode != null && toNode != null) {
      final Collection<Edge<DataObject>> edges = Node.getEdgesBetween(fromNode,
        toNode);
      for (final Edge<DataObject> edge : edges) {
        final LineString updateLine = edge.getLine();
        if (updateLine.equals(line)) {
          return true;
        }
      }
    }
    return false;
  }

  public List<Edge<DataObject>> splitEdges(final Coordinates point,
    final double distance) {
    final List<Edge<DataObject>> edges = new ArrayList<Edge<DataObject>>();
    for (final Edge<DataObject> edge : findEdges(point, distance)) {
      final LineString line = edge.getLine();
      final List<Edge<DataObject>> splitEdges = edge.split(point);
      DirectionalAttributes.edgeSplitAttributes(line, point, splitEdges);
      edges.addAll(splitEdges);
    }
    return edges;
  }
}
