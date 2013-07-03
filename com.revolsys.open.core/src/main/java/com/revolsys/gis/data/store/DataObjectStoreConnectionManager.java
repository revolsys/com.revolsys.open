package com.revolsys.gis.data.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.util.OperatingSystemUtil;

public class DataObjectStoreConnectionManager {

  private static final DataObjectStoreConnectionManager INSTANCE;

  static {
    INSTANCE = new DataObjectStoreConnectionManager();
    final File dataStoresDirectory = OperatingSystemUtil.getUserApplicationDataDirectory("com.revolsys.gis/Data Stores");
    final JsonDataObjectStoreConnectionRegistry registry = new JsonDataObjectStoreConnectionRegistry(
      "User", dataStoresDirectory);
    INSTANCE.addConnectionRegistry(registry);
  }

  public static DataObjectStoreConnectionManager get() {
    return INSTANCE;
  }

  public static DataObjectStore getConnection(final String name) {
    final DataObjectStoreConnectionManager connectionManager = get();
    final List<ConnectionRegistry<DataObjectStoreConnection>> registries = connectionManager.getConnectionRegistries();
    Collections.reverse(registries);
    for (final ConnectionRegistry<DataObjectStoreConnection> registry : registries) {
      final DataObjectStoreConnection dataStoreConnection = registry.getConnection(name);
      if (dataStoreConnection != null) {
        return dataStoreConnection.getDataStore();
      }
    }
    return null;
  }

  private final List<ConnectionRegistry<DataObjectStoreConnection>> registries = new ArrayList<ConnectionRegistry<DataObjectStoreConnection>>();

  public DataObjectStoreConnectionManager() {
  }

  public void addConnectionRegistry(
    final ConnectionRegistry<DataObjectStoreConnection> registry) {
    if (!registries.contains(registry)) {
      registries.add(registry);
    }
  }

  public List<ConnectionRegistry<DataObjectStoreConnection>> getConnectionRegistries() {
    return new ArrayList<ConnectionRegistry<DataObjectStoreConnection>>(
      registries);
  }

  public ConnectionRegistry<DataObjectStoreConnection> getConnectionRegistry(
    final String name) {
    for (final ConnectionRegistry<DataObjectStoreConnection> registry : registries) {
      if (registry.getName().equals(name)) {
        return registry;
      }
    }
    return registries.get(0);
  }

  public void removeConnectionRegistry(
    final ConnectionRegistry<DataObjectStoreConnection> registry) {
    registries.remove(registry);
  }

  @Override
  public String toString() {
    return "Data Stores";
  }
}
