package com.revolsys.jdbc.process;

import java.util.List;

import javax.sql.DataSource;

import com.revolsys.jdbc.JdbcUtils;

public class JdbcUpdateStatementRunnable implements Runnable {

  private DataSource dataSource;

  private List<Object> parameters;

  private String sql;

  public DataSource getDataSource() {
    return dataSource;
  }

  public List<Object> getParameters() {
    return parameters;
  }

  public String getSql() {
    return sql;
  }

  @Override
  public void run() {
    JdbcUtils.executeUpdate(dataSource, sql, parameters.toArray());
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setParameters(final List<Object> parameters) {
    this.parameters = parameters;
  }

  public void setSql(final String sql) {
    this.sql = sql;
  }
}
