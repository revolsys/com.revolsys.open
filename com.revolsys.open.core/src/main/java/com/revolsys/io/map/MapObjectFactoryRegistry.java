package com.revolsys.io.map;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.record.io.format.json.JsonParser;

@SuppressWarnings("unchecked")
public class MapObjectFactoryRegistry {

  public static final Map<String, MapObjectFactory> TYPE_NAME_TO_FACTORY = new HashMap<String, MapObjectFactory>();

  static {
    try {
      final ClassLoader classLoader = MapObjectFactoryRegistry.class.getClassLoader();
      final String resourceName = "META-INF/" + MapObjectFactory.class.getName() + ".json";
      final Enumeration<URL> resources = classLoader.getResources(resourceName);
      while (resources.hasMoreElements()) {
        final URL resource = resources.nextElement();
        try {
          final Map<String, Object> config = JsonParser.getMap(resource.openStream());
          final List<Map<String, Object>> factories = (List<Map<String, Object>>)config
            .get("factories");
          for (final Map<String, Object> factoryConfig : factories) {
            try {
              final String name = (String)factoryConfig.get("typeName");
              final String description = (String)factoryConfig.get("description");
              final String typeClassName = (String)factoryConfig.get("typeClass");
              final String methodName = (String)factoryConfig.get("methodName");
              final Class<?> factoryClass = Class.forName(typeClassName, false, classLoader);
              final InvokeMethodMapObjectFactory factory = new InvokeMethodMapObjectFactory(name,
                description, factoryClass, methodName);
              addFactory(factory);
            } catch (final Throwable e) {
              LoggerFactory.getLogger(MapObjectFactoryRegistry.class)
                .error("Unable to add factory: " + factoryConfig, e);
            }
          }
        } catch (final Throwable e) {
          LoggerFactory.getLogger(MapObjectFactoryRegistry.class)
            .error("Unable to read resource: " + resource, e);
        }
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(MapObjectFactoryRegistry.class).error("Unable to read resources", e);
    }
  }

  public static void addFactory(final MapObjectFactory factory) {
    final String typeName = factory.getTypeName();
    TYPE_NAME_TO_FACTORY.put(typeName, factory);
  }

  public static MapObjectFactory getFactory(final String type) {
    return TYPE_NAME_TO_FACTORY.get(type);
  }
}
