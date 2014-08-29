package com.revolsys.jdbc.io;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.data.io.RecordStoreFactory;
import com.revolsys.data.record.schema.RecordStore;

public class JdbcRecordStoreFactory implements RecordStoreFactory {

  public static void closeDataSource(final DataSource dataSource) {
    final JdbcDatabaseFactory databaseFactory = JdbcFactoryRegistry.databaseFactory(dataSource);
    databaseFactory.closeDataSource(dataSource);
  }

  public static DataSource createDataSource(
    final Map<String, ? extends Object> connectionProperties) {
    final JdbcDatabaseFactory databaseFactory = JdbcFactoryRegistry.databaseFactory(connectionProperties);
    return databaseFactory.createDataSource(connectionProperties);
  }

  private static final List<String> URL_PATTERNS = Arrays.asList("jdbc:.*");

  @Override
  public JdbcRecordStore createRecordStore(
    final Map<String, ? extends Object> connectionProperties) {
    final JdbcDatabaseFactory databaseFactory = JdbcFactoryRegistry.databaseFactory(connectionProperties);
    return databaseFactory.createRecordStore(connectionProperties);
  }

  @Override
  public String getName() {
    return "JDBC";
  }

  @Override
  public List<String> getRecordStoreFileExtensions() {
    return Collections.emptyList();
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return JdbcRecordStore.class;
  }

  @Override
  public List<String> getUrlPatterns() {
    return URL_PATTERNS;
  }

}
