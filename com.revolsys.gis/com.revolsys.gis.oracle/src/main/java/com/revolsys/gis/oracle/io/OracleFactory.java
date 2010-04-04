package com.revolsys.gis.oracle.io;

import javax.sql.DataSource;

import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcFactory;
import com.revolsys.gis.jdbc.io.JdbcWriter;

public class OracleFactory extends JdbcFactory {

  public static JdbcDataObjectStore createDataObjectStore(
    final DataSource dataSource) {
    final OracleFactory factory = createFactory(dataSource);
    return factory.createDataObjectStore();
  }

  public static JdbcWriter createDataObjectWriter(
    final DataSource dataSource) {
    final OracleFactory factory = createFactory(dataSource);
    return factory.createWriter();
  }

  public static OracleFactory createFactory() {
    return new OracleFactory(DEFAULT_DATA_OBJECT_FACTORY);
  }

  public static OracleFactory createFactory(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    final OracleFactory factory = new OracleFactory(dataObjectFactory,
      dataSource);
    return factory;
  }

  public static OracleFactory createFactory(
    final DataSource dataSource) {
    final OracleFactory factory = new OracleFactory(
      DEFAULT_DATA_OBJECT_FACTORY, dataSource);
    return factory;
  }

  public OracleFactory(
    final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public OracleFactory(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    super(dataObjectFactory, dataSource);
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore() {
    final OracleDataObjectStore oracleDataObjectStore = new OracleDataObjectStore(getDataObjectFactory(), getDataSource());
    oracleDataObjectStore.initialize();
    return oracleDataObjectStore;
  }

}
