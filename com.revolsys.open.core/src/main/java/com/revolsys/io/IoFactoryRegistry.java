package com.revolsys.io;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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

import com.revolsys.data.io.RecordWriterFactory;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class IoFactoryRegistry {

  public static void clearInstance() {
    instance = null;
  }

  public static String getFileExtension(final String resultFormat) {
    final IoFactoryRegistry ioFactory = getInstance();
    final RecordWriterFactory writerFactory = ioFactory.getFactoryByMediaType(
      RecordWriterFactory.class, resultFormat);
    if (writerFactory == null) {
      return null;
    } else {
      return writerFactory.getFileExtension(resultFormat);
    }
  }

  public static IoFactoryRegistry getInstance() {
    synchronized (IoFactoryRegistry.class) {
      if (instance == null) {
        instance = new IoFactoryRegistry();
      }
      return instance;
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(IoFactoryRegistry.class);

  private static IoFactoryRegistry instance = new IoFactoryRegistry();

  private final Map<Class<? extends IoFactory>, Set<String>> classFileExtensions = new HashMap<Class<? extends IoFactory>, Set<String>>();

  private final Map<Class<? extends IoFactory>, Set<IoFactory>> classFactories = new HashMap<Class<? extends IoFactory>, Set<IoFactory>>();

  private final Map<Class<? extends IoFactory>, Map<String, IoFactory>> classFactoriesByFileExtension = new HashMap<Class<? extends IoFactory>, Map<String, IoFactory>>();

  private final Map<Class<? extends IoFactory>, Map<String, IoFactory>> classFactoriesByMediaType = new HashMap<Class<? extends IoFactory>, Map<String, IoFactory>>();

  private final Set<IoFactory> factories = new HashSet<IoFactory>();

  private final Map<String, String> extensionMimeTypeMap = new HashMap<String, String>();

  public IoFactoryRegistry() {
    synchronized (IoFactoryRegistry.class) {
      if (instance == null) {
        instance = this;
      }
    }
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
            if (Property.hasValue(factoryClassName)) {
              try {
                final Class<?> factoryClass = Class.forName(factoryClassName.trim());
                if (IoFactory.class.isAssignableFrom(factoryClass)) {
                  @SuppressWarnings("unchecked")
                  final IoFactory factory = ((Class<IoFactory>)factoryClass).newInstance();
                  addFactory(factory);
                } else {
                  LOG.error(factoryClassName + " is not a subclass of "
                    + IoFactory.class);
                }
              } catch (final Throwable e) {
                LOG.error("Unable to load: " + factoryClassName, e);
              }
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
    if (this.factories.add(factory)) {
      final Class<? extends IoFactory> factoryClass = factory.getClass();
      addFactory(factory, factoryClass);
    }
  }

  @SuppressWarnings("unchecked")
  private void addFactory(final IoFactory factory,
    final Class<? extends IoFactory> factoryClass) {
    final Class<?>[] interfaces = factoryClass.getInterfaces();
    for (final Class<?> factoryInterface : interfaces) {
      if (IoFactory.class.isAssignableFrom(factoryInterface)) {
        final Class<IoFactory> ioInterface = (Class<IoFactory>)factoryInterface;
        final Set<IoFactory> factories = getFactories(ioInterface);
        if (factories.add(factory)) {
          for (final String fileExtension : factory.getFileExtensions()) {
            CollectionUtil.addToTreeSet(this.classFileExtensions, ioInterface,
              fileExtension);
            final Map<String, IoFactory> factoriesByFileExtension = getFactoriesByFileExtensionMap(ioInterface);
            factoriesByFileExtension.put(fileExtension, factory);
            for (final String mediaType : factory.getMediaTypes()) {
              this.extensionMimeTypeMap.put(fileExtension.toLowerCase(),
                mediaType);
            }
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

  public Map<String, String> getExtensionMimeTypeMap() {
    return this.extensionMimeTypeMap;
  }

  @SuppressWarnings("unchecked")
  public synchronized <F extends IoFactory> Set<F> getFactories(
    final Class<? extends F> factoryClass) {
    if (factoryClass == null) {
      return Collections.emptySet();
    } else {
      Set<IoFactory> factories = this.classFactories.get(factoryClass);
      if (factories == null) {
        factories = new LinkedHashSet<IoFactory>();
        this.classFactories.put(factoryClass, factories);
      }
      return (Set<F>)factories;
    }
  }

  public Set<IoFactory> getFactoriesByClass() {
    return this.factories;
  }

  public <F extends IoFactory> List<F> getFactoriesByFileExtension(
    final Class<F> factoryClass, final List<String> fileExtensions) {
    if (fileExtensions == null) {
      return Collections.emptyList();
    } else {
      final Map<String, F> factoriesByFileExtension = getFactoriesByFileExtensionMap(factoryClass);
      final List<F> factories = new ArrayList<F>();
      for (final String fileExtension : fileExtensions) {
        final F factory = factoriesByFileExtension.get(fileExtension.toLowerCase());
        if (factory != null) {
          factories.add(factory);
        }
      }
      return factories;
    }
  }

  @SuppressWarnings("unchecked")
  public synchronized <F extends IoFactory> Map<String, F> getFactoriesByFileExtensionMap(
    final Class<F> factoryClass) {
    if (factoryClass == null) {
      return Collections.emptyMap();
    } else {
      Map<String, IoFactory> factoriesByFileExtension = this.classFactoriesByFileExtension.get(factoryClass);
      if (factoriesByFileExtension == null) {
        factoriesByFileExtension = new TreeMap<String, IoFactory>();
        this.classFactoriesByFileExtension.put(factoryClass,
          factoriesByFileExtension);
      }
      return (Map<String, F>)factoriesByFileExtension;
    }
  }

  @SuppressWarnings("unchecked")
  public synchronized <F extends IoFactory> Map<String, F> getFactoriesByMediaType(
    final Class<F> factoryClass) {
    if (factoryClass == null) {
      return Collections.emptyMap();
    } else {
      Map<String, IoFactory> factoriesByMediaType = this.classFactoriesByMediaType.get(factoryClass);
      if (factoriesByMediaType == null) {
        factoriesByMediaType = new TreeMap<String, IoFactory>();
        this.classFactoriesByMediaType.put(factoryClass, factoriesByMediaType);
      }
      return (Map<String, F>)factoriesByMediaType;
    }
  }

  public <F extends IoFactory> F getFactoryByFileExtension(
    final Class<F> factoryClass, final String fileExtension) {
    if (fileExtension == null) {
      return null;
    } else {
      final Map<String, F> factoriesByFileExtension = getFactoriesByFileExtensionMap(factoryClass);
      return factoriesByFileExtension.get(fileExtension.toLowerCase());
    }
  }

  public <F extends IoFactory> F getFactoryByFileName(
    final Class<F> factoryClass, final String fileName) {
    if (fileName == null) {
      return null;
    } else {
      for (final String fileExtension : FileUtil.getFileNameExtensions(fileName)) {
        final F factory = getFactoryByFileExtension(factoryClass, fileExtension);
        if (factory != null) {
          return factory;
        }
      }
      return null;
    }
  }

  public <F extends IoFactory> F getFactoryByMediaType(
    final Class<F> factoryClass, final String mediaType) {
    if (mediaType == null) {
      return null;
    } else if (mediaType.contains("/")) {
      final Map<String, F> factoriesByMediaType = getFactoriesByMediaType(factoryClass);
      return factoriesByMediaType.get(mediaType);
    } else {
      return getFactoryByFileExtension(factoryClass, mediaType);
    }
  }

  public <F extends IoFactory> F getFactoryByResource(
    final Class<F> factoryClass, final Resource resource) {
    String fileName;
    if (resource == null) {
      return null;
    } else if (resource instanceof UrlResource) {
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

  public Set<String> getFileExtensions(
    final Class<? extends IoFactory> factoryClass) {
    if (factoryClass == null) {
      return Collections.emptySet();
    } else {
      final Set<String> emptySet = Collections.<String> emptySet();
      return CollectionUtil.get(this.classFileExtensions, factoryClass,
        emptySet);
    }
  }

  public <F extends IoFactory> Set<String> getMediaTypes(
    final Class<F> factoryClass) {
    if (factoryClass == null) {
      return Collections.emptySet();
    } else {
      final Map<String, F> factoriesByMediaType = getFactoriesByMediaType(factoryClass);
      return factoriesByMediaType.keySet();
    }
  }

  public boolean isFileExtensionSupported(
    final Class<? extends IoFactory> factoryClass, final String fileExtension) {
    if (factoryClass == null || fileExtension == null) {
      return false;
    } else {
      return getFileExtensions(factoryClass).contains(
        fileExtension.toLowerCase());
    }
  }

  public void setFactories(final Set<IoFactory> factories) {
    this.classFactoriesByFileExtension.clear();
    this.classFactoriesByMediaType.clear();
    this.classFactories.clear();

    this.factories.clear();
    if (factories != null) {
      for (final IoFactory factory : factories) {
        addFactory(factory);
      }
    }
  }
}
