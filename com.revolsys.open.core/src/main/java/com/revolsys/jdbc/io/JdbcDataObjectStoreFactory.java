package com.revolsys.jdbc.io;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;

public class JdbcDataObjectStoreFactory implements DataObjectStoreFactory {

  private static final List<String> URL_PATTERNS = Arrays.asList("jdbc:.*");

  public static void closeDataSource(final DataSource dataSource) {
    final JdbcDatabaseFactory databaseFactory = JdbcFactoryRegistry.databaseFactory(dataSource);
    databaseFactory.closeDataSource(dataSource);
  }

  public static DataSource createDataSource(
    final Map<String, ? extends Object> connectionProperties) {
    final JdbcDatabaseFactory databaseFactory = JdbcFactoryRegistry.databaseFactory(connectionProperties);
    return databaseFactory.createDataSource(connectionProperties);
  }

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
