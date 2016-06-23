package com.revolsys.io.map;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.util.Property;

public interface MapSerializer {
  default void addAllToMap(final Map<String, Object> map, final Map<String, Object> values) {
    if (map != null && values != null) {
      for (final Entry<String, Object> entry : values.entrySet()) {
        final String name = entry.getKey();
        final Object value = entry.getValue();
        addToMap(map, name, value);
      }
    }
  }

  /**
   * <p>Add the value to the map. If the value is a {@link MapSerializer} then add the result of
   * {@link MapSerializer#toMap()}. If the value is a supported type add it to the map, otherwise
   * convert the value to a string. Null values will be ignored.</p>
   *
   * @param map
   * @param name
   * @param value
   */
  default void addToMap(final Map<String, Object> map, final String name, final Object value) {
    final Object mapValue = toMapValue(value);
    if (Property.hasValue(mapValue)) {
      map.put(name, mapValue);
    } else {
      map.remove(mapValue);
    }
  }

  default void addToMap(final Map<String, Object> map, final String name, final Object value,
    final Object defaultValue) {
    if (DataType.equal(value, defaultValue)) {
      map.remove(name);
    } else {
      final Object mapValue = toMapValue(value);
      if (Property.hasValue(mapValue) || defaultValue == null) {
        map.put(name, mapValue);
      } else {
        map.remove(name);
      }
    }
  }

  default void addTypeToMap(final Map<String, Object> map, final String type) {
    MapObjectFactory.setType(map, type);
  }

  default MapEx newTypeMap(final String type) {
    final MapEx map = new LinkedHashMapEx();
    addTypeToMap(map, type);
    return map;
  }

  /**
   * <p>Convert the object to a Map of property name, value pairs. The values can be one of
   * the following supported types. Other values should be converted to one of these values.</p>
   *
   * <ul>
   *   <li>boolean or {@link Boolean}</li>
   *   <li>byte or {@link Byte}</li>
   *   <li>short or {@link Short}</li>
   *   <li>int or {@link Integer}</li>
   *   <li>long or {@link Long}</li>
   *   <li>float or {@link Float}</li>
   *   <li>double or {@link Double}</li>
   *   <li>{@link String}</li>
   *   <li>{@link Number} subclasses</li>
   *   <li>{@link Collection} of supported values</li>
   *   <li>{@link Map}<String,Object> of supported values</li>
   *   <li>null</li>
   * </ul>
   * @return
   */
  MapEx toMap();

  @SuppressWarnings("rawtypes")
  default Object toMapValue(final Object value) {
    if (value == null) {
      return null;
    } else {

      if (value instanceof MapSerializer) {
        final MapSerializer mapSerializer = (MapSerializer)value;
        final Map<String, Object> mapObject = mapSerializer.toMap();
        if (mapObject == null || mapObject.isEmpty()) {
          return null;
        }
        return mapObject;
      } else if (value instanceof Map) {
        final Map<String, Object> mapObject = (Map<String, Object>)value;
        if (mapObject.isEmpty()) {
          return null;
        }
        final MapEx map = new LinkedHashMapEx();
        for (final Entry<String, Object> entry : mapObject.entrySet()) {
          final String name = entry.getKey();
          final Object object = entry.getValue();
          addToMap(map, name, object);
        }
        return map;
      } else if (value instanceof Collection) {
        final Collection collectionObject = (Collection)value;
        if (collectionObject.isEmpty()) {
          return null;
        }
        final List<Object> list = new ArrayList<Object>();
        for (final Object object : collectionObject) {
          final Object listValue = toMapValue(object);
          list.add(listValue);
        }
        return list;
      } else if (value instanceof Boolean) {
        return value;
      } else if (value instanceof Number) {
        return value;
      } else if (value instanceof String) {
        final String string = (String)value;
        if (Property.hasValue(string)) {
          return string.trim();
        } else {
          return null;
        }
      } else if (value instanceof Component) {
        return null;
      } else {
        final String string = DataTypes.toString(value);
        if (Property.hasValue(string)) {
          return string.trim();
        } else {
          return null;
        }
      }

    }
  }

  default void writeToFile(final Object target) {
    if (target != null) {
      final MapEx map = toMap();
      Json.writeMap(map, target, true);
    }
  }
}
