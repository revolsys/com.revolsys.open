package com.revolsys.gis.data.store;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.util.OperatingSystemUtil;

public class DataObjectStoreConnectionManager implements
  PropertyChangeSupportProxy, PropertyChangeListener {

  private static final DataObjectStoreConnectionManager INSTANCE;

  static {
    INSTANCE = new DataObjectStoreConnectionManager();
    final File dataStoresDirectory = OperatingSystemUtil.getUserApplicationDataDirectory("com.revolsys.gis/Data Stores");
    final DataObjectStoreConnectionRegistry registry = new DataObjectStoreConnectionRegistry(
      "User", dataStoresDirectory);
    INSTANCE.addConnectionRegistry(registry);
  }

  public static DataObjectStoreConnectionManager get() {
    return INSTANCE;
  }

  public static DataObjectStore getConnection(final String name) {
    final DataObjectStoreConnectionManager connectionManager = get();
    final List<DataObjectStoreConnectionRegistry> registries = new ArrayList<DataObjectStoreConnectionRegistry>(
      connectionManager.registries);
    Collections.reverse(registries);
    for (final DataObjectStoreConnectionRegistry registry : registries) {
      final DataObjectStoreConnection dataStoreConnection = registry.getConnection(name);
      if (dataStoreConnection != null) {
        return dataStoreConnection.getDataStore();
      }
    }
    return null;
  }

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private final List<DataObjectStoreConnectionRegistry> registries = new ArrayList<DataObjectStoreConnectionRegistry>();

  public DataObjectStoreConnectionManager() {
  }

  public void addConnectionRegistry(
    final DataObjectStoreConnectionRegistry registry) {
    if (!registries.contains(registry)) {
      registries.add(registry);
      registry.getPropertyChangeSupport().addPropertyChangeListener(this);
    }
  }

  public List<DataObjectStoreConnectionRegistry> getConnectionRegistries() {
    final List<DataObjectStoreConnectionRegistry> registries = new ArrayList<DataObjectStoreConnectionRegistry>();
    for (final DataObjectStoreConnectionRegistry registry : this.registries) {
      if (registry.isVisible()) {
        registries.add(registry);
      }
    }
    return registries;
  }

  public DataObjectStoreConnectionRegistry getConnectionRegistry(
    final String name) {
    for (final DataObjectStoreConnectionRegistry registry : registries) {
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

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }

  public void removeConnectionRegistry(
    final DataObjectStoreConnectionRegistry registry) {
    registries.remove(registry);
    registry.getPropertyChangeSupport().removePropertyChangeListener(this);
  }

  @Override
  public String toString() {
    return "Data Stores";
  }
}
