package com.revolsys.gis.data.io;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataObjectStoreRegistry {
  private Map<String, DataObjectStore> dataStores = new HashMap<String, DataObjectStore>();

  public void addDataStore(final String name, final DataObjectStore dataStore) {
    this.dataStores.put(name, dataStore);
  }

  public DataObjectStore getDataObjectStore(final String name) {
    return dataStores.get(name);
  }

  public Map<String, DataObjectStore> getDataStores() {
    return Collections.unmodifiableMap(dataStores);
  }

  public void setDataStores(final Map<String, DataObjectStore> dataStores) {
    this.dataStores = dataStores;
  }

}
