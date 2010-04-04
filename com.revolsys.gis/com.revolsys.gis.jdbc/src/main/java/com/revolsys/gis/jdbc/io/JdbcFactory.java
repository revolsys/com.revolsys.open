package com.revolsys.gis.jdbc.io;

import javax.sql.DataSource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObjectFactory;

public abstract class JdbcFactory {
  public static final DataObjectFactory DEFAULT_DATA_OBJECT_FACTORY = new ArrayDataObjectFactory();

  private DataObjectFactory dataObjectFactory;

  private DataSource dataSource;

  public JdbcFactory(
    final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  public JdbcFactory(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    this.dataObjectFactory = dataObjectFactory;
    setDataSource(dataSource);
  }

  public abstract JdbcDataObjectStore createDataObjectStore();

  public JdbcWriter createWriter() {
    final JdbcDataObjectStore dataObjectStore = createDataObjectStore();
    return dataObjectStore.createWriter();
  }

  public DataObjectFactory getDataObjectFactory() {
    return dataObjectFactory;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataObjectFactory(
    final DataObjectFactory dataObjectFactory) {
    this.dataObjectFactory = dataObjectFactory;
  }

  private void setDataSource(
    final DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
