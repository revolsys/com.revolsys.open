package com.revolsys.jdbc.io;

import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

public interface DataSourceFactory {

  void closeDataSource(DataSource dataSource);

  DataSource createDataSource(Map<String, Object> connectionProperties)
    throws SQLException;

}
