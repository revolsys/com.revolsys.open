package com.revolsys.gis.oracle.io;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.jdbc.io.JdbcDataObjectStore;
import com.revolsys.jdbc.io.JdbcDatabaseFactory;
import com.revolsys.util.JavaBeanUtil;

public class OracleDatabaseFactory implements JdbcDatabaseFactory {
  public static final String URL_REGEX = "jdbc:oracle:thin:(?:([^/]+)(?:/([^@]+))?)?@(?://)?([^:]+)(?::([^:]+))[:/](.+)";

  public static final List<String> URL_PATTERNS = Arrays.asList(URL_REGEX);

  private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

  public boolean canHandleUrl(final String url) {
    final Matcher urlMatcher = URL_PATTERN.matcher(url);
    return urlMatcher.matches();
  }

  public void closeDataSource(final DataSource dataSource) {
    if (dataSource instanceof OracleDataSource) {
      final OracleDataSource oracleDataSource = (OracleDataSource)dataSource;
      try {
        oracleDataSource.close();
      } catch (final SQLException e) {
        LoggerFactory.getLogger(OracleDatabaseFactory.class).warn(
          "Unable to close data source", e);
      }
    }
  }

  // public void closeDataSource(final DataSource dataSource) {
  // if (dataSource instanceof PoolDataSource) {
  // try {
  // UniversalConnectionPoolManager poolManager =
  // UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
  //
  // final PoolDataSource oracleDataSource = (PoolDataSource)dataSource;
  // String connectionPoolName = oracleDataSource.getConnectionPoolName();
  // poolManager.stopConnectionPool(connectionPoolName);
  // poolManager.destroyConnectionPool(connectionPoolName);
  // } catch (final UniversalConnectionPoolException e) {
  // LoggerFactory.getLogger(OracleDatabaseFactory.class).warn(
  // "Unable to close data source", e);
  // }
  // }
  // }

  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    return new OracleDataObjectStore(this, connectionProperties);
  }

  public JdbcDataObjectStore createDataObjectStore(final DataSource dataSource) {
    return new OracleDataObjectStore(dataSource);
  }

  public DataSource createDataSource(final Map<String, ? extends Object> config) {
    try {
      final Map<String, Object> newConfig = new HashMap<String, Object>(config);
      Properties cacheProperties = new Properties();
      final String url = (String)newConfig.remove("url");
      final String username = (String)newConfig.remove("username");
      final String password = (String)newConfig.remove("password");
      Object minimumPoolSize = newConfig.remove("minPoolSize");
      if (minimumPoolSize != null) {
        final int minPoolSize = (Integer)StringConverterRegistry.toObject(
          DataTypes.INT, minimumPoolSize);
        cacheProperties.put("MinLimit", minPoolSize);
      }
      Object maximumPoolSize = newConfig.remove("minPoolSize");
      if (maximumPoolSize != null) {
        final int maxPoolSize = (Integer)StringConverterRegistry.toObject(
          DataTypes.INT, maximumPoolSize);
        cacheProperties.put("MaxLimit", maxPoolSize);
      }
      cacheProperties.put("InactivityTimeout", 300);
      final OracleDataSource dataSource = new OracleDataSource();

      dataSource.setConnectionCachingEnabled(true);
      dataSource.setConnectionCacheProperties(cacheProperties);
      for (final Entry<String, Object> property : newConfig.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          JavaBeanUtil.setProperty(dataSource, name, value);
        } catch (final Throwable e) {
          LoggerFactory.getLogger(OracleDatabaseFactory.class).debug(
            "Unable to set Oracle data source property " + name, e);
        }
      }
      dataSource.setURL(url);
      dataSource.setUser(username);
      dataSource.setPassword(password);

      return dataSource;
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to create data source for "
        + config, e);
    }
  }

  // public DataSource createDataSource(final Map<String, ? extends Object>
  // config) {
  // try {
  // UniversalConnectionPoolManager poolManager =
  // UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
  // poolManager.setJmxEnabled(false);
  //
  // final Map<String, Object> newConfig = new HashMap<String, Object>(config);
  // final String url = (String)newConfig.remove("url");
  // final String username = (String)newConfig.remove("username");
  // final String password = (String)newConfig.remove("password");
  // PoolDataSource dataSource = PoolDataSourceFactory.getPoolDataSource();
  // dataSource.setMaxConnectionReuseTime(300);
  // dataSource.setInactiveConnectionTimeout(300);
  //
  // dataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleConnectionPoolDataSource");
  //
  // for (final MapKeyEntry<String, Object> property : newConfig.entrySet()) {
  // final String name = property.getKey();
  // final Object value = property.getValue();
  // try {
  // JavaBeanUtil.setProperty(dataSource, name, value);
  // } catch (final Throwable e) {
  // LoggerFactory.getLogger(OracleDatabaseFactory.class).debug(
  // "Unable to set Oracle data source property " + name, e);
  // }
  // }
  // dataSource.setURL(url);
  // dataSource.setUser(username);
  // dataSource.setPassword(password);
  //
  // poolManager.createConnectionPool((UniversalConnectionPoolAdapter)dataSource);
  // String connectionPoolName = dataSource.getConnectionPoolName();
  // poolManager.startConnectionPool(connectionPoolName);
  // return dataSource;
  // } catch (final Throwable e) {
  // throw new IllegalArgumentException("Unable to create data source for "
  // + config, e);
  // }
  // }

  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcDataObjectStore.class;
  }

  public List<String> getProductNames() {
    return Collections.singletonList("Oracle");
  }

  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }
}
