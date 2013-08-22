package com.revolsys.io.connection;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.util.CollectionUtil;

public abstract class AbstractConnectionRegistry<T> implements
  ConnectionRegistry<T>, PropertyChangeListener {

  private Map<String, T> connections;

  private boolean visible = true;

  private final Map<String, String> connectionNames = new TreeMap<String, String>();

  private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(
    this);

  private File directory;

  private final String name;

  private boolean readOnly;

  private final String fileExtension = "rgobject";

  private ConnectionRegistryManager<ConnectionRegistry<T>> connectionManager;

  public AbstractConnectionRegistry(
    final ConnectionRegistryManager<? extends ConnectionRegistry<T>> connectionManager,
    final String name) {
    this.name = name;
    setConnectionManager(connectionManager);
  }

  protected synchronized void addConnection(final String name,
    final T connection) {
    if (connection != null && name != null) {
      final String lowerName = name.toLowerCase();
      final T existingConnection = this.connections.get(lowerName);
      removeConnection(existingConnection);
      this.connectionNames.put(lowerName, name);
      this.connections.put(lowerName, connection);
      if (connection instanceof PropertyChangeSupportProxy) {
        final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)connection;
        final PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
        if (propertyChangeSupport != null) {
          propertyChangeSupport.addPropertyChangeListener(this);
        }
      }
      final int index = getConnectionIndex(name);
      propertyChangeSupport.fireIndexedPropertyChange("connections", index,
        null, connection);
    }
  }

  @Override
  public void createConnection(
    final Map<String, ? extends Object> connectionParameters) {
    final String name = CollectionUtil.getString(connectionParameters, "name");
    final File file = getConnectionFile(name);
    if (file != null && (!file.exists() || file.canRead())) {
      final FileSystemResource resource = new FileSystemResource(file);
      JsonMapIoFactory.write(connectionParameters, resource, true);
      loadConnection(file);
    }
  }

  protected void doInit() {
    if (directory != null && directory.isDirectory()) {
      for (final File connectionFile : FileUtil.getFilesByExtension(directory,
        fileExtension)) {
        loadConnection(connectionFile);
      }
    }
  }

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

  protected File getConnectionFile(final String name) {
    if (!directory.exists()) {
      if (isReadOnly()) {
        return null;
      } else if (!directory.mkdirs()) {
        return null;
      }
    }
    final String fileName = name.replaceAll("[^a-zA-Z0-9\\-_ ]", "_") + "."
      + fileExtension;
    final File file = new File(directory, fileName);
    return file;
  }

  protected int getConnectionIndex(final String name) {
    final String lowerName = name.toLowerCase();
    final int index = new ArrayList<String>(connectionNames.keySet()).indexOf(lowerName);
    return index;
  }

  @Override
  public ConnectionRegistryManager<ConnectionRegistry<T>> getConnectionManager() {
    return connectionManager;
  }

  @Override
  public List<String> getConnectionNames() {
    final List<String> names = new ArrayList<String>(connectionNames.values());
    return names;
  }

  public File getDirectory() {
    return directory;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  @Override
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

  @Override
  public boolean isVisible() {
    return visible;
  }

  protected abstract T loadConnection(final File connectionFile);

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    propertyChangeSupport.firePropertyChange(event);
  }

  protected synchronized boolean removeConnection(final String name,
    final T connection) {
    if (connection != null && name != null) {
      final String lowerName = name.toLowerCase();
      final T existingConnection = this.connections.get(lowerName);
      if (existingConnection == connection) {
        final int index = getConnectionIndex(name);
        this.connectionNames.remove(lowerName);
        this.connections.remove(lowerName);
        if (connection instanceof PropertyChangeSupportProxy) {
          final PropertyChangeSupportProxy proxy = (PropertyChangeSupportProxy)connection;
          final PropertyChangeSupport propertyChangeSupport = proxy.getPropertyChangeSupport();
          if (propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(this);
          }
        }
        propertyChangeSupport.fireIndexedPropertyChange("connections", index,
          connection, null);
        if (directory != null && !readOnly) {
          final File file = getConnectionFile(name);
          file.delete();
        }
        return true;
      }
    }
    return false;
  }

  protected abstract boolean removeConnection(T connection);

  @Override
  public void setConnectionManager(
    final ConnectionRegistryManager<? extends ConnectionRegistry<T>> connectionManager) {
    if (this.connectionManager != connectionManager) {
      if (this.connectionManager != null) {
        propertyChangeSupport.removePropertyChangeListener(connectionManager);
      }
      this.connectionManager = (ConnectionRegistryManager)connectionManager;
      if (connectionManager != null) {
        propertyChangeSupport.addPropertyChangeListener(connectionManager);
      }
    }
  }

  protected void setDirectory(final Resource resource) {
    if (resource instanceof FileSystemResource) {
      final FileSystemResource fileResource = (FileSystemResource)resource;
      final File directory = fileResource.getFile();
      boolean readOnly;
      if (resource.exists()) {
        readOnly = !directory.canWrite();
      } else if (directory.mkdirs()) {
        readOnly = false;
      } else {
        readOnly = true;
      }
      setReadOnly(readOnly);
      this.directory = directory;
    } else {
      setReadOnly(true);
      this.directory = null;
    }
  }

  public void setReadOnly(final boolean readOnly) {
    if (this.isReadOnly() && !readOnly) {
      throw new IllegalArgumentException(
        "Cannot make a read only registry not read only");
    }
    this.readOnly = readOnly;
  }

  public void setVisible(final boolean visible) {
    this.visible = visible;
  }

  @Override
  public String toString() {
    return getName();
  }
}
