package com.revolsys.io.datastore;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnectionRegistry;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.util.CollectionUtil;

public class DataObjectStoreConnectionRegistry extends
  AbstractConnectionRegistry<DataObjectStoreConnection> {

  private static final ThreadLocal<DataObjectStoreConnectionRegistry> threadRegistry = new ThreadLocal<DataObjectStoreConnectionRegistry>();

  public static DataObjectStoreConnectionRegistry getForThread() {
    return DataObjectStoreConnectionRegistry.threadRegistry.get();
  }

  public static DataObjectStoreConnectionRegistry setForThread(
    final DataObjectStoreConnectionRegistry registry) {
    final DataObjectStoreConnectionRegistry oldValue = getForThread();
    DataObjectStoreConnectionRegistry.threadRegistry.set(registry);
    return oldValue;
  }

  protected DataObjectStoreConnectionRegistry(
    final DataObjectStoreConnectionManager connectionManager,
    final String name, final boolean visible) {
    super(connectionManager, name);
    setVisible(visible);
    init();
  }

  protected DataObjectStoreConnectionRegistry(
    final DataObjectStoreConnectionManager connectionManager,
    final String name, final Resource resource) {
    super(connectionManager, name);
    setDirectory(resource);
    init();
  }

  public DataObjectStoreConnectionRegistry(final String name) {
    this(null, name, true);
  }

  public DataObjectStoreConnectionRegistry(final String name,
    final Resource resource, final boolean readOnly) {
    super(null, name);
    setReadOnly(readOnly);
    setDirectory(resource);
    init();
  }

  public void addConnection(final DataObjectStoreConnection connection) {
    addConnection(connection.getName(), connection);
  }

  public void addConnection(final Map<String, Object> config) {
    final DataObjectStoreConnection connection = new DataObjectStoreConnection(
      this, null, config);
    addConnection(connection);
  }

  public void addConnection(final String name, final DataObjectStore dataStore) {
    final DataObjectStoreConnection connection = new DataObjectStoreConnection(
      this, name, dataStore);
    addConnection(connection);
  }

  @Override
  protected DataObjectStoreConnection loadConnection(final File dataStoreFile) {
    final Map<String, ? extends Object> config = JsonMapIoFactory.toMap(dataStoreFile);
    String name = CollectionUtil.getString(config, "name");
    if (!StringUtils.hasText(name)) {
      name = FileUtil.getBaseName(dataStoreFile);
    }
    try {
      final Map<String, Object> connectionProperties = CollectionUtil.get(
        config, "connection", Collections.<String, Object> emptyMap());
      if (connectionProperties.isEmpty()) {
        LoggerFactory.getLogger(getClass()).error(
          "Data store must include a 'connection' map property: "
            + dataStoreFile);
        return null;
      } else {
        final DataObjectStoreConnection dataStoreConnection = new DataObjectStoreConnection(
          this, dataStoreFile.toString(), config);
        addConnection(name, dataStoreConnection);
        return dataStoreConnection;
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Error creating data store from: " + dataStoreFile, e);
      return null;
    }
  }

  @Override
  public boolean removeConnection(final DataObjectStoreConnection connection) {
    if (connection == null) {
      return false;
    } else {
      return removeConnection(connection.getName(), connection);
    }
  }

}
