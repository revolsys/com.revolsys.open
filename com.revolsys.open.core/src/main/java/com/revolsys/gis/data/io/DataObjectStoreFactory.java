package com.revolsys.gis.data.io;

import java.util.List;
import java.util.Map;

public interface DataObjectStoreFactory {
  DataObjectStore createDataObjectStore(
    Map<String, ? extends Object> connectionProperties);

  Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<String> getFileExtensions();

  String getName();

  List<String> getUrlPatterns();
}
