package com.revolsys.io.map;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.logging.Logs;
import com.revolsys.parallel.InvokeMethodRunnable;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Dates;
import com.revolsys.util.Property;

@SuppressWarnings("unchecked")
public class MapObjectFactoryRegistry {
  public static final Map<String, MapObjectFactory> TYPE_NAME_TO_FACTORY = new HashMap<>();

  static {
    final long startTime = System.currentTimeMillis();
    final List<Runnable> postInitMethods = new ArrayList<>();
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
              final String typeName = (String)factoryConfig.get("typeName");
              final String description = (String)factoryConfig.get("description");
              final String typeClassName = (String)factoryConfig.get("typeClass");
              final String methodName = (String)factoryConfig.get("methodName");
              if (Property.hasValue(typeClassName)) {
                final Class<?> factoryClass = Class.forName(typeClassName, false, classLoader);
                for (final Method method : factoryClass.getDeclaredMethods()) {
                  if (method.getName().equals("mapObjectFactoryInit")) {
                    if (Modifier.isStatic(method.getModifiers())) {
                      if (method.getParameterTypes().length == 0) {
                        if (method.getReturnType() == Void.TYPE) {
                          try {
                            method.invoke(null);
                          } catch (final Throwable e) {
                            Logs.error(factoryClass, e);
                          }
                        }
                      }
                    }
                  }
                  if (method.getName().equals("mapObjectFactoryPostInit")) {
                    if (Modifier.isStatic(method.getModifiers())) {
                      if (method.getParameterTypes().length == 0) {
                        if (method.getReturnType() == Void.TYPE) {
                          postInitMethods.add(new InvokeMethodRunnable(method));
                        }
                      }
                    }
                  }
                }

                if (Property.hasValue(typeName)) {
                  final MapObjectFactory factory;
                  if (Property.hasValue(methodName)) {
                    factory = new InvokeMethodMapObjectFactory(typeName, description, factoryClass,
                      methodName);
                  } else {
                    factory = new InvokeConstructorMapObjectFactory(typeName, description,
                      factoryClass);
                  }
                  addFactory(factory);
                }
              }
            } catch (final Throwable e) {
              Logs.error(MapObjectFactoryRegistry.class, "Unable to add factory: " + factoryConfig,
                e);
            }
          }
        } catch (final Throwable e) {
          Logs.error(MapObjectFactoryRegistry.class, "Unable to read resource: " + resource, e);
        }
      }
    } catch (final Throwable e) {
      Logs.error(MapObjectFactoryRegistry.class, "Unable to read resources", e);
    }
    for (final Runnable runnable : postInitMethods) {
      runnable.run();
    }
    Dates.debugEllapsedTime(MapObjectFactoryRegistry.class, "init", startTime);
  }

  public static void addFactory(final MapObjectFactory factory) {
    final String typeName = factory.getTypeName();
    TYPE_NAME_TO_FACTORY.put(typeName, factory);
  }

  public static MapObjectFactory getFactory(final String type) {
    return TYPE_NAME_TO_FACTORY.get(type);
  }

  public static void init() {
  }

  public static void newFactory(final String typeName,
    final Function<Map<String, ? extends Object>, Object> function) {
    newFactory(typeName, CaseConverter.toCapitalizedWords(typeName), function);
  }

  public static void newFactory(final String typeName, final String description,
    final Function<Map<String, ? extends Object>, Object> function) {
    final FunctionMapObjectFactory factory = new FunctionMapObjectFactory(typeName, description,
      function);
    addFactory(factory);
  }
}
