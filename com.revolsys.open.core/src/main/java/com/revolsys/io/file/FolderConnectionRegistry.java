package com.revolsys.io.file;

import java.io.File;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnectionRegistry;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class FolderConnectionRegistry extends AbstractConnectionRegistry<FolderConnection> {

  private static final ThreadLocal<FolderConnectionRegistry> threadRegistry = new ThreadLocal<FolderConnectionRegistry>();

  public static FolderConnectionRegistry getForThread() {
    return FolderConnectionRegistry.threadRegistry.get();
  }

  public static FolderConnectionRegistry setForThread(final FolderConnectionRegistry registry) {
    final FolderConnectionRegistry oldValue = getForThread();
    FolderConnectionRegistry.threadRegistry.set(registry);
    return oldValue;
  }

  public FolderConnectionRegistry(final FileConnectionManager connectionManager,
    final String name) {
    super(connectionManager, name);
    init();
  }

  public FolderConnectionRegistry(final FileConnectionManager connectionManager,
    final String name, final boolean visible, final FolderConnection... connections) {
    super(connectionManager, name);
    setReadOnly(!visible);
    setVisible(visible);
    init();
    for (final FolderConnection connection : connections) {
      addConnection(connection);
    }
  }

  public FolderConnectionRegistry(final FileConnectionManager connectionManager,
    final String name, final Resource resource, final boolean readOnly) {
    super(connectionManager, name);
    setReadOnly(readOnly);
    setDirectory(resource);
    init();
  }

  public FolderConnectionRegistry(final String name) {
    this(null, name, true);
  }

  public FolderConnectionRegistry(final String name, final Resource resource,
    final boolean readOnly) {
    this(null, name, resource, readOnly);
  }

  public void addConnection(final FolderConnection connection) {
    final String name = connection.getName();
    addConnection(name, connection);
    if (!isReadOnly()) {
      final File file = getConnectionFile(name);
      if (file != null && (!file.exists() || file.canWrite())) {
        final FileSystemResource resource = new FileSystemResource(file);
        Json.write(connection.toMap(), resource);
      }
    }
  }

  public FolderConnection addConnection(final String name, final File file) {
    final FolderConnection connection = new FolderConnection(this, name, file);
    addConnection(connection);
    return connection;
  }

  @Override
  protected FolderConnection loadConnection(final File connectionFile) {
    final Map<String, ? extends Object> config = Json.toMap(connectionFile);
    String name = Maps.getString(config, "name");
    if (!Property.hasValue(name)) {
      name = FileUtil.getBaseName(connectionFile);
    }
    try {
      final String fileName = (String)config.get("file");

      return addConnection(name, FileUtil.getFile(fileName));
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass())
        .error("Error creating folder connection from: " + connectionFile, e);
      return null;
    }
  }

  @Override
  public boolean removeConnection(final FolderConnection connection) {
    if (connection == null || isReadOnly()) {
      return false;
    } else {
      final String name = connection.getName();
      final boolean removed = removeConnection(name, connection);
      return removed;
    }
  }
}
