package com.revolsys.jdbc.process;

import java.util.List;

import javax.sql.DataSource;

import com.revolsys.jdbc.JdbcUtils;
import com.revolsys.parallel.AbstractRunnable;

public class JdbcUpdateStatementRunnable extends AbstractRunnable {

  private DataSource dataSource;

  private List<Object> parameters;

  private String sql;

  @Override
  public void doRun() {
    JdbcUtils.executeUpdate(this.dataSource, this.sql, this.parameters.toArray());
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  public List<Object> getParameters() {
    return this.parameters;
  }

  public String getSql() {
    return this.sql;
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
