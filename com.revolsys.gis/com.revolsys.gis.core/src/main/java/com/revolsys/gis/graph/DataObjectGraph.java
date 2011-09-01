package com.revolsys.gis.graph;

import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectUtil;
import com.vividsolutions.jts.geom.LineString;

public class DataObjectGraph extends Graph<DataObject> {
  public void add(final DataObject object) {
    final LineString line = object.getGeometryValue();
    add(object, line);
  }

  public void add(final List<DataObject> objects) {
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
}
