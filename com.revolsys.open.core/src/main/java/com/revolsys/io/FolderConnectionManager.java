package com.revolsys.io;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.util.OperatingSystemUtil;

public class FolderConnectionManager implements PropertyChangeSupportProxy,
  PropertyChangeListener {

  private static final FolderConnectionManager INSTANCE;

  static {
    INSTANCE = new FolderConnectionManager();
    final File directory = OperatingSystemUtil.getUserApplicationDataDirectory("com.revolsys.gis/Folder Connections");
    INSTANCE.addConnectionRegistry("User", directory);
  }

  public static FolderConnectionManager get() {
    return INSTANCE;
  }

  public static File getConnection(final String name) {
    final FolderConnectionManager connectionManager = get();
    final List<FolderConnectionRegistry> registries = new ArrayList<FolderConnectionRegistry>(
      connectionManager.registries);
    Collections.reverse(registries);
    for (final FolderConnectionRegistry registry : registries) {
      final FolderConnection connection = registry.getConnection(name);
      if (connection != null) {
        return connection.getFile();
      }
    }
    return null;
  }

  private final List<FolderConnectionRegistry> registries = new ArrayList<FolderConnectionRegistry>();

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  public FolderConnectionManager() {
  }

  public synchronized void addConnectionRegistry(
    final FolderConnectionRegistry registry) {
    if (!registries.contains(registry)) {
      registries.add(registry);
      registry.getPropertyChangeSupport().addPropertyChangeListener(this);
    }
  }

  public synchronized void addConnectionRegistry(final String name) {
    addConnectionRegistry(new FolderConnectionRegistry(name));
  }

  public synchronized void addConnectionRegistry(final String name,
    final File directory) {
    addConnectionRegistry(new FolderConnectionRegistry(name, directory));
  }

  public List<FolderConnectionRegistry> getConnectionRegistries() {
    final List<FolderConnectionRegistry> registries = new ArrayList<FolderConnectionRegistry>();
    for (final FolderConnectionRegistry registry : this.registries) {
      if (registry.isVisible()) {
        registries.add(registry);
      }
    }
    return registries;
  }

  public FolderConnectionRegistry getConnectionRegistry(final String name) {
    for (final FolderConnectionRegistry registry : registries) {
      if (registry.getName().equals(name)) {
        return registry;
      }
    }
    return registries.get(0);
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  public List<FolderConnectionRegistry> getVisibleRegistries() {
    final List<FolderConnectionRegistry> registries = new ArrayList<FolderConnectionRegistry>();
    for (final FolderConnectionRegistry registry : this.registries) {
      if (registry.isVisible()) {
        registries.add(registry);
      }
    }
    return registries;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }

  public void removeConnectionRegistry(final FolderConnectionRegistry registry) {
    registries.remove(registry);
    registry.getPropertyChangeSupport().removePropertyChangeListener(this);
  }

  @Override
  public String toString() {
    return "Folder Connections";
  }
}
