package com.revolsys.io.connection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

public class AbstractConnectionRegistryManager<T extends ConnectionRegistry<V>, V>
  implements ConnectionRegistryManager<T> {

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  private final List<T> registries = new ArrayList<T>();

  private final String name;

  public AbstractConnectionRegistryManager(final String name) {
    this.name = name;
  }

  @Override
  public void addConnectionRegistry(final T registry) {
    if (registry != null) {
      int index = -1;
      synchronized (this.registries) {
        if (!this.registries.contains(registry)) {
          index = this.registries.size();
          this.registries.add(registry);
          registry.setConnectionManager(this);
        }
      }
      if (index != -1) {
        index = getVisibleConnectionRegistries().indexOf(registry);
        if (index != -1) {
          this.propertyChangeSupport.fireIndexedPropertyChange("registries", index, null, registry);
        }
      }
    }
  }

  protected T findConnectionRegistry(final String name) {
    for (final T registry : this.registries) {
      if (registry.getName().equals(name)) {
        return registry;
      }
    }
    return null;
  }

  @Override
  public List<T> getConnectionRegistries() {
    return new ArrayList<T>(this.registries);
  }

  @Override
  public T getConnectionRegistry(final String name) {
    final T connectionRegistry = findConnectionRegistry(name);
    if (connectionRegistry == null) {
      return this.registries.get(0);
    }
    return connectionRegistry;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  @Override
  public List<T> getVisibleConnectionRegistries() {
    final List<T> registries = new ArrayList<T>();
    for (final T registry : this.registries) {
      if (registry != null && registry.isVisible()) {
        registries.add(registry);
      }
    }
    return registries;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    this.propertyChangeSupport.firePropertyChange(event);
  }

  public void removeConnectionRegistry(final String name) {
    final T connectionRegistry = findConnectionRegistry(name);
    removeConnectionRegistry(connectionRegistry);
  }

  @Override
  public void removeConnectionRegistry(final T registry) {
    if (registry != null) {
      int index;
      synchronized (this.registries) {
        index = this.registries.indexOf(registry);
        if (index != -1) {
          this.registries.remove(registry);
          registry.setConnectionManager(null);
          registry.getPropertyChangeSupport().removePropertyChangeListener(this);
        }
      }
      if (index != -1) {
        this.propertyChangeSupport.fireIndexedPropertyChange("registries", index, registry, null);
      }
    }
  }

  @Override
  public String toString() {
    return this.name;
  }
}
