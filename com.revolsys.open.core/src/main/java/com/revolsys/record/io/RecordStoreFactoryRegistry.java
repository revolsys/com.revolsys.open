package com.revolsys.record.io;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreFactoryRegistry {

  private static Set<String> fileExtensions = new TreeSet<String>();

  private static List<RecordStoreFactory> fileRecordStoreFactories = new ArrayList<>();

  private static Map<Pattern, RecordStoreFactory> recordStoreFactoryUrlPatterns = new HashMap<>();

  static {
    try {
      final Enumeration<URL> resources = RecordStoreFactoryRegistry.class.getClassLoader()
        .getResources("META-INF/com.revolsys.gis.recordStore.sf.xml");
      while (resources.hasMoreElements()) {
        final URL url = resources.nextElement();
        try {
          new ClassPathXmlApplicationContext(url.toString());
        } catch (final Throwable e) {
          LoggerFactory.getLogger(RecordStoreFactoryRegistry.class)
            .error("Unable to initialize plugin: " + url, e);
        }

      }
    } catch (final IOException e) {
      LoggerFactory.getLogger(RecordStoreFactoryRegistry.class)
        .error("Unable to initialize plugins", e);
    }
    IoFactoryRegistry.getInstance();
  }

  public static Set<String> getFileExtensions() {
    return fileExtensions;
  }

  public static List<RecordStoreFactory> getFileRecordStoreFactories() {
    return Collections.unmodifiableList(fileRecordStoreFactories);
  }

  public static RecordStoreFactory getRecordStoreFactory(final String url) {
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      for (final Entry<Pattern, RecordStoreFactory> entry : recordStoreFactoryUrlPatterns
        .entrySet()) {
        final Pattern pattern = entry.getKey();
        final RecordStoreFactory factory = entry.getValue();
        if (pattern.matcher(url).matches()) {
          return factory;
        }
      }
      return null;
    }
  }

  public static Class<?> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final RecordStoreFactory factory = getRecordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Data Source Factory not found for " + url);
    } else {
      return factory.getRecordStoreInterfaceClass(connectionProperties);
    }
  }

  public static boolean isRecordStore(final Path path) {
    final String fileExtension = Paths.getFileNameExtension(path);
    if (fileExtensions.contains(fileExtension)) {
      for (final RecordStoreFactory recordStoreFactory : fileRecordStoreFactories) {
        if (recordStoreFactory.canOpen(path)) {
          return true;
        }
      }
    }
    return false;
  }

  public static <T extends RecordStore> T newRecordStore(final File file) {
    return newRecordStore(FileUtil.toUrlString(file));
  }

  public static <T extends RecordStore> T newRecordStore(final File directory,
    final String fileExtension) {
    if (!directory.exists()) {
      throw new IllegalArgumentException("Directory does not exist: " + directory);
    } else if (!directory.isDirectory()) {
      throw new IllegalArgumentException("File is not a directory: " + directory);
    } else {
      final String url = FileUtil.toUrlString(directory) + "?format=" + fileExtension;
      return newRecordStore(url);
    }
  }

  /**
   * Construct a newn initialized record store.
   * @param connectionProperties
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T newRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final RecordStoreFactory factory = getRecordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Record Store Factory not found for " + url);
    } else {
      return (T)factory.newRecordStore(connectionProperties);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T newRecordStore(final String url) {
    final RecordStoreFactory factory = getRecordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Record Store Factory not found for " + url);
    } else {
      final Map<String, Object> connectionProperties = new HashMap<String, Object>();
      connectionProperties.put("url", url);
      return (T)factory.newRecordStore(connectionProperties);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T newRecordStore(final String url, final String username,
    final String password) {
    final RecordStoreFactory factory = getRecordStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Record Store Factory not found for " + url);
    } else {
      final Map<String, Object> connectionProperties = new HashMap<String, Object>();
      connectionProperties.put("url", url);
      connectionProperties.put("username", username);
      connectionProperties.put("password", password);
      return (T)factory.newRecordStore(connectionProperties);
    }
  }

  public static RecordStoreFactory register(final RecordStoreFactory factory) {
    if (factory != null && factory.isAvailable()) {
      final List<String> patterns = factory.getUrlPatterns();
      for (final String regex : patterns) {
        final Pattern pattern = Pattern.compile(regex);
        recordStoreFactoryUrlPatterns.put(pattern, factory);
      }
      final List<String> factoryFileExtensions = factory.getRecordStoreFileExtensions();
      if (!factoryFileExtensions.isEmpty()) {
        fileExtensions.addAll(factoryFileExtensions);
        fileRecordStoreFactories.add(factory);
      }
    }
    return factory;
  }

  public static void setConnectionProperties(final RecordStore recordStore,
    final Map<String, Object> properties) {
    final DirectFieldAccessor dataSourceBean = new DirectFieldAccessor(recordStore);
    for (final Entry<String, Object> property : properties.entrySet()) {
      final String name = property.getKey();
      final Object value = property.getValue();
      try {
        dataSourceBean.setPropertyValue(name, value);
      } catch (final Throwable e) {
      }
    }
  }
}
