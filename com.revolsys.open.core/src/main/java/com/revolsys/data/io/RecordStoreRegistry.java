package com.revolsys.data.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RecordStoreRegistry {
  private Map<String, RecordStore> recordStores = new HashMap<String, RecordStore>();

  public void addDataStore(final String name, final RecordStore recordStore) {
    this.recordStores.put(name, recordStore);
  }

  public RecordStore getRecordStore(final String name) {
    return recordStores.get(name);
  }

  public Map<String, RecordStore> getRecordStores() {
    return Collections.unmodifiableMap(recordStores);
  }

  public void setRecordStores(final Map<String, RecordStore> recordStores) {
    this.recordStores = recordStores;
  }

}
