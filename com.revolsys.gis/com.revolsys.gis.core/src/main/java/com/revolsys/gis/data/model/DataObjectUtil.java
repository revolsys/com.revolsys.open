/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2007 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.gis.data.model;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

public final class DataObjectUtil {

  public static final DataObjectMetaData GEOMETRY_META_DATA = new DataObjectMetaDataImpl(
    new QName("http://open.revolsys.com/", "Feature", "rso"), new Attribute(
      "geometry", DataTypes.GEOMETRY, true));

  /**
   * Create a copy of the data object replacing the geometry with the new
   * geometry. If the existing geometry on the object has user data it will be
   * cloned to the new geometry.
   * 
   * @param object The object to copy.
   * @param geometry The new geometry.
   * @return The copied object.
   */
  public static <T extends DataObject> T copy(final T object,
    final Geometry geometry) {
    final Geometry oldGeometry = object.getGeometryValue();
    final T newObject = (T)object.clone();
    newObject.setGeometryValue(geometry);
    final Map<String, Object> userData = JtsGeometryUtil.getGeometryProperties(oldGeometry);
    if (userData != null) {
      geometry.setUserData(new HashMap<String, Object>(userData));
    }
    return newObject;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttributeByPath(final DataObject object,
    final String path) {

    final String[] propertyPath = path.split("\\.");
    Object propertyValue = object;
    for (int i = 0; i < propertyPath.length && propertyValue != null; i++) {
      final String propertyName = propertyPath[i];
      if (propertyValue instanceof DataObject) {
        final DataObject dataObject = (DataObject)propertyValue;

        if (dataObject.hasAttribute(propertyName)) {
          propertyValue = dataObject.getValue(propertyName);
          if (propertyValue == null) {
            return null;
          } else if (i + 1 < propertyPath.length) {
            final DataObjectMetaData metaData = dataObject.getMetaData();
            final DataObjectStore dataStore = metaData.getDataObjectStore();
            if (dataStore != null) {
              final CodeTable codeTable = dataStore.getCodeTableByColumn(propertyName);
              if (codeTable != null) {
                propertyValue = codeTable.getMap((Number)propertyValue);
              }
            }
          }
        } else {
          return null;
        }
      } else if (propertyValue instanceof Geometry) {
        final Geometry geometry = (Geometry)propertyValue;
        propertyValue = JtsGeometryUtil.getGeometryProperty(geometry,
          propertyName);
      } else if (propertyValue instanceof Map) {
        final Map<String, Object> map = (Map<String, Object>)propertyValue;
        propertyValue = map.get(propertyName);
      } else {
        propertyValue = JavaBeanUtil.getProperty(propertyValue, propertyName);
      }
    }
    return (T)propertyValue;
  }

  public static void setAttributeValue(final DataObject object,
    final String name, final Object value) {
    DataObjectMetaData metaData = object.getMetaData();
    DataObjectStore dataObjectStore = metaData.getDataObjectStore();
    if (dataObjectStore != null) {
      CodeTable codeTable = dataObjectStore.getCodeTableByColumn(name);
      if (codeTable != null && codeTable.getValueAttributeNames().size() == 1) {
        Object id = codeTable.getId(value);
        object.setValue(name, id);
        return;
      }
    }
    object.setValue(name, value);
  }

  private DataObjectUtil() {
  }
}
