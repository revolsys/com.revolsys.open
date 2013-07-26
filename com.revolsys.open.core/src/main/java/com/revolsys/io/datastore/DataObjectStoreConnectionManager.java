package com.revolsys.io.datastore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.io.connection.AbstractConnectionRegistryManager;
import com.revolsys.util.OperatingSystemUtil;

public class DataObjectStoreConnectionManager extends
  AbstractConnectionRegistryManager<DataObjectStoreConnectionRegistry> {

  private static final DataObjectStoreConnectionManager INSTANCE;

  static {
    INSTANCE = new DataObjectStoreConnectionManager();
    final File dataStoresDirectory = OperatingSystemUtil.getUserApplicationDataDirectory("com.revolsys.gis/Data Stores");
    INSTANCE.addConnectionRegistry("User", dataStoresDirectory);
  }

  public static DataObjectStoreConnectionManager get() {
    return INSTANCE;
  }

  public static DataObjectStore getConnection(final String name) {
    final DataObjectStoreConnectionManager connectionManager = get();
    final List<DataObjectStoreConnectionRegistry> registries = new ArrayList<DataObjectStoreConnectionRegistry>(
      connectionManager.getConnectionRegistries());
    Collections.reverse(registries);
    for (final DataObjectStoreConnectionRegistry registry : registries) {
      final DataObjectStoreConnection dataStoreConnection = registry.getConnection(name);
      if (dataStoreConnection != null) {
        return dataStoreConnection.getDataStore();
      }
    }
    return null;
  }

  public DataObjectStoreConnectionManager() {
    super("Data Stores");
  }

  public DataObjectStoreConnectionRegistry addConnectionRegistry(
    final String name, final boolean visible) {
    final DataObjectStoreConnectionRegistry registry = new DataObjectStoreConnectionRegistry(
      this, name, visible);
    addConnectionRegistry(registry);
    return registry;
  }

  public DataObjectStoreConnectionRegistry addConnectionRegistry(
    final String name, final File dataStoresDirectory) {
    final DataObjectStoreConnectionRegistry registry = new DataObjectStoreConnectionRegistry(
      this, name, dataStoresDirectory);
    addConnectionRegistry(registry);
    return registry;
  }
}
