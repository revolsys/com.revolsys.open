package com.revolsys.gis.data.store;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.util.StringUtils;

public abstract class AbstractConnectionRegistry<T> implements
  ConnectionRegistry<T> {

  private Map<String, T> connections;

  private final Set<String> connectionNames = new TreeSet<String>();

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private final String name;

  private boolean readOnly;

  public AbstractConnectionRegistry(final String name) {
    this.name = name;
  }

  protected void addConnection(final String name, final T connection) {
    if (connection != null) {
      this.connectionNames.add(name);
      final String lowerName = name.toLowerCase();
      this.connections.put(lowerName, connection);
    }
  }

  protected abstract void doInit();

  @Override
  public List<T> getConections() {
    return new ArrayList<T>(connections.values());
  }

  @Override
  public T getConnection(final String connectionName) {
    if (StringUtils.hasText(connectionName)) {
      return connections.get(connectionName.toLowerCase());
    } else {
      return null;
    }
  }

  @Override
  public List<String> getConnectionNames() {
    return new ArrayList<String>(connectionNames);
  }

  public String getName() {
    return name;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return propertyChangeSupport;
  }

  protected synchronized void init() {
    connections = new TreeMap<String, T>();
    doInit();
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  protected void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public String toString() {
    return getName();
  }
}
