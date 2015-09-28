package com.revolsys.jdbc.io;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.record.schema.RecordStore;

public interface JdbcDatabaseFactory extends RecordStoreFactory {

  boolean canHandleUrl(String url);

  void closeDataSource(DataSource dataSource);

  List<String> getProductNames();

  @Override
  Class<? extends RecordStore> getRecordStoreInterfaceClass(
    Map<String, ? extends Object> connectionProperties);

  @Override
  List<String> getUrlPatterns();

  DataSource newDataSource(Map<String, ? extends Object> connectionProperties);

  JdbcRecordStore newRecordStore(DataSource dataSource);

  @Override
  JdbcRecordStore newRecordStore(Map<String, ? extends Object> connectionProperties);
}
