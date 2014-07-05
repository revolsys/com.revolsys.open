package com.revolsys.data.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecordStoreRegistry {
  private Map<String, RecordStore> recordStores = new HashMap<String, RecordStore>();

  public void addDataStore(final String name, final RecordStore dataStore) {
    this.recordStores.put(name, dataStore);
  }

  public RecordStore getRecordStore(final String name) {
    return recordStores.get(name);
  }

  public Map<String, RecordStore> getRecordStores() {
    return Collections.unmodifiableMap(recordStores);
  }

  public void setRecordStores(final Map<String, RecordStore> dataStores) {
    this.recordStores = dataStores;
  }

}
