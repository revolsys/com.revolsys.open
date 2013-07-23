package com.revolsys.io;

import java.io.File;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.store.AbstractConnectionRegistry;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.util.CollectionUtil;

public class FolderConnectionRegistry extends
  AbstractConnectionRegistry<FolderConnection> {

  public FolderConnectionRegistry(final String name) {
    super("rgfolder", name);
    init();
  }

  public FolderConnectionRegistry(final String name, final boolean visible,
    final FolderConnection... connections) {
    super("rgfolder", name);
    setReadOnly(!visible);
    setVisible(visible);
    init();
    for (final FolderConnection connection : connections) {
      addConnection(connection);
    }
  }

  public FolderConnectionRegistry(final String name, final File directory) {
    super("rgfolder", name);
    setDirectory(directory);
    init();
  }

  public void addConnection(final FolderConnection connection) {
    final String name = connection.getName();
    addConnection(name, connection);
    if (!isReadOnly()) {
      final File file = getConnectionFile(name);
      if (file != null && (!file.exists() || file.canWrite())) {
        final FileSystemResource resource = new FileSystemResource(file);
        JsonMapIoFactory.write(connection.toMap(), resource);
      }
    }
  }

  public FolderConnection addConnection(final String name, final File file) {
    final FolderConnection connection = new FolderConnection(this, name, file);
    addConnection(connection);
    return connection;
  }

  @Override
  protected void doInit() {

  }

  @Override
  protected FolderConnection loadConnection(final File connectionFile) {
    final Map<String, ? extends Object> config = JsonMapIoFactory.toMap(connectionFile);
    String name = CollectionUtil.getString(config, "name");
    if (!StringUtils.hasText(name)) {
      name = FileUtil.getBaseName(connectionFile);
    }
    try {
      final String fileName = (String)config.get("file");

      return addConnection(name, FileUtil.getFile(fileName));
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error(
        "Error creating folder connection from: " + connectionFile, e);
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
