package com.revolsys.gis.data.io;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.FactoryBean;

import com.revolsys.util.JavaBeanUtil;

public class DataObjectStoreFactoryBean implements FactoryBean<DataObjectStore> {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  private DataObjectStore dataObjectStore;

  public Map<String, Object> getConfig() {
    return config;
  }

  public DataObjectStore getObject() throws Exception {
    if (dataObjectStore == null) {
      dataObjectStore = DataObjectStoreFactoryRegistry.createDataObjectStore(config);
      JavaBeanUtil.setProperties(dataObjectStore, properties);
      dataObjectStore.initialize();
    }
    return dataObjectStore;
  }

  public Class<?> getObjectType() {
    return DataObjectStore.class;
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

  public void setProperties(final Map<String, Object> properties) {
    this.properties = properties;
  }

  public void setUrl(final String url) {
    config.put("url", url);
  }

  @PreDestroy
  public void destroy() {
    if (dataObjectStore != null) {
      dataObjectStore.close();
      dataObjectStore = null;
    }
  }
}
