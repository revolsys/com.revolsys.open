package com.revolsys.io.map;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.collection.map.Maps;
import com.revolsys.format.json.JsonMapIoFactory;
import com.revolsys.format.json.JsonParser;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

@SuppressWarnings("unchecked")
public class MapObjectFactoryRegistry {

  public static void addFactory(final MapObjectFactory factory) {
    final String typeName = factory.getTypeName();
    TYPE_NAME_TO_FACTORY.put(typeName, factory);
  }

  public static <V> V toObject(final File file) {
    final FileSystemResource resource = new FileSystemResource(file);
    return (V)toObject(resource);
  }

  public static <V> V toObject(final Map<String, ? extends Object> map) {
    final String typeClass = Maps.getString(map, "typeClass");
    if (Property.hasValue(typeClass)) {
      final Constructor<V> configConstructor = JavaBeanUtil.getConstructor(
        typeClass, Map.class);
      final V object;
      if (configConstructor == null) {
        object = (V)JavaBeanUtil.createInstance(typeClass);
      } else {
        object = JavaBeanUtil.invokeConstructor(configConstructor, map);
      }
      return object;
    } else {
      final String type = Maps.getString(map, "type");
      final MapObjectFactory objectFactory = TYPE_NAME_TO_FACTORY.get(type);
      if (objectFactory == null) {
        LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error(
          "No factory for " + type);
        return null;
      } else {
        return (V)objectFactory.toObject(map);
      }
    }
  }

  public static <V> V toObject(final Resource resource) {
    final Resource oldResource = SpringUtil.setBaseResource(SpringUtil.getParentResource(resource));

    try {
      final Map<String, Object> properties = JsonMapIoFactory.toMap(resource);
      return (V)MapObjectFactoryRegistry.toObject(properties);
    } catch (final Throwable t) {
      LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error(
        "Cannot load object from " + resource, t);
      return null;
    } finally {
      SpringUtil.setBaseResource(oldResource);
    }
  }

  public static String toString(final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    return JsonMapIoFactory.toString(properties);
  }

  public static void write(final File file, final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    JsonMapIoFactory.write(properties, file, true);
  }

  public static void write(final Resource resource,
    final MapSerializer serializer) {
    final Map<String, Object> properties = serializer.toMap();
    JsonMapIoFactory.write(properties, resource, true);
  }

  public static final Map<String, MapObjectFactory> TYPE_NAME_TO_FACTORY = new HashMap<String, MapObjectFactory>();

  static {
    try {
      final ClassLoader classLoader = MapObjectFactoryRegistry.class.getClassLoader();
      final String resourceName = "META-INF/"
          + MapObjectFactory.class.getName() + ".json";
      final Enumeration<URL> resources = classLoader.getResources(resourceName);
      while (resources.hasMoreElements()) {
        final URL resource = resources.nextElement();
        try {
          final Map<String, Object> config = JsonParser.getMap(resource.openStream());
          final List<Map<String, Object>> factories = (List<Map<String, Object>>)config.get("factories");
          for (final Map<String, Object> factoryConfig : factories) {
            try {
              final String name = (String)factoryConfig.get("typeName");
              final String description = (String)factoryConfig.get("description");
              final String typeClassName = (String)factoryConfig.get("typeClass");
              final String methodName = (String)factoryConfig.get("methodName");
              final Class<?> factoryClass = Class.forName(typeClassName, false,
                classLoader);
              final InvokeMethodMapObjectFactory factory = new InvokeMethodMapObjectFactory(
                name, description, factoryClass, methodName);
              addFactory(factory);
            } catch (final Throwable e) {
              LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error(
                "Unable to add factory: " + factoryConfig, e);
            }
          }
        } catch (final Throwable e) {
          LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error(
            "Unable to read resource: " + resource, e);
        }
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error(
        "Unable to read resources", e);
    }
  }
}
