package com.revolsys.io.datastore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.io.connection.AbstractConnectionRegistryManager;
import com.revolsys.util.OS;

public class DataObjectStoreConnectionManager
  extends
  AbstractConnectionRegistryManager<DataObjectStoreConnectionRegistry, DataObjectStoreConnection> {

  private static final DataObjectStoreConnectionManager INSTANCE;

  static {
    INSTANCE = new DataObjectStoreConnectionManager();
    final File dataStoresDirectory = OS.getApplicationDataDirectory("com.revolsys.gis/Data Stores");
    INSTANCE.addConnectionRegistry("User", new FileSystemResource(
      dataStoresDirectory));
  }

  public static DataObjectStoreConnectionManager get() {
    return INSTANCE;
  }

  public static DataObjectStore getConnection(final String name) {
    final DataObjectStoreConnectionManager connectionManager = get();
    final List<DataObjectStoreConnectionRegistry> registries = new ArrayList<DataObjectStoreConnectionRegistry>();
    registries.addAll(connectionManager.getConnectionRegistries());
    final DataObjectStoreConnectionRegistry threadRegistry = DataObjectStoreConnectionRegistry.getForThread();
    if (threadRegistry != null) {
      registries.add(threadRegistry);
    }
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
    final String name, final Resource dataStoresDirectory) {
    final DataObjectStoreConnectionRegistry registry = new DataObjectStoreConnectionRegistry(
      this, name, dataStoresDirectory);
    addConnectionRegistry(registry);
    return registry;
  }

}
