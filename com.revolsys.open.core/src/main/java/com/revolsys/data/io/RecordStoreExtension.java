package com.revolsys.data.io;

import java.util.Map;

public interface RecordStoreExtension {

  public abstract void initialize(RecordStore dataStore,
    Map<String, Object> connectionProperties);

  boolean isEnabled(RecordStore dataStore);

  public abstract void postProcess(RecordStoreSchema schema);

  public abstract void preProcess(RecordStoreSchema schema);
}
