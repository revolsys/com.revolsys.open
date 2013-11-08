package com.revolsys.gis.data.io;

public interface DataObjectStoreProxy {
  <V extends DataObjectStore> V getDataStore();
}
