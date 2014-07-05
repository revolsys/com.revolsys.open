package com.revolsys.data.io;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.revolsys.util.Property;

public class RecordStoreFactoryBean extends
  AbstractFactoryBean<RecordStore> {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  @Override
  protected RecordStore createInstance() throws Exception {
    final RecordStore recordStore = RecordStoreFactoryRegistry.createRecordStore(config);
    Property.set(recordStore, properties);
    recordStore.initialize();
    return recordStore;
  }

  @Override
  protected void destroyInstance(final RecordStore recordStore)
    throws Exception {
    recordStore.close();
    properties = null;
    config = null;
  }

  public Map<String, Object> getConfig() {
    return config;
  }

  @Override
  public Class<?> getObjectType() {
    return RecordStore.class;
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
