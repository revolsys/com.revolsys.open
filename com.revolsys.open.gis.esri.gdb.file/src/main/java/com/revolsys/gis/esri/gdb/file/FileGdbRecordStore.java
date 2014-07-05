package com.revolsys.gis.esri.gdb.file;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.record.schema.RecordDefinition;

public interface FileGdbRecordStore extends RecordStore {

  void deleteGeodatabase();

  @Override
  RecordDefinition getMetaData(RecordDefinition metaData);

  @Override
  void initialize();

  void setCreateMissingDataStore(boolean createMissingDataStore);

  void setCreateMissingTables(boolean createMissingTables);

  void setDefaultSchema(String string);

}
