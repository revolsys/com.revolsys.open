package com.revolsys.jdbc.io;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JdbcDataSourceFactoryBean extends AbstractFactoryBean<DataSource>
  implements ApplicationContextAware {

  private Map<String, Object> config = new HashMap<String, Object>();

  private String url;

  private String username;

  private String password;

  private JdbcDatabaseFactory databaseFactory;

  private ApplicationContext applicationContext;

  public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  protected void destroyInstance(DataSource dataSource) throws Exception {
    try {
      databaseFactory.closeDataSource(dataSource);
    } finally {
      config = null;
      databaseFactory = null;
      password = null;
      url = null;
      username = null;
      applicationContext = null;
    }
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  @Override
  protected DataSource createInstance() throws Exception {
    final Map<String, Object> config = new HashMap<String, Object>(this.config);
    config.put("url", url);
    config.put("username", username);
    config.put("password", password);
    final JdbcFactoryRegistry jdbcFactoryRegistry = JdbcFactoryRegistry.getFactory(applicationContext);
    databaseFactory = jdbcFactoryRegistry.getDatabaseFactory(config);
    DataSource dataSource = databaseFactory.createDataSource(config);
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
