package com.revolsys.io;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class IoFactoryRegistry {
  private static WeakReference<IoFactoryRegistry> instance = new WeakReference<IoFactoryRegistry>(new IoFactoryRegistry());

  private static final Logger LOG = LoggerFactory.getLogger(IoFactoryRegistry.class);

  public static IoFactoryRegistry getInstance() {
    return instance.get();
  }

  private final Map<Class<? extends IoFactory>, Set<IoFactory>> classFactories = new HashMap<Class<? extends IoFactory>, Set<IoFactory>>();

  private final Map<Class<? extends IoFactory>, Map<String, IoFactory>> classFactoriesByFileExtension = new HashMap<Class<? extends IoFactory>, Map<String, IoFactory>>();

  private final Map<Class<? extends IoFactory>, Map<String, IoFactory>> classFactoriesByMediaType = new HashMap<Class<? extends IoFactory>, Map<String, IoFactory>>();

  private final Set<IoFactory> factories = new HashSet<IoFactory>();

  public IoFactoryRegistry() {
    final ClassLoader classLoader = IoFactoryRegistry.class.getClassLoader();
    try {
      final Enumeration<URL> urls = classLoader.getResources("META-INF/com.revolsys.io.IoFactory.properties");
      while (urls.hasMoreElements()) {
        final URL resourceUrl = urls.nextElement();
        try {
          final Properties properties = new Properties();
          properties.load(resourceUrl.openStream());
          final String factoryClassNames = properties.getProperty("com.revolsys.io.IoFactory.factoryClassNames");
          for (final String factoryClassName : factoryClassNames.split(",")) {
            final Class<?> factoryClass = Class.forName(factoryClassName.trim());
            if (IoFactory.class.isAssignableFrom(factoryClass)) {
              @SuppressWarnings("unchecked")
              final IoFactory factory = ((Class<IoFactory>)factoryClass).newInstance();
              addFactory(factory);
            } else {
              LOG.error(factoryClassName + " is not a subclass of "
                + IoFactory.class);
            }
          }
        } catch (final Throwable e) {
          LOG.error("Unable to load: " + resourceUrl, e);
        }
      }
    } catch (final IOException e) {
      LOG.error("Unable to load META-INF/com.revolsys.io.MapWriter.properties",
        e);
    }
  }

  public synchronized void addFactory(final IoFactory factory) {
    if (factories.add(factory)) {
      final Class<? extends IoFactory> factoryClass = factory.getClass();
      addFactory(factory, factoryClass);
    }
  }

  @SuppressWarnings("unchecked")
  private void addFactory(
    final IoFactory factory,
    final Class<? extends IoFactory> factoryClass) {
    final Class<?>[] interfaces = factoryClass.getInterfaces();
    for (final Class<?> factoryInterface : interfaces) {
      if (IoFactory.class.isAssignableFrom(factoryInterface)) {
        final Class<IoFactory> ioInterface = (Class<IoFactory>)factoryInterface;
        final Set<IoFactory> factories = getFactories(ioInterface);
        if (factories.add(factory)) {
          for (final String fileExtension : factory.getFileExtensions()) {
            final Map<String, IoFactory> factoriesByFileExtension = getFactoriesByFileExtensionMap(ioInterface);
            factoriesByFileExtension.put(fileExtension, factory);
          }
          final Map<String, IoFactory> factoriesByMediaType = getFactoriesByMediaType(ioInterface);
          for (final String mediaType : factory.getMediaTypes()) {
            factoriesByMediaType.put(mediaType, factory);
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

  @SuppressWarnings("unchecked")
  public synchronized <F extends IoFactory> Set<F> getFactories(
    final Class<F> factoryClass) {
    Set<IoFactory> factories = classFactories.get(factoryClass);
    if (factories == null) {
      factories = new LinkedHashSet<IoFactory>();
      classFactories.put(factoryClass, factories);
    }
    return (Set<F>)factories;
  }

  public Set<IoFactory> getFactoriesByClass() {
    return factories;
  }

  public <F extends IoFactory> List<F> getFactoriesByFileExtension(
    final Class<F> factoryClass,
    final List<String> fileExtensions) {
    final Map<String, F> factoriesByFileExtension = getFactoriesByFileExtensionMap(factoryClass);
    final List<F> factories = new ArrayList<F>();
    for (final String fileExtension : fileExtensions) {
      final F factory = factoriesByFileExtension.get(fileExtension);
      if (factory != null) {
        factories.add(factory);
      }
    }
    return factories;
  }

  @SuppressWarnings("unchecked")
  public synchronized <F extends IoFactory> Map<String, F> getFactoriesByFileExtensionMap(
    final Class<F> factoryClass) {
    Map<String, IoFactory> factoriesByFileExtension = classFactoriesByFileExtension.get(factoryClass);
    if (factoriesByFileExtension == null) {
      factoriesByFileExtension = new TreeMap<String, IoFactory>();
      classFactoriesByFileExtension.put(factoryClass, factoriesByFileExtension);
    }
    return (Map<String, F>)factoriesByFileExtension;
  }

  @SuppressWarnings("unchecked")
  public synchronized <F extends IoFactory> Map<String, F> getFactoriesByMediaType(
    final Class<F> factoryClass) {
    Map<String, IoFactory> factoriesByMediaType = classFactoriesByMediaType.get(factoryClass);
    if (factoriesByMediaType == null) {
      factoriesByMediaType = new TreeMap<String, IoFactory>();
      classFactoriesByMediaType.put(factoryClass, factoriesByMediaType);
    }
    return (Map<String, F>)factoriesByMediaType;
  }

  public <F extends IoFactory> F getFactoryByFileExtension(
    final Class<F> factoryClass,
    final String fileExtension) {
    final Map<String, F> factoriesByFileExtension = getFactoriesByFileExtensionMap(factoryClass);
    return factoriesByFileExtension.get(fileExtension);
  }

  public <F extends IoFactory> F getFactoryByFileName(
    final Class<F> factoryClass,
    final String fileName) {
    final String fileExtension = FileUtil.getFileNameExtension(fileName);
    return getFactoryByFileExtension(factoryClass, fileExtension);
  }

  public <F extends IoFactory> F getFactoryByMediaType(
    final Class<F> factoryClass,
    final String mediaType) {
    final Map<String, F> factoriesByMediaType = getFactoriesByMediaType(factoryClass);
    return factoriesByMediaType.get(mediaType);
  }

  public <F extends IoFactory> F getFactoryByResource(
    final Class<F> factoryClass,
    final Resource resource) {
    String fileName;
    if (resource instanceof UrlResource) {
      final UrlResource urlResoure = (UrlResource)resource;
      try {
        fileName = urlResoure.getURL().getPath();
      } catch (final IOException e) {
        fileName = resource.getFilename();
      }
    } else {
      fileName = resource.getFilename();
    }
    return getFactoryByFileName(factoryClass, fileName);
  }

  public <F extends IoFactory> Set<String> getFileExtensions(
    final Class<F> factoryClass) {
    final Map<String, F> factoriesByFileExtension = getFactoriesByFileExtensionMap(factoryClass);
    return factoriesByFileExtension.keySet();
  }

  public <F extends IoFactory> Set<String> getMediaTypes(
    final Class<F> factoryClass) {
    final Map<String, F> factoriesByMediaType = getFactoriesByMediaType(factoryClass);
    return factoriesByMediaType.keySet();
  }

  public void setFactories(final Set<IoFactory> factories) {
    classFactoriesByFileExtension.clear();
    classFactoriesByMediaType.clear();
    classFactories.clear();
    this.factories.clear();
    for (final IoFactory factory : factories) {
      addFactory(factory);
    }
  }
}
