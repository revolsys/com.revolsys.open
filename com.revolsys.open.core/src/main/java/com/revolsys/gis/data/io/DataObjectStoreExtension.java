package com.revolsys.gis.data.io;

public interface DataObjectStoreExtension {

  public abstract void initialize(DataObjectStore dataStore);

  public abstract void postProcess(DataObjectStoreSchema schema);

  public abstract void preProcess(DataObjectStoreSchema schema);

}
