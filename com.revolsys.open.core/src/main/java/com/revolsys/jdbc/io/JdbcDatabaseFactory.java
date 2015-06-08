package com.revolsys.jdbc.io;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.data.record.io.RecordStoreFactory;
import com.revolsys.data.record.schema.RecordStore;

public interface JdbcDatabaseFactory extends RecordStoreFactory {

  boolean canHandleUrl(String url);

  void closeDataSource(DataSource dataSource);

  DataSource createDataSource(Map<String, ? extends Object> connectionProperties);

  JdbcRecordStore createRecordStore(DataSource dataSource);

  @Override
  JdbcRecordStore createRecordStore(Map<String, ? extends Object> connectionProperties);

  List<String> getProductNames();

  @Override
  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  @Override
  List<String> getUrlPatterns();
}
