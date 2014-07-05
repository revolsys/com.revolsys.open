package com.revolsys.jdbc.io;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.data.io.RecordStore;
import com.revolsys.data.io.RecordStoreFactory;

public interface JdbcDatabaseFactory extends RecordStoreFactory {

  boolean canHandleUrl(String url);

  void closeDataSource(DataSource dataSource);

  JdbcRecordStore createRecordStore(DataSource dataSource);

  @Override
  JdbcRecordStore createRecordStore(
    Map<String, ? extends Object> connectionProperties);

  DataSource createDataSource(Map<String, ? extends Object> connectionProperties);

  @Override
  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  List<String> getProductNames();

  @Override
  List<String> getUrlPatterns();
}
