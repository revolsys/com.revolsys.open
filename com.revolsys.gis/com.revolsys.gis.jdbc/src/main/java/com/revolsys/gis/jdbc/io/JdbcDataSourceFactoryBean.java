package com.revolsys.gis.jdbc.io;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import com.revolsys.jdbc.JdbcUtils;

public class JdbcDataSourceFactoryBean implements FactoryBean<DataSource> {

  private Map<String, Object> config = new HashMap<String, Object>();

  private String url;

  private String username;

  private String password;

  private WeakReference<DataSource> dataSourceReference;

  private DataSourceFactory dataSourceFactory;
  public Map<String, Object> getConfig() {
    return config;
  }

  public DataSource getObject() throws Exception {
    Map<String, Object> config = new HashMap<String, Object>(this.config);
    config.put("url", url);
    config.put("username", username);
    config.put("password", password);
    if (dataSourceReference == null) {
       dataSourceFactory = JdbcFactory.getDataSourceFactory(url);
      DataSource dataSource = dataSourceFactory.createDataSource(config);
      dataSourceReference = new WeakReference<DataSource>(dataSource); 
    }
    return dataSourceReference.get();
    
  }

  public void close() {
    DataSource dataSource = dataSourceReference.get();
    if (dataSource != null) {
      dataSourceFactory.closeDataSource(dataSource);
    }
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
