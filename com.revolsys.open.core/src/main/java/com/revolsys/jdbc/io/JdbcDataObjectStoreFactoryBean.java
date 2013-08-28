package com.revolsys.jdbc.io;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.revolsys.util.Property;

public class JdbcDataObjectStoreFactoryBean extends
  AbstractFactoryBean<JdbcDataObjectStore> implements ApplicationContextAware {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  private DataSource dataSource;

  private ApplicationContext applicationContext;

  @Override
  protected JdbcDataObjectStore createInstance() throws Exception {
    JdbcDataObjectStore dataObjectStore;
    final JdbcFactoryRegistry jdbcFactoryRegistry = JdbcFactoryRegistry.getFactory(applicationContext);
    if (dataSource == null) {
      final JdbcDatabaseFactory databaseFactory = jdbcFactoryRegistry.getDatabaseFactory(config);
      dataObjectStore = databaseFactory.createDataObjectStore(config);
    } else {
      final JdbcDatabaseFactory databaseFactory = jdbcFactoryRegistry.getDatabaseFactory(dataSource);
      dataObjectStore = databaseFactory.createDataObjectStore(dataSource);
    }
    Property.set(dataObjectStore, properties);
    dataObjectStore.initialize();
    return dataObjectStore;
  }

  @Override
  protected void destroyInstance(final JdbcDataObjectStore dataObjectStore)
    throws Exception {
    dataObjectStore.close();
    config = null;
    dataSource = null;
    properties = null;
    applicationContext = null;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public Class<?> getObjectType() {
    return JdbcDataObjectStore.class;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext)
    throws BeansException {
    this.applicationContext = applicationContext;
  }

  public void setConfig(final Map<String, Object> config) {
    this.config = config;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setProperties(final Map<String, Object> properties) {
    this.properties = properties;
  }
}
