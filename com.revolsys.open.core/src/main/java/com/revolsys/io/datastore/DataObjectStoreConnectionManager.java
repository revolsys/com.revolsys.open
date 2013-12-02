package com.revolsys.io.datastore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DataObjectStoreFactoryRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnectionRegistryManager;
import com.revolsys.util.JavaBeanUtil;
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

  // TODO make this garbage collectable with reference counting.
  private static Map<Map<String, Object>, DataObjectStore> dataStoreByConfig = new HashMap<Map<String, Object>, DataObjectStore>();

  private static Map<Map<String, Object>, AtomicInteger> dataStoreCounts = new HashMap<Map<String, Object>, AtomicInteger>();

  public static DataObjectStoreConnectionManager get() {
    return INSTANCE;
  }

  public static <V extends DataObjectStore> V getDataStore(final File file) {
    final Map<String, String> connectionProperties = Collections.singletonMap(
      "url", FileUtil.toUrlString(file));
    final Map<String, Object> config = Collections.<String, Object> singletonMap(
      "connection", connectionProperties);
    return getDataStore(config);
  }

  /**
   * Get an initialized data store.
   * @param connectionProperties
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends DataObjectStore> T getDataStore(
    final Map<String, ? extends Object> config) {
    @SuppressWarnings("rawtypes")
    final Map<String, Object> configClone = (Map)JavaBeanUtil.clone(config);
    synchronized (dataStoreByConfig) {
      DataObjectStore dataStore = dataStoreByConfig.get(configClone);
      if (dataStore == null) {
        final Map<String, ? extends Object> connectionProperties = (Map<String, ? extends Object>)configClone.get("connection");
        final String name = (String)connectionProperties.get("name");
        if (StringUtils.hasText(name)) {
          dataStore = getDataStore(name);
          if (dataStore == null) {
            // TODO give option to add
            return null;
          }
        } else {
          dataStore = DataObjectStoreFactoryRegistry.createDataObjectStore(connectionProperties);
          dataStore.setProperties(config);
          dataStore.initialize();
        }
        dataStoreByConfig.put(configClone, dataStore);
        dataStoreCounts.put(configClone, new AtomicInteger(1));
      } else {
        final AtomicInteger count = dataStoreCounts.get(configClone);
        count.incrementAndGet();
      }
      return (T)dataStore;
    }
  }

  public static DataObjectStore getDataStore(final String name) {
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

  @SuppressWarnings("unchecked")
  public static void releaseDataStore(final Map<String, ? extends Object> config) {
    @SuppressWarnings("rawtypes")
    final Map<String, Object> configClone = (Map)JavaBeanUtil.clone(config);
    synchronized (dataStoreByConfig) {
      final DataObjectStore dataStore = dataStoreByConfig.get(configClone);
      if (dataStore != null) {
        final AtomicInteger count = dataStoreCounts.get(configClone);
        if (count.decrementAndGet() == 0) {
          final Map<String, ? extends Object> connectionProperties = (Map<String, ? extends Object>)configClone.get("connection");
          final String name = (String)connectionProperties.get("name");
          if (!StringUtils.hasText(name)) {
            // TODO release for connections from connection registries
            dataStore.close();
          }
          dataStoreByConfig.remove(configClone);
          dataStoreCounts.remove(configClone);
        }
      }
    }
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
