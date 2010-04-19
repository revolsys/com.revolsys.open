package com.revolsys.gis.postgis;

import javax.sql.DataSource;

import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.jdbc.io.JdbcDataObjectStore;
import com.revolsys.gis.jdbc.io.JdbcFactory;

public class PostGisFactory extends JdbcFactory {
  public PostGisFactory() {
  }

  public PostGisFactory(
    DataSource dataSource) {
    super(dataSource);
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
