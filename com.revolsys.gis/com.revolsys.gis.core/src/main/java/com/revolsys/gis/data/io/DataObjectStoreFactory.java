package com.revolsys.gis.data.io;

import java.util.List;
import java.util.Map;

public interface DataObjectStoreFactory {
  DataObjectStore createDataObjectStore(Map<String, Object> connectionProperties);

  List<String> getUrlPatterns();
}
