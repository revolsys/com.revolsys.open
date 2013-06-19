package com.revolsys.gis.data.store;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.io.DelegatingDataObjectStoreHandler;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.util.CollectionUtil;

public class JsonDataObjectStoreConnectionRegistry extends
  AbstractConnectionRegistry<DataObjectStore> {

  private final File directory;

  public JsonDataObjectStoreConnectionRegistry(final String name,
    final File directory) {
    super(name);
    this.directory = directory;
    final boolean readOnly = !directory.canWrite();
    setReadOnly(readOnly);
    init();
  }

  @Override
  public void createConnection(
    final Map<String, ? extends Object> connectionParameters) {
    final String name = CollectionUtil.getString(connectionParameters, "name");
    final File file = new File(directory, name + ".rgdatastore");
    final FileSystemResource resource = new FileSystemResource(file);
    JsonMapIoFactory.write(connectionParameters, resource);
    loadDataStore(file);
  }

  @Override
  protected void doInit() {
    if (directory.isDirectory()) {
      for (final File dataStoreFile : FileUtil.getFilesByExtension(directory,
        "rgdatastore")) {
        loadDataStore(dataStoreFile);
      }
    }
  }

  protected void loadDataStore(final File dataStoreFile) {
    final Map<String, ? extends Object> config = JsonMapIoFactory.toMap(dataStoreFile);
    final String name = CollectionUtil.getString(config, "name");
    try {
      final Map<String, Object> connectionProperties = CollectionUtil.get(
        config, "connection", Collections.<String, Object> emptyMap());
      if (connectionProperties.isEmpty()) {
        LoggerFactory.getLogger(getClass()).error(
          "Data store must include a 'connection' map property: "
            + dataStoreFile);
      } else {
        final DataObjectStore dataStore = DelegatingDataObjectStoreHandler.create(
          name, connectionProperties);
        addConnection(name, dataStore);
      }
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Error creating data store from: " + dataStoreFile, e);
    }
  }
}
