package com.revolsys.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import com.revolsys.collection.map.Maps;
import com.revolsys.util.Exceptions;

public class IoFactoryRegistry {
  static final Map<Class<? extends IoFactory>, Set<IoFactory>> factoriesByClass = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Map<String, IoFactory>> factoryByClassAndFileExtension = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Map<String, IoFactory>> factoryByClassAndMediaType = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Set<String>> mediaTypesByClass = new HashMap<>();

  static final Map<Class<? extends IoFactory>, Set<String>> fileExtensionsByClass = new HashMap<>();

  static final Map<String, String> mediaTypeByFileExtension = new HashMap<>();

  static final Set<IoFactory> factories = new HashSet<>();

  static {
    synchronized (IoFactoryRegistry.class) {
      final ClassLoader classLoader = IoFactoryRegistry.class.getClassLoader();
      final ServiceLoader<IoFactory> ioFactories = ServiceLoader.load(IoFactory.class, classLoader);
      for (final IoFactory ioFactory : ioFactories) {
        try {
          if (ioFactory.isAvailable()) {
            addFactory(ioFactory);
          }
        } catch (final Throwable e) {
          Exceptions.log(IoFactoryRegistry.class, e);
        }
      }
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
