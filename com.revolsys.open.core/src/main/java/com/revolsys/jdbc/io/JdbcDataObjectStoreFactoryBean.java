package com.revolsys.jdbc.io;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.util.JavaBeanUtil;

public class JdbcDataObjectStoreFactoryBean implements
  FactoryBean<DataObjectStore>, ApplicationContextAware {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  private DataSource dataSource;

  private ApplicationContext applicationContext;

  public void setApplicationContext(ApplicationContext applicationContext)
    throws BeansException {
    this.applicationContext = applicationContext;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private JdbcDataObjectStore dataObjectStore;

  public JdbcDataObjectStore getObject() throws Exception {
    if (dataObjectStore == null) {
      final JdbcFactoryRegistry jdbcFactoryRegistry = JdbcFactoryRegistry.getFactory(applicationContext);
      if (dataSource == null) {
        final JdbcDatabaseFactory databaseFactory = jdbcFactoryRegistry.getDatabaseFactory(config);
        dataObjectStore = databaseFactory.createDataObjectStore(config);
      } else {
        final JdbcDatabaseFactory databaseFactory = jdbcFactoryRegistry.getDatabaseFactory(dataSource);
        dataObjectStore = databaseFactory.createDataObjectStore(dataSource);
      }
      JavaBeanUtil.setProperties(dataObjectStore, properties);
      dataObjectStore.initialize();
    }
    return dataObjectStore;
  }

  public Class<?> getObjectType() {
    return JdbcDataObjectStore.class;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public boolean isSingleton() {
    return true;
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

  @PreDestroy
  public void destroy() {
    if (dataObjectStore != null) {
      dataObjectStore.close();
      dataObjectStore = null;
    }
  }

}
