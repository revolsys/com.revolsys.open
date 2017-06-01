package com.revolsys.io.map;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.logging.Logs;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public interface MapObjectFactory {
  static String TYPE = "j:type";

  static String TYPE_CLASS = "j:typeClass";

  static String getType(final Map<String, ? extends Object> map) {
    String type = Maps.getString(map, TYPE);
    if (type == null) {
      type = Maps.getString(map, "type");
    }
    return type;
  }

  static String getType(final ObjectWithProperties map) {
    return getType(map.getProperties());
  }

  static String getTypeClass(final Map<String, ? extends Object> map) {
    String type = Maps.getString(map, TYPE_CLASS);
    if (type == null) {
      type = Maps.getString(map, "typeClass");
    }
    return type;
  }

  @SuppressWarnings("unchecked")
  static <V> V objectToObject(final Object value) {
    try {
      if (value instanceof Map) {
        final Map<String, ? extends Object> valueMap = (Map<String, ? extends Object>)value;
        return toObject(valueMap);
      } else if (value instanceof List) {
        final List<Object> newList = new ArrayList<>();
        final List<?> values = (List<?>)value;
        for (final Object listValue : values) {
          final Object newValue = objectToObject(listValue);
          newList.add(newValue);
        }
        return (V)newList;
      }
    } catch (final Throwable e) {
      Logs.debug(MapObjectFactory.class, "Unable to convert:" + value, e);
    }
    return (V)value;
  }

  static void setType(final Map<String, ? super Object> map, final String type) {
    if (Property.hasValue(type)) {
      map.put(TYPE, type);
    }
  }

  @SuppressWarnings("unchecked")
  static <V> V toObject(final Map<String, ? extends Object> map) {
    if (map == null) {
      return null;
    } else {
      final MapEx objectMap = new LinkedHashMapEx();
      for (final Entry<String, ? extends Object> entry : map.entrySet()) {
        final String key = entry.getKey();
        Object value = entry.getValue();
        value = objectToObject(value);
        objectMap.put(key, value);
      }
      final String typeClass = getTypeClass(objectMap);
      if (Property.hasValue(typeClass)) {
        final Constructor<V> configConstructor = JavaBeanUtil.getConstructor(typeClass, Map.class);
        final V object;
        if (configConstructor == null) {
          object = (V)JavaBeanUtil.createInstance(typeClass);
          ObjectWithProperties.setProperties(object, objectMap);
        } else {
          object = JavaBeanUtil.invokeConstructor(configConstructor, objectMap);
        }
        return object;
      } else {
        final String type = getType(objectMap);
        final MapObjectFactory objectFactory = MapObjectFactoryRegistry.getFactory(type);
        if (objectFactory == null) {
          return (V)objectMap;
        } else {
          return (V)objectFactory.mapToObject(objectMap);
        }
      }
    }
  }

  static <V> V toObject(final Object source) {
    final Resource resource = Resource.getResource(source);
    final Resource oldResource = Resource.setBaseResource(resource.getParent());

    try {
      final MapEx properties = Json.toMap(resource);
      return toObject(properties);
    } catch (final Throwable t) {
      Logs.error(MapObjectFactoryRegistry.class, "Cannot load object from " + resource, t);
      return null;
    } finally {
      Resource.setBaseResource(oldResource);
    }
  }

  /**
   * Convert the resource specified in the source parameter to an object. The properties parameter
   * will override those specific properties.
   *
   * @param source
   * @param properties
   * @return
   */
  static <V> V toObject(final Object source, final Map<String, ? extends Object> properties) {
    final Resource resource = Resource.getResource(source);
    final Resource oldResource = Resource.setBaseResource(resource.getParent());

    try {
      final MapEx resourceProperties = Json.toMap(resource);
      resourceProperties.putAll(properties);
      return toObject(resourceProperties);
    } catch (final Throwable t) {
      Logs.error(MapObjectFactoryRegistry.class, "Cannot load object from " + resource, t);
      return null;
    } finally {
      Resource.setBaseResource(oldResource);
    }
  }

  /**
   * Convert the resource specified in the source parameter to an object. The properties parameter
   * will override those specific properties.
   *
   * @param source
   * @param properties
   * @return
   */
  static <V> V toObject(final Object source, final Map<String, ? extends Object> properties,
    final BiConsumer<Resource, Throwable> errorHandler) {
    final Resource resource = Resource.getResource(source);
    final Resource oldResource = Resource.setBaseResource(resource.getParent());

    try {
      final MapEx resourceProperties = Json.toMap(resource);
      resourceProperties.putAll(properties);
      return toObject(resourceProperties);
    } catch (final Throwable t) {
      errorHandler.accept(resource, t);
      return null;
    } finally {
      Resource.setBaseResource(oldResource);
    }
  }

  static String toString(final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    return Json.toString(properties);
  }

  static void write(final File file, final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    Json.writeMap(properties, file, true);
  }

  static void write(final Path path, final MapSerializer serializer) {
    final PathResource resource = new PathResource(path);
    write(resource, serializer);
  }

  static void write(final Resource resource, final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    Json.writeMap(properties, resource, true);
  }

  String getDescription();

  String getTypeName();

  <V> V mapToObject(Map<String, ? extends Object> map);
}
