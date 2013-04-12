package com.revolsys.transaction;

import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;

public class DataSourceTransactionManagerFactory {

  private static Map<DataSource, DataSourceTransactionManager> transactionManagers = new WeakHashMap<DataSource, DataSourceTransactionManager>();

  public static DataSourceTransactionManager getTransactionManager(
    DataSource dataSource) {
    synchronized (transactionManagers) {
      DataSourceTransactionManager transactionManager = transactionManagers.get(dataSource);
      if (transactionManager == null) {
        transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManagers.put(dataSource, transactionManager);
      }
      return transactionManager;
    }
  }
}
