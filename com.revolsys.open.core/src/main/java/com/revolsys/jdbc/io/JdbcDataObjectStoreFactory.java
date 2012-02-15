package com.revolsys.jdbc.io;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;

public class JdbcDataObjectStoreFactory implements DataObjectStoreFactory {

  private static final List<String> URL_PATTERNS = Arrays.asList("jdbc:.*");

  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, Object> connectionProperties) {
    return JdbcFactory.createDataObjectStore(connectionProperties);
  }

  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, Object> connectionProperties) {
    return JdbcDataObjectStore.class;
  }

  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}
