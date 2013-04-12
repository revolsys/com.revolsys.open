package com.revolsys.transaction;

import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class DataSourceTransactionManagerFactory implements
  FactoryBean<DataSourceTransactionManager> {

  private static Map<DataSource, DataSourceTransactionManager> transactionManagers = new WeakHashMap<DataSource, DataSourceTransactionManager>();

  public static DataSourceTransactionManager getTransactionManager(
    final DataSource dataSource) {
    synchronized (transactionManagers) {
      DataSourceTransactionManager transactionManager = transactionManagers.get(dataSource);
      if (transactionManager == null) {
        transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManagers.put(dataSource, transactionManager);
      }
      return transactionManager;
    }
  }

  private DataSource dataSource;

  public DataSource getDataSource() {
    return dataSource;
  }

  @Override
  public DataSourceTransactionManager getObject() throws Exception {
    return getTransactionManager(dataSource);
  }

  @Override
  public Class<?> getObjectType() {
    return DataSourceTransactionManager.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }

  public void setDataSource(final DataSource dataSource) {
    this.dataSource = dataSource;
  }
}
