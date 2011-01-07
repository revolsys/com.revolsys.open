package com.revolsys.gis.jdbc.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.sql.DataSource;

import com.revolsys.gis.data.io.DataObjectStore;

public class JdbcDataObjectStoreConnections {

  private static JdbcDataObjectStoreConnections INSTANCE = new JdbcDataObjectStoreConnections();

  public static JdbcDataObjectStoreConnections get() {
    return INSTANCE;
  }

  private final Preferences jdbcDataStoresPrefereneces;

  public JdbcDataObjectStoreConnections() {
    this(Preferences.userRoot(), "com/revolsys/jdbc/dataSource");
  }

  public JdbcDataObjectStoreConnections(final Preferences root,
    final String preferencesPath) {
    jdbcDataStoresPrefereneces = root.node("com/revolsys/jdbc/dataSource");
  }

  public JdbcDataObjectStoreConnections(final String preferencesPath) {
    this(Preferences.userRoot(), preferencesPath);
  }

  public List<String> getConnectionNames() {
    try {
      final String[] names = jdbcDataStoresPrefereneces.childrenNames();
      return Arrays.asList(names);
    } catch (final BackingStoreException e) {
      throw new RuntimeException(e);
    }
  }

  private DataObjectStore getDataObjectStore(final String connectionName) {
    final Preferences preferences = getPreferences(connectionName);
    final DataSource dataSource = null;
    if (dataSource == null) {
      return null;
    } else {
      return JdbcFactory.createDataObjectStore(dataSource);
    }
  }

  public List<DataObjectStore> getDataObjectStores() {
    final List<DataObjectStore> dataObjectStores = new ArrayList<DataObjectStore>();
    final List<String> connectionNames = getConnectionNames();
    for (final String connectionName : connectionNames) {
      final DataObjectStore dataObjectStore = getDataObjectStore(connectionName);
      if (dataObjectStore != null) {
        dataObjectStores.add(dataObjectStore);
      }
    }
    return dataObjectStores;
  }

  private Preferences getPreferences(final String connectionName) {
    return jdbcDataStoresPrefereneces.node(connectionName);
  }

  @Override
  public String toString() {
    return "JDBC Connections";
  }
}
