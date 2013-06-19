package com.revolsys.gis.data.store;

import java.io.File;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DelegatingDataObjectStoreHandler;
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
    final List<ConnectionRegistry<DataObjectStore>> registries = connectionManager.getConnectionRegistries();
    Collections.reverse(registries);
    for (final ConnectionRegistry<DataObjectStore> registry : registries) {
      DataObjectStore dataStore = registry.getConnection(name);
      if (dataStore != null) {
        if (dataStore instanceof Proxy) {
          final DelegatingDataObjectStoreHandler handler = (DelegatingDataObjectStoreHandler)Proxy.getInvocationHandler(dataStore);
          dataStore = handler.getDataStore();
        }
        return dataStore;
      }
    }
    return null;
  }

  private final List<ConnectionRegistry<DataObjectStore>> registries = new ArrayList<ConnectionRegistry<DataObjectStore>>();

  public DataObjectStoreConnectionManager() {
  }

  public void addConnectionRegistry(
    final ConnectionRegistry<DataObjectStore> registry) {
    if (!registries.contains(registry)) {
      registries.add(registry);
    }
  }

  public List<ConnectionRegistry<DataObjectStore>> getConnectionRegistries() {
    return new ArrayList<ConnectionRegistry<DataObjectStore>>(registries);
  }

  public ConnectionRegistry<DataObjectStore> getConnectionRegistry(
    final String name) {
    for (final ConnectionRegistry<DataObjectStore> registry : registries) {
      if (registry.getName().equals(name)) {
        return registry;
      }
    }
    return registries.get(0);
  }

  public void removeConnectionRegistry(
    final ConnectionRegistry<DataObjectStore> registry) {
    registries.remove(registry);
  }

  @Override
  public String toString() {
    return "Data Stores";
  }
}
