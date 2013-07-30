package com.revolsys.io.file;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.connection.AbstractConnectionRegistryManager;
import com.revolsys.util.OperatingSystemUtil;

public class FolderConnectionManager extends
  AbstractConnectionRegistryManager<FolderConnectionRegistry> {

  private static final FolderConnectionManager INSTANCE;

  static {
    INSTANCE = new FolderConnectionManager();
    final File directory = OperatingSystemUtil.getUserApplicationDataDirectory("com.revolsys.gis/Folder Connections");
    INSTANCE.addConnectionRegistry("User", new FileSystemResource(directory));
  }

  public static FolderConnectionManager get() {
    return INSTANCE;
  }

  public static File getConnection(final String name) {
    final FolderConnectionManager connectionManager = get();
    final List<FolderConnectionRegistry> registries = connectionManager.getConnectionRegistries();
    Collections.reverse(registries);
    for (final FolderConnectionRegistry registry : registries) {
      final FolderConnection connection = registry.getConnection(name);
      if (connection != null) {
        return connection.getFile();
      }
    }
    return null;
  }

  public FolderConnectionManager() {
    super("Folder Connections");
  }

  public synchronized void addConnectionRegistry(final String name) {
    addConnectionRegistry(new FolderConnectionRegistry(this, name));
  }

  public synchronized FolderConnectionRegistry addConnectionRegistry(
    final String name, final Resource resource) {
    final FolderConnectionRegistry registry = new FolderConnectionRegistry(
      this, name, resource);
    addConnectionRegistry(registry);
    return registry;
  }

}
