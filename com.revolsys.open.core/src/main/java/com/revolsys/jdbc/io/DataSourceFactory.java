package com.revolsys.jdbc.io;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

public interface DataSourceFactory {

  DataSource createDataSource(Map<String, Object> connectionProperties) throws SQLException;

  void closeDataSource(DataSource dataSource);

}
