package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcFactory {

  private static Map<String, Class<JdbcDataObjectStore>> dataObjectStoreClassNames = new HashMap<String, Class<JdbcDataObjectStore>>();

  private static Map<String, DataSourceFactory> dataSourceFactoriesByProductName = new HashMap<String, DataSourceFactory>();

  private static Map<Pattern, DataSourceFactory> dataSourceFactoryUrlPatterns = new HashMap<Pattern, DataSourceFactory>();

  public static final DataObjectFactory DEFAULT_DATA_OBJECT_FACTORY = new ArrayDataObjectFactory();

  static {
    new ClassPathXmlApplicationContext(
      "classpath*:META-INF/com.revolsys.gis.jdbc.sf.xml");
  }

  public static JdbcDataObjectStore createDataObjectStore(
    final Map<String, Object> config) {
    DataSource dataSource = createDataSource(config);
    return createDataObjectStore(dataSource);
  }

  public static JdbcDataObjectStore createDataObjectStore(
    final DataSource dataSource) {
    try {
      final Connection connection = JdbcUtils.getConnection(dataSource);
      try {
        final DatabaseMetaData metaData = connection.getMetaData();
        final String productName = metaData.getDatabaseProductName();
        final Class<JdbcDataObjectStore> dataObjectStoreClass = dataObjectStoreClassNames.get(productName);
        if (dataObjectStoreClass == null) {
          throw new IllegalArgumentException("Data Store not found for "
            + productName);
        } else {
          final JdbcDataObjectStore dataObjectStore = dataObjectStoreClass.newInstance();
          dataObjectStore.setDataSource(dataSource);
          dataObjectStore.initialize();
          return dataObjectStore;
        }
      } catch (final InstantiationException e) {
        throw new RuntimeException("Unable to instantiate", e);
      } catch (final IllegalAccessException e) {
        throw new RuntimeException("Unable to instantiate", e);
      } finally {
        JdbcUtils.close(connection);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to detect database version", e);
    }
  }

  public static JdbcDataObjectStore createDataObjectStore(
    final String connectionName, final String productName,
    final Map<String, Object> config) {
    final DataSource dataSource = createDataSource(productName, config);
    if (dataSource == null) {
      return null;
    } else {
      JdbcDataObjectStore dataObjectStore = createDataObjectStore(dataSource);
      dataObjectStore.setLabel(connectionName);
      return dataObjectStore;
    }
  }

  public static DataSource createDataSource(final String url,
    final String username, final String password, Map<String, Object> config) {
    Map<String, Object> newConfig = new HashMap<String, Object>(config);
    newConfig.put("url", url);
    newConfig.put("username", username);
    newConfig.put("password", password);
    return createDataSource(newConfig);
  }

  public static DataSource createDataSource(final String url,
    final String username, final String password) {
    final Map<String, Object> config = Collections.emptyMap();
    return createDataSource(url, username, password, config);
  }

  public static DataObjectStore createDataObjectStore(final String url,
    final String username, final String password, Map<String, Object> config) {
    Map<String, Object> newConfig = new HashMap<String, Object>(config);
    newConfig.put("url", url);
    newConfig.put("username", username);
    newConfig.put("password", password);
    return createDataObjectStore(newConfig);
  }

  public static DataObjectStore createDataObjectStore(final String url,
    final String username, final String password) {
    final Map<String, Object> config = Collections.emptyMap();
    return createDataObjectStore(url, username, password, config);
  }

  public static DataSource createDataSource(final String productName,
    final Map<String, Object> config) {
    try {
      final DataSourceFactory dataSourceFactory = dataSourceFactoriesByProductName.get(productName);
      if (dataSourceFactory == null) {
        throw new IllegalArgumentException("Data Source Factory not found for "
          + productName);
      } else {
        return dataSourceFactory.createDataSource(config);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to create data source", e);
    }
  }

  public static DataSource createDataSource(final Map<String, Object> config) {
    try {
      final String url = (String)config.get("url");
      DataSourceFactory dataSourceFactory = getDataSourceFactory(url);
      return dataSourceFactory.createDataSource(config);
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to create data source", e);
    }
  }

  public static DataSourceFactory getDataSourceFactory(final String url) {
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      for (Entry<Pattern, DataSourceFactory> entry : dataSourceFactoryUrlPatterns.entrySet()) {
        Pattern pattern = entry.getKey();
        DataSourceFactory dataSourceFactory = entry.getValue();
        if (pattern.matcher(url).matches()) {
          return dataSourceFactory;
        }
      }
      throw new IllegalArgumentException("Data Source Factory not found for "
        + url);
    }
  }

  @SuppressWarnings("unchecked")
  public static Class<?> register(final String productName,
    final String dataStoreClassName) {
    try {
      final Class<?> clazz = Class.forName(dataStoreClassName);
      if (JdbcDataObjectStore.class.isAssignableFrom(clazz)) {
        dataObjectStoreClassNames.put(productName,
          (Class<JdbcDataObjectStore>)clazz);
      }
      return clazz;
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to register " + productName, e);
    }
  }

  public static DataSourceFactory register(final String productName,
    DataSourceFactory dataSourceFactory, List<String> patterns) {
    dataSourceFactoriesByProductName.put(productName, dataSourceFactory);
    for (String regex : patterns) {
      dataSourceFactoryUrlPatterns.put(Pattern.compile(regex),
        dataSourceFactory);
    }
    return dataSourceFactory;
  }

  private DataObjectFactory dataObjectFactory;

  private DataSource dataSource;

  public JdbcFactory() {
    this(DEFAULT_DATA_OBJECT_FACTORY);
  }

  public JdbcFactory(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public JdbcFactory(final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this.dataObjectFactory = dataObjectFactory;
    setDataSource(dataSource);
  }

  public JdbcFactory(final DataSource dataSource) {
    this(DEFAULT_DATA_OBJECT_FACTORY);
    setDataSource(dataSource);
  }

  public JdbcDataObjectStore createDataObjectStore() {
    return createDataObjectStore(getDataSource());
  }

  public JdbcWriter createWriter() {
    final JdbcDataObjectStore dataObjectStore = createDataObjectStore();
    return dataObjectStore.createWriter();
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataObjectFactory(final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  private void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
