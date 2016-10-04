package com.revolsys.geometry.util;

import java.lang.ref.Reference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.util.JavaBeanUtil;

public class GeometryProperties {

  public static final String FEATURE_PROPERTY = "_feature";

  @SuppressWarnings("unchecked")
  public static void copyUserData(final Geometry oldGeometry, final Geometry newGeometry) {
    if (oldGeometry != null && newGeometry != null && oldGeometry != newGeometry) {
      Object userData = oldGeometry.getExtendedData();
      if (userData instanceof Map) {
        final Map<String, Object> oldValues = (Map<String, Object>)userData;
        final Map<String, Object> newValues = new TreeMap<>();
        for (final Entry<String, Object> entry : oldValues.entrySet()) {
          final String key = entry.getKey();
          final Object value = entry.getValue();
          if (value != null) {
            if (!(value instanceof Reference)) {
              final Object newValue = JavaBeanUtil.clone(value);
              newValues.put(key, newValue);
            }
          }
        }
        if (newValues.isEmpty()) {
          userData = null;
        } else {
          userData = newValues;
        }
      } else if (userData != null) {
        userData = JavaBeanUtil.clone(userData);
      }
      newGeometry.setExtendedData(userData);
    }
  }

  public static Record getGeometryFeature(final Geometry geometry) {
    return (Record)getGeometryProperty(geometry, FEATURE_PROPERTY);
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> getGeometryProperties(final Geometry geometry) {
    final Object userData = geometry.getExtendedData();
    if (userData instanceof Map) {
      final Map<String, Object> map = (Map<String, Object>)userData;
      return map;
    }
    return new TreeMap<>();
  }

  @SuppressWarnings("unchecked")
  public static <T extends Object> T getGeometryProperty(final Geometry geometry,
    final String name) {
    final Map<String, Object> map = getGeometryProperties(geometry);
    return (T)map.get(name);
  }

  @SuppressWarnings("unchecked")
  public static void setGeometryProperty(final Geometry geometry, final CharSequence name,
    final Object value) {
    Object userData = geometry.getExtendedData();
    if (!(userData instanceof Map)) {
      userData = new TreeMap<>();
      geometry.setExtendedData(userData);
    }
    final Map<Object, Object> map = (Map<Object, Object>)userData;
    map.put(name.toString(), value);

  }

}
