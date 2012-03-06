package com.revolsys.jdbc.io;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

public class JdbcDataSourceFactoryBean implements FactoryBean<DataSource> {

  private Map<String, Object> config = new HashMap<String, Object>();

  private String url;

  private String username;

  private String password;

  private DataSource dataSource;

  private DataSourceFactory dataSourceFactory;

  @PreDestroy
  public void close() {
    if (dataSource != null) {
      try {
        if (dataSource != null) {
          dataSourceFactory.closeDataSource(dataSource);
        }
        dataSource = null;
      } finally {
        config = null;
        dataSourceFactory = null;
        dataSource = null;
        password = null;
        url = null;
        username = null;
      }
    }
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public DataSource getObject() throws Exception {
    final Map<String, Object> config = new HashMap<String, Object>(this.config);
    config.put("url", url);
    config.put("username", username);
    config.put("password", password);
    if (dataSource == null) {
      dataSourceFactory = JdbcFactory.getDataSourceFactory(url);
      dataSource = dataSourceFactory.createDataSource(config);
    }
    return dataSource;
  }

  public Class<?> getObjectType() {
    return DataSource.class;
  }

  public String getPassword() {
    return password;
  }

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setConfig(final Map<String, Object> config) {
    this.config = config;
  }

  @Required
  public void setPassword(final String password) {
    this.password = password;
  }

  @Required
  public void setUrl(final String url) {
    this.url = url;
  }

  @Required
  public void setUsername(final String username) {
    this.username = username;
  }
}
