package com.revolsys.data.io;

public interface DataObjectStoreProxy {
  <V extends DataObjectStore> V getDataStore();
}
