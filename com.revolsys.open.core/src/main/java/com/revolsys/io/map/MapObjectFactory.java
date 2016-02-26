package com.revolsys.io.map;

import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.PathResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public interface MapObjectFactory {
  static <V> V toObject(final File file) {
    final FileSystemResource resource = new FileSystemResource(file);
    return (V)toObject(resource);
  }

  @SuppressWarnings("unchecked")
  static <V> V toObject(final Map<String, ? extends Object> map) {
    final String typeClass = Maps.getString(map, "typeClass");
    if (Property.hasValue(typeClass)) {
      final Constructor<V> configConstructor = JavaBeanUtil.getConstructor(typeClass, Map.class);
      final V object;
      if (configConstructor == null) {
        object = (V)JavaBeanUtil.createInstance(typeClass);
      } else {
        object = JavaBeanUtil.invokeConstructor(configConstructor, map);
      }
      return object;
    } else {
      final String type = Maps.getString(map, "type");
      final MapObjectFactory objectFactory = MapObjectFactoryRegistry.getFactory(type);
      if (objectFactory == null) {
        LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error("No factory for " + type);
        return null;
      } else {
        return (V)objectFactory.mapToObject(map);
      }
    }
  }

  static <V> V toObject(final Resource resource) {
    final Resource oldResource = SpringUtil.setBaseResource(resource.getParent());

    try {
      final Map<String, Object> properties = Json.toMap(resource);
      return (V)MapObjectFactory.toObject(properties);
    } catch (final Throwable t) {
      LoggerFactory.getLogger(MapObjectFactoryRegistry.class)
        .error("Cannot load object from " + resource, t);
      return null;
    } finally {
      SpringUtil.setBaseResource(oldResource);
    }
  }

  static String toString(final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    return Json.toString(properties);
  }

  static void write(final File file, final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    Json.write(properties, file, true);
  }

  static void write(final Path path, final MapSerializer serializer) {
    final PathResource resource = new PathResource(path);
    write(resource, serializer);
  }

  static void write(final Resource resource, final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    Json.write(properties, resource, true);
  }

  String getDescription();

  String getTypeName();

  <V> V mapToObject(Map<String, ? extends Object> map);
}
