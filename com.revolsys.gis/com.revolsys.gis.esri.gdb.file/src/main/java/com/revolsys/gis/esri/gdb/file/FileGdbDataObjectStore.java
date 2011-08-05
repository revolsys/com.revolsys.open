package com.revolsys.gis.esri.gdb.file;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.esri.gdb.xml.model.DETable;
import com.revolsys.gis.esri.gdb.xml.model.Domain;

public interface FileGdbDataObjectStore extends DataObjectStore{

  void setCreateMissingGeodatabase(boolean createMissingGeodatabase);

  void setCreateMissingTables(boolean createMissingTables);

  void initialize();

  void setDefaultSchema(String string);

  DataObjectMetaData getMetaData(DataObjectMetaData metaData);

  void deleteGeodatabase();

}
