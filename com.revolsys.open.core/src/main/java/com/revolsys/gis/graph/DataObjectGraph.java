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

  public void add(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      add(object);
    }
  }

  public Edge<DataObject> add(final DataObject object) {
    final LineString line = object.getGeometryValue();
    return add(object, line);
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

  public List<Edge<DataObject>> splitEdges(Coordinates point, double distance) {
    List<Edge<DataObject>> edges = new ArrayList<Edge<DataObject>>();
    for (Edge<DataObject> edge : findEdges(point, distance)) {
      LineString line = edge.getLine();
      List<Edge<DataObject>> splitEdges = edge.split(point);
      DirectionalAttributes.edgeSplitAttributes(line, point, splitEdges);
      edges.addAll(splitEdges);
    }
    return edges;
  }

  public List<DataObject> getObjects(Collection<Integer> edgeIds) {
    List<DataObject> objects = new ArrayList<DataObject>();
    for (Integer edgeId : edgeIds) {
      Edge<DataObject> edge = getEdge(edgeId);
      DataObject object = edge.getObject();
      objects.add(object);
    }
    return objects;
  }
}
