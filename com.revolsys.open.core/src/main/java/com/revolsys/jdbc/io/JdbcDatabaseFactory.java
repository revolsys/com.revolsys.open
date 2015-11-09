package com.revolsys.jdbc.io;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.IoFactory;
import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.PasswordUtil;

public interface JdbcDatabaseFactory extends RecordStoreFactory {
  static List<JdbcDatabaseFactory> databaseFactories() {
    return IoFactory.factories(JdbcDatabaseFactory.class);
  }

  static JdbcDatabaseFactory databaseFactory(final DataSource dataSource) {
    final String productName = JdbcUtils.getProductName(dataSource);
    return databaseFactory(productName);
  }

  static JdbcDatabaseFactory databaseFactory(final Map<String, ? extends Object> config) {
    final String url = (String)config.get("url");
    if (url == null) {
      throw new IllegalArgumentException("The url parameter must be specified");
    } else {
      for (final JdbcDatabaseFactory databaseFactory : databaseFactories()) {
        if (databaseFactory.canOpenUrl(url)) {
          return databaseFactory;
        }
      }
      throw new IllegalArgumentException("Database factory not found for " + url);
    }
  }

  static JdbcDatabaseFactory databaseFactory(final String productName) {
    for (final JdbcDatabaseFactory databaseFactory : databaseFactories()) {
      if (databaseFactory.getProductName().equals(productName)) {
        return databaseFactory;
      }
    }
    return null;
  }

  @Override
  default boolean canOpenPath(final Path path) {
    return false;
  }

  @Override
  default boolean canOpenUrl(final String url) {
    if (url.startsWith("jdbc:" + getVendorName() + ":")) {
      return true;
    } else {
      return false;
    }
  }

  default void closeDataSource(final DataSource dataSource) {
    if (dataSource instanceof DataSourceImpl) {
      final DataSourceImpl basicDataSource = (DataSourceImpl)dataSource;
      try {
        basicDataSource.close();
      } catch (final SQLException e) {
      }
    }
  }

  /**
   * Get  the map from connection name to JDBC URL for the database driver. For
   * example in Oracle this will be connections loaded from the TNSNAMES.ora file.
   * @return
   */
  default Map<String, String> getConnectionUrlMap() {
    return Collections.emptyMap();
  }

  default String getConnectionValidationQuery() {
    return "SELECT 1";
  }

  String getDriverClassName();

  String getProductName();

  @Override
  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  @Override
  default List<Pattern> getUrlPatterns() {
    return Collections.singletonList(Pattern.compile("jdbc:" + getVendorName() + ":.+"));
  }

  String getVendorName();

  @Override
  default boolean isAvailable() {
    return true;
  }

  default DataSource newDataSource(final Map<String, ? extends Object> config) {
    try {
      final Map<String, Object> newConfig = new HashMap<>(config);
      final String url = (String)newConfig.remove("url");
      final String user = (String)newConfig.remove("user");
      String password = (String)newConfig.remove("password");
      password = PasswordUtil.decrypt(password);
      final DataSourceImpl dataSource = new DataSourceImpl();
      dataSource.setDriverClassName(getDriverClassName());
      dataSource.setUsername(user);
      dataSource.setPassword(password);
      dataSource.setUrl(url);
      dataSource.setValidationQuery(getConnectionValidationQuery());

      final int minPoolSize = Maps.getInteger(config, "minPoolSize", -1);
      newConfig.remove("minPoolSize");
      dataSource.setMinIdle(minPoolSize);
      dataSource.setMaxIdle(-1);

      final int maxPoolSize = Maps.getInteger(config, "maxPoolSize", 10);
      newConfig.remove("maxPoolSize");
      dataSource.setMaxTotal(maxPoolSize);

      final int maxWaitMillis = Maps.getInteger(config, "waitTimeout", 10);
      newConfig.remove("waitTimeout");
      dataSource.setMaxWaitMillis(maxWaitMillis);

      final boolean validateConnection = Maps.getBool(config, "validateConnection", true);
      newConfig.remove("validateConnection");
      // dataSource.setTestOnBorrow(validateConnection);

      final int inactivityTimeout = Maps.getInteger(config, "inactivityTimeout", 60);
      newConfig.remove("inactivityTimeout");
      dataSource.setMinEvictableIdleTimeMillis(inactivityTimeout * 1000);
      dataSource.setTimeBetweenEvictionRunsMillis(inactivityTimeout * 1000);

      for (final Entry<String, Object> property : newConfig.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          JavaBeanUtil.setProperty(dataSource, name, value);
        } catch (final Throwable t) {
          LoggerFactory.getLogger(getClass())
            .debug("Unable to set data source property " + name + " = " + value + " for " + url, t);
        }
      }
      return dataSource;
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to create data source for " + config, e);
    }
  }

  JdbcRecordStore newRecordStore(DataSource dataSource);

  @Override
  JdbcRecordStore newRecordStore(Map<String, ? extends Object> connectionProperties);

  Map<String, Object> parseJdbcUrl(String url);
}
