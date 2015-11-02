package com.revolsys.jdbc.io;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.PasswordUtil;

public interface JdbcDatabaseFactory extends RecordStoreFactory {
  boolean canHandleUrl(String url);

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

  List<String> getProductNames();

  @Override
  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  @Override
  List<String> getUrlPatterns();

  @Override
  default boolean isAvailable() {
    return true;
  }

  default DataSource newDataSource(final Map<String, ? extends Object> config) {
    try {
      final Map<String, Object> newConfig = new HashMap<>(config);
      final String url = (String)newConfig.remove("url");
      final String username = (String)newConfig.remove("username");
      String password = (String)newConfig.remove("password");
      password = PasswordUtil.decrypt(password);
      final DataSourceImpl dataSource = new DataSourceImpl();
      dataSource.setDriverClassName(getDriverClassName());
      dataSource.setUsername(username);
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
}
