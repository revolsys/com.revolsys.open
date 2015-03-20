package com.revolsys.jdbc.io;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

public class DataSourceImpl extends BasicDataSource {

  @Override
  public synchronized Connection getConnection() throws SQLException {
    return super.getConnection();
  }

  @Override
  public synchronized Connection getConnection(final String user, final String pass)
    throws SQLException {
    return super.getConnection(user, pass);
  }
}
