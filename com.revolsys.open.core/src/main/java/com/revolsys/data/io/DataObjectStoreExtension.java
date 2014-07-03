package com.revolsys.data.io;

import java.util.Map;

public interface DataObjectStoreExtension {

  public abstract void initialize(DataObjectStore dataStore,
    Map<String, Object> connectionProperties);

  boolean isEnabled(DataObjectStore dataStore);

  public abstract void postProcess(DataObjectStoreSchema schema);

  public abstract void preProcess(DataObjectStoreSchema schema);
}
