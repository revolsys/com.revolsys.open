package com.revolsys.gis.jdbc.io;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.util.CollectionUtil;

public class JdbcDataObjectStoreConnections implements
  PropertyChangeSupportProxy {

  private static final Logger LOG = LoggerFactory.getLogger(JdbcDataObjectStoreConnections.class);

  private static JdbcDataObjectStoreConnections INSTANCE = new JdbcDataObjectStoreConnections();

  private Map<String, JdbcDataObjectStore> dataStores = new HashMap<String, JdbcDataObjectStore>();

  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

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

  private JdbcDataObjectStore getDataObjectStore(final String connectionName) {
    JdbcDataObjectStore dataStore = dataStores.get(connectionName);
    if (dataStore == null) {
      final Preferences preferences = getPreferences(connectionName);
      Map<String, Object> config = CollectionUtil.toMap(preferences);
      config.remove("productName");
      try {
        if (config.get("url") == null) {
          LOG.error("No JDBC URL set for " + connectionName);
          preferences.removeNode();
        } else {
          dataStore = new LazyJdbcDataObjectStore(connectionName, config);
          dataStores.put(connectionName, dataStore);
        }
      } catch (Throwable t) {
        LOG.error("Unable to create data store " + connectionName, t);
      }
    }
    return dataStore;
  }

  public List<JdbcDataObjectStore> getDataObjectStores() {
    final List<JdbcDataObjectStore> dataObjectStores = new ArrayList<JdbcDataObjectStore>();
    final List<String> connectionNames = getConnectionNames();
    for (final String connectionName : connectionNames) {
      final JdbcDataObjectStore dataObjectStore = getDataObjectStore(connectionName);
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

  public void createConnection(String connectionName, Map<String, String> config) {
    final Preferences preferences = getPreferences(connectionName);
    for (Entry<String, String> param : config.entrySet()) {
      preferences.put(param.getKey(), param.getValue());
    }
    propertyChangeSupport.firePropertyChange(connectionName, null, preferences);

  }

  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }
}
