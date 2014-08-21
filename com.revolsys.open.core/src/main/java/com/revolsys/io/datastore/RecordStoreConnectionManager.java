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

import com.revolsys.data.io.RecordStoreFactoryRegistry;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnectionRegistryManager;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.OS;
import com.revolsys.util.Property;

public class RecordStoreConnectionManager
extends
AbstractConnectionRegistryManager<RecordStoreConnectionRegistry, RecordStoreConnection> {

  public static RecordStoreConnectionManager get() {
    return INSTANCE;
  }

  public static <V extends RecordStore> V getRecordStore(final File file) {
    final Map<String, String> connectionProperties = Collections.singletonMap(
      "url", FileUtil.toUrlString(file));
    final Map<String, Object> config = Collections.<String, Object> singletonMap(
      "connection", connectionProperties);
    return getRecordStore(config);
  }

  /**
   * Get an initialized data store.
   * @param connectionProperties
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T extends RecordStore> T getRecordStore(
    final Map<String, ? extends Object> config) {
    @SuppressWarnings("rawtypes")
    final Map<String, Object> configClone = (Map)JavaBeanUtil.clone(config);
    synchronized (recordStoreByConfig) {
      RecordStore recordStore = recordStoreByConfig.get(configClone);
      if (recordStore == null) {
        final Map<String, ? extends Object> connectionProperties = (Map<String, ? extends Object>)configClone.get("connection");
        final String name = (String)connectionProperties.get("name");
        if (Property.hasValue(name)) {
          recordStore = getRecordStore(name);
          if (recordStore == null) {
            // TODO give option to add
            return null;
          }
        } else {
          recordStore = RecordStoreFactoryRegistry.createRecordStore(connectionProperties);
          recordStore.setProperties(config);
          recordStore.initialize();
        }
        recordStoreByConfig.put(configClone, recordStore);
        recordStoreCounts.put(configClone, new AtomicInteger(1));
      } else {
        final AtomicInteger count = recordStoreCounts.get(configClone);
        count.incrementAndGet();
      }
      return (T)recordStore;
    }
  }

  public static RecordStore getRecordStore(final String name) {
    final RecordStoreConnectionManager connectionManager = get();
    final List<RecordStoreConnectionRegistry> registries = new ArrayList<RecordStoreConnectionRegistry>();
    registries.addAll(connectionManager.getConnectionRegistries());
    final RecordStoreConnectionRegistry threadRegistry = RecordStoreConnectionRegistry.getForThread();
    if (threadRegistry != null) {
      registries.add(threadRegistry);
    }
    Collections.reverse(registries);
    for (final RecordStoreConnectionRegistry registry : registries) {
      final RecordStoreConnection recordStoreConnection = registry.getConnection(name);
      if (recordStoreConnection != null) {
        return recordStoreConnection.getRecordStore();
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static void releaseRecordStore(
    final Map<String, ? extends Object> config) {
    @SuppressWarnings("rawtypes")
    final Map<String, Object> configClone = (Map)JavaBeanUtil.clone(config);
    synchronized (recordStoreByConfig) {
      final RecordStore recordStore = recordStoreByConfig.get(configClone);
      if (recordStore != null) {
        final AtomicInteger count = recordStoreCounts.get(configClone);
        if (count.decrementAndGet() == 0) {
          final Map<String, ? extends Object> connectionProperties = (Map<String, ? extends Object>)configClone.get("connection");
          final String name = (String)connectionProperties.get("name");
          if (!Property.hasValue(name)) {
            // TODO release for connections from connection registries
            recordStore.close();
          }
          recordStoreByConfig.remove(configClone);
          recordStoreCounts.remove(configClone);
        }
      }
    }
  }

  private static final RecordStoreConnectionManager INSTANCE;

  static {
    INSTANCE = new RecordStoreConnectionManager();
    final File recordStoresDirectory = OS.getApplicationDataDirectory("com.revolsys.gis/Data Stores");
    INSTANCE.addConnectionRegistry("User", new FileSystemResource(
      recordStoresDirectory));
  }

  // TODO make this garbage collectable with reference counting.
  private static Map<Map<String, Object>, RecordStore> recordStoreByConfig = new HashMap<Map<String, Object>, RecordStore>();

  private static Map<Map<String, Object>, AtomicInteger> recordStoreCounts = new HashMap<Map<String, Object>, AtomicInteger>();

  public RecordStoreConnectionManager() {
    super("Data Stores");
  }

  public RecordStoreConnectionRegistry addConnectionRegistry(final String name,
    final boolean visible) {
    final RecordStoreConnectionRegistry registry = new RecordStoreConnectionRegistry(
      this, name, visible);
    addConnectionRegistry(registry);
    return registry;
  }

  public RecordStoreConnectionRegistry addConnectionRegistry(final String name,
    final Resource recordStoresDirectory) {
    final RecordStoreConnectionRegistry registry = new RecordStoreConnectionRegistry(
      this, name, recordStoresDirectory);
    addConnectionRegistry(registry);
    return registry;
  }

}
