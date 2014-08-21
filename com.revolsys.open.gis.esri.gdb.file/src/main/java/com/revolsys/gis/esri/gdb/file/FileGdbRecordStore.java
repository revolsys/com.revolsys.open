package com.revolsys.gis.esri.gdb.file;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStore;

public interface FileGdbRecordStore extends RecordStore {

  void deleteGeodatabase();

  @Override
  RecordDefinition getRecordDefinition(RecordDefinition recordDefinition);

  @Override
  void initialize();

  void setCreateMissingRecordStore(boolean createMissingRecordStore);

  void setCreateMissingTables(boolean createMissingTables);

  void setDefaultSchema(String string);

}
