package com.revolsys.gis.jdbc.io;

import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;

public class LazyJdbcDataObjectStore extends JdbcDelegatingDataObjectStore {
  private String label;

  private Map<String, Object> config;

  public LazyJdbcDataObjectStore(String label, Map<String, Object> config) {
    this.label = label;
    this.config = config;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  @Override
  protected DataObjectStore createDataObjectStore() {
    JdbcDataObjectStore dataStore = JdbcFactory.createDataObjectStore(config);
    dataStore.setLabel(label);
    return dataStore;
  }

}
