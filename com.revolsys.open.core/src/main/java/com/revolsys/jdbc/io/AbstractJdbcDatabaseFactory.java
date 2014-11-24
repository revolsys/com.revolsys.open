package com.revolsys.jdbc.io;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.LoggerFactory;

import com.revolsys.util.CollectionUtil;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.PasswordUtil;

public abstract class AbstractJdbcDatabaseFactory implements
  JdbcDatabaseFactory {

  private boolean useCommonsDbcp = true;

  @Override
  public void closeDataSource(final DataSource dataSource) {
    if (dataSource instanceof BasicDataSource) {
      final BasicDataSource basicDataSource = (BasicDataSource)dataSource;
      try {
        basicDataSource.close();
      } catch (final SQLException e) {
      }
    }
  }

  @Override
  public DataSource createDataSource(final Map<String, ? extends Object> config) {
    try {
      final Map<String, Object> newConfig = new HashMap<String, Object>(config);
      final String url = (String)newConfig.remove("url");
      final String username = (String)newConfig.remove("username");
      String password = (String)newConfig.remove("password");
      password = PasswordUtil.decrypt(password);
      final BasicDataSource dataSource = new BasicDataSource();
      dataSource.setDriverClassName(getDriverClassName());
      dataSource.setUsername(username);
      dataSource.setPassword(password);
      dataSource.setUrl(url);
      dataSource.setValidationQuery(getConnectionValidationQuery());

      final int minPoolSize = CollectionUtil.getInteger(config, "minPoolSize",
        -1);
      newConfig.remove("minPoolSize");
      dataSource.setMinIdle(minPoolSize);
      dataSource.setMaxIdle(-1);

      final int maxPoolSize = CollectionUtil.getInteger(config, "maxPoolSize",
        10);
      newConfig.remove("maxPoolSize");
      dataSource.setMaxTotal(maxPoolSize);

      final int maxWaitMillis = CollectionUtil.getInteger(config,
        "waitTimeout", 1);
      newConfig.remove("waitTimeout");
      dataSource.setMaxWaitMillis(maxWaitMillis);

      final boolean validateConnection = CollectionUtil.getBool(config,
        "validateConnection", true);
      newConfig.remove("validateConnection");
      dataSource.setTestOnBorrow(validateConnection);

      final int inactivityTimeout = CollectionUtil.getInteger(config,
        "inactivityTimeout", 60);
      newConfig.remove("inactivityTimeout");
      dataSource.setMinEvictableIdleTimeMillis(inactivityTimeout * 1000);
      dataSource.setTimeBetweenEvictionRunsMillis(inactivityTimeout * 1000);

      for (final Entry<String, Object> property : newConfig.entrySet()) {
        final String name = property.getKey();
        final Object value = property.getValue();
        try {
          JavaBeanUtil.setProperty(dataSource, name, value);
        } catch (final Throwable t) {
          LoggerFactory.getLogger(getClass()).debug(
            "Unable to set data source property " + name + " = " + value
              + " for " + url, t);
        }
      }
      return dataSource;
    } catch (final Throwable e) {
      throw new IllegalArgumentException("Unable to create data source for "
        + config, e);
    }
  }

  public String getConnectionValidationQuery() {
    return "SELECT 1";
  }

  public abstract String getDriverClassName();

  public boolean isUseCommonsDbcp() {
    return this.useCommonsDbcp;
  }

  public void setUseCommonsDbcp(final boolean useCommonsDbcp) {
    this.useCommonsDbcp = useCommonsDbcp;
  }
}
