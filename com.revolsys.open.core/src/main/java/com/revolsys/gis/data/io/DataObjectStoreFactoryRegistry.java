package com.revolsys.gis.data.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DataObjectStoreFactoryRegistry {

  private static Map<Pattern, DataObjectStoreFactory> dataStoreFactoryUrlPatterns = new HashMap<Pattern, DataObjectStoreFactory>();

  static {
    new ClassPathXmlApplicationContext(
      "classpath*:META-INF/com.revolsys.gis.dataStore.sf.xml");
  }

  private static final Logger LOG = LoggerFactory.getLogger(DataObjectStoreFactoryRegistry.class);

  public static <T extends DataObjectStore> T createDataObjectStore(
    final Map<String, Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final DataObjectStoreFactory factory = getDataSourceFactory(url);
    return (T)factory.createDataObjectStore(connectionProperties);
  }

  public static <T extends DataObjectStore> T createDataObjectStore(
    final String url) {
    final DataObjectStoreFactory factory = getDataSourceFactory(url);
    Map<String, Object> connectionProperties = new HashMap<String, Object>();
    connectionProperties.put("url", url);
    return (T)factory.createDataObjectStore(connectionProperties);
  }

  public static Class<?> getDataObjectStoreInterfaceClass(
    final Map<String, Object> connectionProperties) {
    final String url = (String)connectionProperties.get("url");
    final DataObjectStoreFactory factory = getDataSourceFactory(url);
    return factory.getDataObjectStoreInterfaceClass(connectionProperties);
  }

  public static DataObjectStoreFactory getDataSourceFactory(final String url) {
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      for (final Entry<Pattern, DataObjectStoreFactory> entry : dataStoreFactoryUrlPatterns.entrySet()) {
        final Pattern pattern = entry.getKey();
        final DataObjectStoreFactory factory = entry.getValue();
        if (pattern.matcher(url).matches()) {
          return factory;
        }
      }
      throw new IllegalArgumentException("Data Source Factory not found for "
        + url);
    }
  }

  public static DataObjectStoreFactory register(
    final DataObjectStoreFactory factory) {
    final List<String> patterns = factory.getUrlPatterns();
    for (final String regex : patterns) {
      final Pattern pattern = Pattern.compile(regex);
      dataStoreFactoryUrlPatterns.put(pattern, factory);
    }
    return factory;
  }

  public static void setConnectionProperties(
    final DataObjectStore dataObjectStore,
    final Map<String, Object> properties) {
    final DirectFieldAccessor dataSourceBean = new DirectFieldAccessor(
      dataObjectStore);
    for (final Entry<String, Object> property : properties.entrySet()) {
      final String name = property.getKey();
      final Object value = property.getValue();
      try {
        dataSourceBean.setPropertyValue(name, value);
      } catch (final Throwable e) {
        LOG.error("Unable to set data store property " + name, e);
      }
    }
  }

}
