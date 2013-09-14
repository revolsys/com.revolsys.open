package com.revolsys.io.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.connection.AbstractConnectionRegistryManager;
import com.revolsys.util.OS;

public class FolderConnectionManager extends
  AbstractConnectionRegistryManager<FolderConnectionRegistry, FolderConnection> {

  private static final FolderConnectionManager INSTANCE;

  static {
    INSTANCE = new FolderConnectionManager();
    final File directory = OS.getApplicationDataDirectory("com.revolsys.gis/Folder Connections");
    INSTANCE.addConnectionRegistry("User", new FileSystemResource(directory));
  }

  public static FolderConnectionManager get() {
    return INSTANCE;
  }

  public static File getConnection(final String name) {
    final FolderConnectionManager connectionManager = get();
    final List<FolderConnectionRegistry> registries = new ArrayList<FolderConnectionRegistry>();
    registries.addAll(connectionManager.getConnectionRegistries());
    final FolderConnectionRegistry threadRegistry = FolderConnectionRegistry.getForThread();
    if (threadRegistry != null) {
      registries.add(threadRegistry);
    }
    Collections.reverse(registries);
    for (final FolderConnectionRegistry registry : registries) {
      final FolderConnection connection = registry.getConnection(name);
      if (connection != null) {
        return connection.getFile().getFile();
      }
    }
    return null;
  }

  public FolderConnectionManager() {
    super("Folder Connections");
  }

  public synchronized FolderConnectionRegistry addConnectionRegistry(
    final String name) {
    final FolderConnectionRegistry registry = new FolderConnectionRegistry(
      this, name);
    addConnectionRegistry(registry);
    return registry;
  }

  public synchronized FolderConnectionRegistry addConnectionRegistry(
    final String name, final Resource resource) {
    final FolderConnectionRegistry registry = new FolderConnectionRegistry(
      this, name, resource, false);
    addConnectionRegistry(registry);
    return registry;
  }

}
