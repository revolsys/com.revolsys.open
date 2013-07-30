package com.revolsys.io.map;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class MapSerializerUtil {
  /**
   * <p>Add the value to the map. If the value is a {@link MapSerializer} then add the result of
   * {@link MapSerializer#toMap()}. If the value is a supported type add it to the map, otherwise
   * convert the value to a string. Null values will be ignored.</p>
   * 
   * @param map
   * @param name
   * @param value
   */
  public static void add(final Map<String, Object> map, final String name,
    final Object value) {
    final Object mapValue = getValue(value);
    if (mapValue != null) {
      map.put(name, mapValue);
    }
  }

  public static void add(final Map<String, Object> map, final String name,
    final Object value, final Object defaultValue) {
    if (!EqualsRegistry.equal(value, defaultValue)) {
      final Object mapValue = getValue(value);
      if (mapValue != null) {
        map.put(name, mapValue);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  public static Object getValue(final Object value) {
    if (value == null) {
      return null;
    } else {

      if (value instanceof MapSerializer) {
        final MapSerializer mapSerializer = (MapSerializer)value;
        final Map<String, Object> mapObject = mapSerializer.toMap();
        if (mapObject.isEmpty()) {
          return null;
        }
        return mapObject;
      } else if (value instanceof Map) {
        final Map<String, Object> mapObject = (Map<String, Object>)value;
        if (mapObject.isEmpty()) {
          return null;
        }
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (final Entry<String, Object> entry : mapObject.entrySet()) {
          final String name = entry.getKey();
          final Object object = entry.getValue();
          add(map, name, object);
        }
        return map;
      } else if (value instanceof Collection) {
        final Collection collectionObject = (Collection)value;
        if (collectionObject.isEmpty()) {
          return null;
        }
        final List<Object> list = new ArrayList<Object>();
        for (final Object object : collectionObject) {
          final Object listValue = getValue(object);
          list.add(listValue);
        }
        return list;
      } else if (value instanceof Boolean) {
        return value;
      } else if (value instanceof Number) {
        return value;
      } else if (value instanceof String) {
        final String string = (String)value;
        if (StringUtils.hasText(string)) {
          return string.trim();
        } else {
          return null;
        }
      } else if (value instanceof Component) {
        return null;
      } else {
        final String string = StringConverterRegistry.toString(value);
        if (StringUtils.hasText(string)) {
          return string.trim();
        } else {
          return null;
        }
      }

    }
  }
}
