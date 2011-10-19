package com.revolsys.gis.graph;

import java.util.Collection;

import javax.xml.namespace.QName;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.revolsys.gis.data.model.filter.DataObjectGeometryFilter;
import com.revolsys.gis.graph.filter.EdgeObjectFilter;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;

public class DataObjectGraph extends Graph<DataObject> {

  public static <T extends Geometry> Filter<Edge<DataObject>> getEdgeFilter(
    Filter<T> geometryFilter) {
    final Filter<DataObject> objectFilter = new DataObjectGeometryFilter<T>(
      geometryFilter);
    final EdgeObjectFilter<DataObject> edgeFilter = new EdgeObjectFilter<DataObject>(
      objectFilter);
    return edgeFilter;
  }

  public Edge<DataObject> add(final DataObject object) {
    final LineString line = object.getGeometryValue();
    return add(object, line);
  }

  public void add(final Collection<DataObject> objects) {
    for (final DataObject object : objects) {
      add(object);
    }
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
  public QName getTypeName(final Edge<DataObject> edge) {
    final DataObject object = edge.getObject();
    if (object == null) {
      return null;
    } else {
      final DataObjectMetaData metaData = object.getMetaData();
      final QName typeName = metaData.getName();
      return typeName;
    }
  }

  public boolean hasEdge(final DataObject object) {
    LineString line = object.getGeometryValue();
    Coordinates fromPoint = LineStringUtil.getFromPoint(line);
    Coordinates toPoint = LineStringUtil.getToPoint(line);
    Node<DataObject> fromNode = findNode(fromPoint);
    Node<DataObject> toNode = findNode(toPoint);
    if (fromNode != null && toNode != null) {
      Collection<Edge<DataObject>> edges = Node.getEdgesBetween(fromNode,
        toNode);
      for (Edge<DataObject> edge : edges) {
        LineString updateLine = edge.getLine();
        if (updateLine.equals(line)) {
          return true;
        }
      }
    }
    return false;
  }
}
