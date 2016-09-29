package com.revolsys.io;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.revolsys.collection.map.Maps;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.util.Property;

public class IoFactoryRegistry {
  static final Map<Class<? extends IoFactory>, Set<IoFactory>> factoriesByClass = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Map<String, IoFactory>> factoryByClassAndFileExtension = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Map<String, IoFactory>> factoryByClassAndMediaType = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Set<String>> mediaTypesByClass = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Set<String>> fileExtensionsByClass = new HashMap<>();

  static final Map<String, String> mediaTypeByFileExtension = new HashMap<>();

  static final Set<IoFactory> factories = new HashSet<>();

  static {
    try {
      final ClassLoader classLoader = IoFactoryRegistry.class.getClassLoader();
      final ServiceLoader<IoFactory> ioFactories = ServiceLoader.load(IoFactory.class, classLoader);
      for (final IoFactory ioFactory : ioFactories) {
        try {
          if (ioFactory.isAvailable()) {
            addFactory(ioFactory);
          }
        } catch (final Throwable e) {
          Logs.error(IoFactoryRegistry.class, e);
        }
      }

      final String resourceName = "META-INF/" + IoFactory.class.getName() + ".json";
      final Enumeration<URL> resources = classLoader.getResources(resourceName);
      while (resources.hasMoreElements()) {
        final URL resource = resources.nextElement();
        try {
          final Map<String, Object> config = JsonParser.getMap(resource.openStream());
          @SuppressWarnings("unchecked")
          final List<Map<String, Object>> factories = (List<Map<String, Object>>)config
            .get("factories");
          for (final Map<String, Object> factoryConfig : factories) {
            try {
              final String typeClassName = (String)factoryConfig.get("typeClass");
              if (Property.hasValue(typeClassName)) {
                final Class<?> factoryClass = Class.forName(typeClassName, false, classLoader);
                boolean initialized = false;
                for (final Method method : factoryClass.getDeclaredMethods()) {
                  final String methodName = method.getName();
                  if (methodName.equals("ioFactoryInit")) {
                    if (Modifier.isStatic(method.getModifiers())) {
                      if (method.getParameterTypes().length == 0) {
                        if (method.getReturnType() == Void.TYPE) {
                          initialized = true;
                          try {
                            method.invoke(null);
                          } catch (final Throwable e) {
                            Logs.error(factoryClass, e);
                          }
                        }
                      }
                    }
                  }
                }
                if (!initialized) {
                  if (IoFactory.class.isAssignableFrom(factoryClass)) {
                    try {
                      final IoFactory factory = (IoFactory)factoryClass.newInstance();
                      if (factory.isAvailable()) {
                        addFactory(factory);
                      }
                    } catch (final Throwable e) {
                      Logs.debug(factoryClass, "Unable to instantiate factory", e);
                    }
                  }
                }
              }
            } catch (final Throwable e) {
              Logs.error(IoFactoryRegistry.class, "Unable to add factory: " + factoryConfig, e);
            }
          }
        } catch (final Throwable e) {
          Logs.error(IoFactoryRegistry.class, "Unable to read resource: " + resource, e);
        }
      }
    } catch (final Throwable e) {
      Logs.error(IoFactoryRegistry.class, "Unable to read resources", e);
    }
  }

  public static void addFactory(final IoFactory factory) {
    synchronized (factories) {
      if (factories.add(factory)) {
        factory.init();
        final Class<? extends IoFactory> factoryClass = factory.getClass();
        addFactory(factory, factoryClass);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void addFactory(final IoFactory factory,
    final Class<? extends IoFactory> factoryClass) {
    final Class<?>[] interfaces = factoryClass.getInterfaces();
    for (final Class<?> factoryInterface : interfaces) {
      if (IoFactory.class.isAssignableFrom(factoryInterface)) {
        final Class<IoFactory> ioInterface = (Class<IoFactory>)factoryInterface;
        if (Maps.addToSet(factoriesByClass, ioInterface, factory)) {
          for (final String fileExtension : factory.getFileExtensions()) {
            Maps.addToTreeSet(fileExtensionsByClass, ioInterface, fileExtension);
            Maps.put(factoryByClassAndFileExtension, ioInterface, fileExtension, factory);

            for (final String mediaType : factory.getMediaTypes()) {
              mediaTypeByFileExtension.put(fileExtension.toLowerCase(), mediaType);
            }
          }
          for (final String mediaType : factory.getMediaTypes()) {
            Maps.put(factoryByClassAndMediaType, ioInterface, mediaType, factory);
            Maps.addToTreeSet(mediaTypesByClass, ioInterface, mediaType);
          }
        }
        addFactory(factory, ioInterface);
      }
    }
    final Class<?> superclass = factoryClass.getSuperclass();
    if (superclass != null) {
      if (IoFactory.class.isAssignableFrom(superclass)) {
        addFactory(factory, (Class<IoFactory>)superclass);
      }
    }
  }

  public static void clearInstance() {
    synchronized (IoFactoryRegistry.class) {
      factoriesByClass.clear();
      factoryByClassAndFileExtension.clear();
      mediaTypeByFileExtension.clear();
      factories.clear();
      factoryByClassAndMediaType.clear();
      fileExtensionsByClass.clear();
      mediaTypesByClass.clear();
    }
  }
}
