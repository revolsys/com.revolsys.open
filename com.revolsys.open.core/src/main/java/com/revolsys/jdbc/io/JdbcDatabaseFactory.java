package com.revolsys.jdbc.io;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactory;

public interface JdbcDatabaseFactory extends DataObjectStoreFactory {

  List<String> getProductNames();

  List<String> getUrlPatterns();

  boolean canHandleUrl(String url);
  
  void closeDataSource(DataSource dataSource);

  DataSource createDataSource(Map<String, Object> connectionProperties);

  JdbcDataObjectStore createDataObjectStore(Map<String, Object> connectionProperties);

  Class<? extends DataObjectStore> getDataObjectStoreInterfaceClass(
    Map<String, Object> connectionProperties);

  JdbcDataObjectStore createDataObjectStore(DataSource dataSource);
}
