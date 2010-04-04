package com.revolsys.gis.postgis;

import javax.sql.DataSource;

import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcFactory;
import com.revolsys.gis.jdbc.io.JdbcWriter;

public class PostGisFactory extends JdbcFactory {

  public static JdbcDataObjectStore createDataObjectStore(
    final DataSource dataSource) {
    final PostGisFactory factory = createFactory(dataSource);
    return factory.createDataObjectStore();
  }

  public static JdbcWriter createDataObjectWriter(
    final DataSource dataSource) {
    final PostGisFactory factory = createFactory(dataSource);
    return factory.createWriter();
  }

  public static PostGisFactory createFactory() {
    return new PostGisFactory(DEFAULT_DATA_OBJECT_FACTORY);
  }

  public static PostGisFactory createFactory(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    final PostGisFactory factory = new PostGisFactory(dataObjectFactory,
      dataSource);
    return factory;
  }

  public static PostGisFactory createFactory(
    final DataSource dataSource) {
    final PostGisFactory factory = new PostGisFactory(
      DEFAULT_DATA_OBJECT_FACTORY, dataSource);
    return factory;
  }

  public PostGisFactory(
    final DataObjectFactory dataObjectFactory) {
    super(dataObjectFactory);
  }

  public PostGisFactory(
    final DataObjectFactory dataObjectFactory,
    final DataSource dataSource) {
    super(dataObjectFactory, dataSource);
  }

  @Override
  public JdbcDataObjectStore createDataObjectStore() {
    return new PostGisDataObjectStore(getDataObjectFactory(), getDataSource());
  }

}
