package com.revolsys.gis.esri.gdb.file;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObjectMetaData;

public interface FileGdbDataObjectStore extends DataObjectStore {

  void deleteGeodatabase();

  @Override
  DataObjectMetaData getMetaData(DataObjectMetaData metaData);

  @Override
  void initialize();

  void setCreateMissingDataStore(boolean createMissingDataStore);

  void setCreateMissingTables(boolean createMissingTables);

  void setDefaultSchema(String string);

}
