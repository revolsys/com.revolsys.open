package com.revolsys.gis.data.io;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.revolsys.util.JavaBeanUtil;

public class DataObjectStoreFactoryBean extends
  AbstractFactoryBean<DataObjectStore> {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  @Override
  protected DataObjectStore createInstance() throws Exception {
    final DataObjectStore dataObjectStore = DataObjectStoreFactoryRegistry.createDataObjectStore(config);
    JavaBeanUtil.setProperties(dataObjectStore, properties);
    dataObjectStore.initialize();
    return dataObjectStore;
  }

  @Override
  protected void destroyInstance(final DataObjectStore dataObjectStore)
    throws Exception {
    dataObjectStore.close();
    properties = null;
    config = null;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  @Override
  public Class<?> getObjectType() {
    return DataObjectStore.class;
  }

  public Map<String, Object> getProperties() {
    return properties;
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
}
