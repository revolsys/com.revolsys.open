package com.revolsys.gis.data.store;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.util.CollectionUtil;

public class DataObjectStoreConnectionRegistry extends
  AbstractConnectionRegistry<DataObjectStoreConnection> {

  public DataObjectStoreConnectionRegistry(final String name,
    final boolean visible) {
    super("rgdatastore", name);
    setVisible(visible);
    init();
  }

  public DataObjectStoreConnectionRegistry(final String name,
    final File directory) {
    super("rgdatastore", name);
    setDirectory(directory);
    init();
  }

  public void addConnection(final DataObjectStoreConnection connection) {
    addConnection(connection.getName(), connection);
  }

  public void addConnection(final String name, final DataObjectStore dataStore) {
    addConnection(new DataObjectStoreConnection(this, name, dataStore));
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
