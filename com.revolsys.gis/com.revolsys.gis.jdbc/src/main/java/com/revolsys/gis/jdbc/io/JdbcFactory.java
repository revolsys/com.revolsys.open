package com.revolsys.gis.jdbc.io;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.jdbc.JdbcUtils;

public class JdbcFactory {

  private static Map<String, Class<JdbcDataObjectStore>> dataObjectStoreClassNames = new HashMap<String, Class<JdbcDataObjectStore>>();

  public static final DataObjectFactory DEFAULT_DATA_OBJECT_FACTORY = new ArrayDataObjectFactory();

  static {
    new ClassPathXmlApplicationContext(
      "classpath*:META-INF/com.revolsys.gis.jdbc.sf.xml");
  }

  public static JdbcDataObjectStore createDataObjectStore(
    final DataSource dataSource) {
    try {
      final Connection connection = JdbcUtils.getConnection(dataSource);
      try {
        final DatabaseMetaData metaData = connection.getMetaData();
        final String productName = metaData.getDatabaseProductName();
        final Class<JdbcDataObjectStore> dataObjectStoreClass = dataObjectStoreClassNames.get(productName);
        if (dataObjectStoreClass == null) {
throw new IllegalArgumentException("Data Store not found for " + productName);
        } else {
          final JdbcDataObjectStore dataObjectStore = dataObjectStoreClass.newInstance();
          dataObjectStore.setDataSource(dataSource);
          dataObjectStore.initialize();
          return dataObjectStore;
        }
      } catch (final InstantiationException e) {
        throw new RuntimeException("Unable to instantiate", e);
      } catch (final IllegalAccessException e) {
        throw new RuntimeException("Unable to instantiate", e);
      } finally {
        JdbcUtils.close(connection);
      }
    } catch (final SQLException e) {
      throw new RuntimeException("Unable to detect database version", e);
    }
  }

  public static Class<JdbcDataObjectStore> register(
    final String productName,
    final String dataStoreClassName) {
    try {
      final Class<JdbcDataObjectStore> dataObjectStoreClass = (Class<JdbcDataObjectStore>)Class.forName(dataStoreClassName);
      dataObjectStoreClassNames.put(productName, dataObjectStoreClass);
      return dataObjectStoreClass;
    } catch (final ClassNotFoundException e) {
      throw new IllegalArgumentException("Unable to register " + productName, e);
    }
  }

  private DataObjectFactory dataObjectFactory;

  private DataSource dataSource;

  public JdbcFactory() {
    this(DEFAULT_DATA_OBJECT_FACTORY);
  }

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

  public JdbcFactory(
    final DataSource dataSource) {
    this(DEFAULT_DATA_OBJECT_FACTORY);
    setDataSource(dataSource);
  }

  public JdbcDataObjectStore createDataObjectStore() {
    return createDataObjectStore(getDataSource());
  }

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
