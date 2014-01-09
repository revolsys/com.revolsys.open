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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jts.JtsGeometryUtil;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.JavaBeanUtil;
import com.vividsolutions.jts.geom.Geometry;

public final class DataObjectUtil {

  public static final DataObjectMetaData GEOMETRY_META_DATA = new DataObjectMetaDataImpl(
    "Feature", new Attribute("geometry", DataTypes.GEOMETRY, true));

  public static DataObject copy(final DataObjectMetaData metaData,
    final DataObject object) {
    final DataObject copy = new ArrayDataObject(metaData);
    copy.setValues(object);
    return copy;
  }

  /**
   * Create a copy of the data object replacing the geometry with the new
   * geometry. If the existing geometry on the object has user data it will be
   * cloned to the new geometry.
   * 
   * @param object The object to copy.
   * @param geometry The new geometry.
   * @return The copied object.
   */
  @SuppressWarnings("unchecked")
  public static <T extends DataObject> T copy(final T object,
    final Geometry geometry) {
    final Geometry oldGeometry = object.getGeometryValue();
    final T newObject = (T)object.clone();
    newObject.setGeometryValue(geometry);
    JtsGeometryUtil.copyUserData(oldGeometry, geometry);
    return newObject;
  }

  public static <D extends DataObject> List<D> filter(
    final Collection<D> objects, final Geometry geometry,
    final double maxDistance) {
    final List<D> results = new ArrayList<D>();
    for (final D object : objects) {
      final Geometry objectGeometry = object.getGeometryValue();
      final double distance = objectGeometry.distance(geometry);
      if (distance < maxDistance) {
        results.add(object);
      }
    }
    return results;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getAttributeByPath(final DataObject object,
    final String path) {
    final DataObjectMetaData metaData = object.getMetaData();

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
            final CodeTable codeTable = metaData.getCodeTableByColumn(propertyName);
            if (codeTable != null) {
              propertyValue = codeTable.getMap(propertyValue);
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
        if (propertyValue == null) {
          return null;
        } else if (i + 1 < propertyPath.length) {
          final CodeTable codeTable = metaData.getCodeTableByColumn(propertyName);
          if (codeTable != null) {
            propertyValue = codeTable.getMap(propertyValue);
          }
        }
      } else {
        try {
          propertyValue = JavaBeanUtil.getProperty(propertyValue, propertyName);
        } catch (final IllegalArgumentException e) {
          throw new IllegalArgumentException("Path does not exist " + path, e);
        }
      }
    }
    return (T)propertyValue;
  }

  public static boolean getBoolean(final DataObject object,
    final String attributeName) {
    if (object == null) {
      return false;
    } else {
      final Object value = object.getValue(attributeName);
      if (value == null) {
        return false;
      } else if (value instanceof Boolean) {
        final Boolean booleanValue = (Boolean)value;
        return booleanValue;
      } else if (value instanceof Number) {
        final Number number = (Number)value;
        return number.intValue() == 1;
      } else {
        final String stringValue = value.toString();
        if (stringValue.equals("1") || Boolean.parseBoolean(stringValue)) {
          return true;
        } else {
          return false;
        }
      }
    }
  }

  public static Double getDouble(final DataObject object,
    final int attributeIndex) {
    final Number value = object.getValue(attributeIndex);
    if (value == null) {
      return null;
    } else if (value instanceof Double) {
      return (Double)value;
    } else {
      return value.doubleValue();
    }
  }

  public static Double getDouble(final DataObject object,
    final String attributeName) {
    final Number value = object.getValue(attributeName);
    if (value == null) {
      return null;
    } else if (value instanceof Double) {
      return (Double)value;
    } else {
      return value.doubleValue();
    }
  }

  public static Integer getInteger(final DataObject object,
    final String attributeName) {
    if (object == null) {
      return null;
    } else {
      final Number value = object.getValue(attributeName);
      if (value == null) {
        return null;
      } else if (value instanceof Integer) {
        return (Integer)value;
      } else {
        return value.intValue();
      }
    }
  }

  public static Integer getInteger(final DataObject object,
    final String attributeName, final Integer defaultValue) {
    if (object == null) {
      return null;
    } else {
      final Number value = object.getValue(attributeName);
      if (value == null) {
        return defaultValue;
      } else if (value instanceof Integer) {
        return (Integer)value;
      } else {
        return value.intValue();
      }
    }
  }

  public static Long getLong(final DataObject object, final String attributeName) {
    final Number value = object.getValue(attributeName);
    if (value == null) {
      return null;
    } else if (value instanceof Long) {
      return (Long)value;
    } else {
      return value.longValue();
    }
  }

  public static DataObject getObject(final DataObjectMetaData metaData,
    final Map<String, Object> values) {
    final DataObject object = new ArrayDataObject(metaData);
    for (final Entry<String, Object> entry : values.entrySet()) {
      final String name = entry.getKey();
      final Attribute attribute = metaData.getAttribute(name);
      if (attribute != null) {
        final Object value = entry.getValue();
        if (value != null) {
          final DataType dataType = attribute.getType();
          @SuppressWarnings("unchecked")
          final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
          if (dataTypeClass.isAssignableFrom(value.getClass())) {
            object.setValue(name, value);
          } else {
            final StringConverter<Object> converter = StringConverterRegistry.getInstance()
              .getConverter(dataTypeClass);
            if (converter == null) {
              object.setValue(name, value);
            } else {
              final Object convertedValue = converter.toObject(value);
              object.setValue(name, convertedValue);
            }
          }
        }
      }
    }
    return object;
  }

  public static List<DataObject> getObjects(final DataObjectMetaData metaData,
    final Collection<? extends Map<String, Object>> list) {
    final List<DataObject> objects = new ArrayList<DataObject>();
    for (final Map<String, Object> map : list) {
      final DataObject object = getObject(metaData, map);
      objects.add(object);
    }
    return objects;
  }

  public static void mergeValue(final Map<String, Object> object,
    final DataObject object1, final DataObject object2, final String fieldName,
    final String separator) {
    final String value1 = object1.getString(fieldName);
    final String value2 = object2.getString(fieldName);
    Object value;
    if (!StringUtils.hasText(value1)) {
      value = value2;
    } else if (!StringUtils.hasText(value2)) {
      value = value1;
    } else {
      value = value1 + separator + value2;
    }
    object.put(fieldName, value);
  }

  public static void setValues(final DataObject target,
    final DataObject source, final Collection<String> attributesNames,
    final Collection<String> ignoreAttributeNames) {
    for (final String attributeName : attributesNames) {
      if (!ignoreAttributeNames.contains(attributeName)) {
        final Object oldValue = target.getValue(attributeName);
        Object newValue = source.getValue(attributeName);
        if (!EqualsRegistry.INSTANCE.equals(oldValue, newValue)) {
          newValue = JavaBeanUtil.clone(newValue);
          target.setValue(attributeName, newValue);
        }
      }
    }
  }

  private DataObjectUtil() {
  }

}
