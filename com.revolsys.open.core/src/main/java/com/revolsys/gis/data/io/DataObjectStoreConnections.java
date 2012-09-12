package com.revolsys.gis.data.io;

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

public class DataObjectStoreConnections implements PropertyChangeSupportProxy {

  private static final Logger LOG = LoggerFactory.getLogger(DataObjectStoreConnections.class);

  private static DataObjectStoreConnections INSTANCE = new DataObjectStoreConnections();

  public static DataObjectStoreConnections get() {
    return INSTANCE;
  }

  private final Map<String, DataObjectStore> dataStores = new HashMap<String, DataObjectStore>();

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private final Preferences dataStoresPrefereneces;

  public DataObjectStoreConnections() {
    this(Preferences.userRoot(), "com/revolsys/data/store");
  }

  public DataObjectStoreConnections(final Preferences root,
    final String preferencesPath) {
    dataStoresPrefereneces = root.node(preferencesPath);
  }

  public DataObjectStoreConnections(final String preferencesPath) {
    this(Preferences.userRoot(), preferencesPath);
  }

  public void createConnection(final String connectionName,
    final Map<String, String> config) {
    final Preferences preferences = getPreferences(connectionName);
    for (final Entry<String, String> param : config.entrySet()) {
      preferences.put(param.getKey(), param.getValue());
    }
    final DataObjectStore dataObjectStore = getDataObjectStore(connectionName);
    final int index = getConnectionNames().indexOf(connectionName);
    propertyChangeSupport.fireIndexedPropertyChange("connections", index, null,
      dataObjectStore);

  }

  public List<String> getConnectionNames() {
    try {
      final String[] names = dataStoresPrefereneces.childrenNames();
      return Arrays.asList(names);
    } catch (final BackingStoreException e) {
      throw new RuntimeException(e);
    }
  }

  public DataObjectStore getDataObjectStore(final String connectionName) {
    DataObjectStore dataStore = dataStores.get(connectionName);
    if (dataStore == null) {
      final Preferences preferences = getPreferences(connectionName);
      final Map<String, Object> config = CollectionUtil.toMap(preferences);
      config.remove("productName");
      try {
        if (config.get("url") == null) {
          LOG.error("No URL set for " + connectionName);
          preferences.removeNode();
        } else {
          dataStore = DelegatingDataObjectStore.create(connectionName, config);
          dataStores.put(connectionName, dataStore);
        }
      } catch (final Throwable t) {
        LOG.error("Unable to create data store " + connectionName, t);
      }
    }
    return dataStore;
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
    return dataStoresPrefereneces.node(connectionName);
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  @Override
  public String toString() {
    return "Data Store Connections";
  }
}
