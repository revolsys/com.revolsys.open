package com.revolsys.jdbc.io;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.collection.map.MapEx;

public class JdbcRecordStoreFactoryBean extends AbstractFactoryBean<JdbcRecordStore> {
  private MapEx config = JsonObject.hash();

  private DataSource dataSource;

  private Map<String, Object> properties = new LinkedHashMap<>();

  @Override
  protected JdbcRecordStore createInstance() throws Exception {
    JdbcRecordStore recordStore;
    if (this.dataSource == null) {
      final JdbcDatabaseFactory databaseFactory = JdbcDatabaseFactory.databaseFactory(this.config);
      recordStore = databaseFactory.newRecordStore(this.config);
    } else {
      final JdbcDatabaseFactory databaseFactory = JdbcDatabaseFactory
        .databaseFactory(this.dataSource);
      recordStore = databaseFactory.newRecordStore(this.dataSource);
    }
    recordStore.setProperties(this.properties);
    recordStore.initialize();
    return recordStore;
  }

  @Override
  protected void destroyInstance(final JdbcRecordStore recordStore) throws Exception {
    recordStore.close();
    this.config = null;
    this.dataSource = null;
    this.properties = null;
  }

  public Map<String, Object> getConfig() {
    return this.config;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

  @Override
  public Class<?> getObjectType() {
    return JdbcRecordStore.class;
  }

  public Map<String, Object> getProperties() {
    return this.properties;
  }

  public void setConfig(final MapEx config) {
    this.config = config;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void setProperties(final Map<String, Object> properties) {
    this.properties = properties;
  }
}
