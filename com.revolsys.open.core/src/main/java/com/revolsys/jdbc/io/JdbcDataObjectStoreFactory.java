package com.revolsys.jdbc.io;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;

public class JdbcDataObjectStoreFactory implements DataObjectStoreFactory {

  private static final List<String> URL_PATTERNS = Arrays.asList("jdbc:.*");

  @Override
  public JdbcDataObjectStore createDataObjectStore(
    final Map<String, ? extends Object> connectionProperties) {
    final JdbcDatabaseFactory databaseFactory = JdbcFactoryRegistry.databaseFactory(connectionProperties);
    return databaseFactory.createDataObjectStore(connectionProperties);
  }

  @Override
  public Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcDataObjectStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}
