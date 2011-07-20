package com.revolsys.gis.jdbc.io;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStoreFactory;

public class JdbcDataObjectStoreFactory implements DataObjectStoreFactory {

  private static final List<String> URL_PATTERNS = Arrays.asList("jdbc:.*");

  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, Object> connectionProperties) {
    return JdbcFactory.createDataObjectStore(connectionProperties);
  }

  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}
