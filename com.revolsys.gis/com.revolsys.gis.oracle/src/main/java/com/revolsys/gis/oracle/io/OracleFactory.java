package com.revolsys.gis.oracle.io;

import javax.sql.DataSource;

import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcFactory;

public class OracleFactory extends JdbcFactory {

  public OracleFactory(
    final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public OracleFactory(
    final DataSource dataSource) {
    super(dataSource);
  }

  public OracleFactory(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    super(dataObjectFactory, dataSource);
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore() {
    final JdbcDataObjectStore oracleDataObjectStore = new OracleDataObjectStore(
      getDataObjectFactory(), getDataSource());
    oracleDataObjectStore.initialize();
    return oracleDataObjectStore;
  }

}
