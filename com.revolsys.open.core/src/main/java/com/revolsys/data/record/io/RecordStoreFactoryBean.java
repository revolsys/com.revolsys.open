package com.revolsys.data.record.io;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.util.Property;

public class RecordStoreFactoryBean extends
AbstractFactoryBean<RecordStore> {

  private Map<String, Object> config = new LinkedHashMap<String, Object>();

  private Map<String, Object> properties = new LinkedHashMap<String, Object>();

  @Override
  protected RecordStore createInstance() throws Exception {
    final RecordStore recordStore = RecordStoreFactoryRegistry.createRecordStore(this.config);
    Property.set(recordStore, this.properties);
    recordStore.initialize();
    return recordStore;
  }

  @Override
  protected void destroyInstance(final RecordStore recordStore)
      throws Exception {
    recordStore.close();
    this.properties = null;
    this.config = null;
  }

  public Map<String, Object> getConfig() {
    return this.config;
  }

  @Override
  public Class<?> getObjectType() {
    return RecordStore.class;
  }

  public Map<String, Object> getProperties() {
    return this.properties;
  }

  public void setConfig(final Map<String, Object> config) {
    this.config = config;
  }

  public void setProperties(final Map<String, Object> properties) {
    this.properties = properties;
  }

  public void setUrl(final String url) {
    this.config.put("url", url);
  }
}
