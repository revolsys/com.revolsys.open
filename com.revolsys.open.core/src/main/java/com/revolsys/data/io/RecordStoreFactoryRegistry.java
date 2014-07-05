package com.revolsys.data.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RecordStoreFactoryRegistry {

  private static Map<Pattern, RecordStoreFactory> dataStoreFactoryUrlPatterns = new HashMap<Pattern, RecordStoreFactory>();

  private static List<RecordStoreFactory> fileDataStoreFactories = new ArrayList<RecordStoreFactory>();

  private static Set<String> fileExtensions = new TreeSet<String>();

  static {
    new ClassPathXmlApplicationContext(
      "classpath*:META-INF/com.revolsys.gis.dataStore.sf.xml");
  }

  /**
   * Create an initialized data store.
   * @param connectionProperties
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T createRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final RecordStoreFactory factory = getDataStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Data Source Factory not found for "
        + url);
    } else {
      return (T)factory.createRecordStore(connectionProperties);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T createRecordStore(
    final String url) {
    final RecordStoreFactory factory = getDataStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Data Source Factory not found for "
        + url);
    } else {
      final Map<String, Object> connectionProperties = new HashMap<String, Object>();
      connectionProperties.put("url", url);
      return (T)factory.createRecordStore(connectionProperties);
    }
  }

  public static Class<?> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final RecordStoreFactory factory = getDataStoreFactory(url);
    if (factory == null) {
      throw new IllegalArgumentException("Data Source Factory not found for "
        + url);
    } else {
      return factory.getRecordStoreInterfaceClass(connectionProperties);
    }
  }

  public static RecordStoreFactory getDataStoreFactory(final String url) {
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      for (final Entry<Pattern, RecordStoreFactory> entry : dataStoreFactoryUrlPatterns.entrySet()) {
        final Pattern pattern = entry.getKey();
        final RecordStoreFactory factory = entry.getValue();
        if (pattern.matcher(url).matches()) {
          return factory;
        }
      }
      return null;
    }
  }

  public static List<RecordStoreFactory> getFileDataStoreFactories() {
    return Collections.unmodifiableList(fileDataStoreFactories);
  }

  public static Set<String> getFileExtensions() {
    return fileExtensions;
  }

  public static RecordStoreFactory register(
    final RecordStoreFactory factory) {
    final List<String> patterns = factory.getUrlPatterns();
    for (final String regex : patterns) {
      final Pattern pattern = Pattern.compile(regex);
      dataStoreFactoryUrlPatterns.put(pattern, factory);
    }
    final List<String> factoryFileExtensions = factory.getFileExtensions();
    if (!factoryFileExtensions.isEmpty()) {
      fileExtensions.addAll(factoryFileExtensions);
      fileDataStoreFactories.add(factory);
    }
    return factory;
  }

  public static void setConnectionProperties(
    final RecordStore dataObjectStore, final Map<String, Object> properties) {
    final DirectFieldAccessor dataSourceBean = new DirectFieldAccessor(
      dataObjectStore);
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
