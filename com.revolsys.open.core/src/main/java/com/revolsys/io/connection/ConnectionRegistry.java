package com.revolsys.io.connection;

import java.util.List;
import java.util.Map;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.NameProxy;
import com.revolsys.collection.Parent;

public interface ConnectionRegistry<T> extends PropertyChangeSupportProxy, Parent<T>, NameProxy {
  @Override
  default List<T> getChildren() {
    return getConnections();
  }

  T getConnection(final String connectionName);

  ConnectionRegistryManager<ConnectionRegistry<T>> getConnectionManager();

  List<String> getConnectionNames();

  List<T> getConnections();

  boolean isVisible();

  T newConnection(Map<String, ? extends Object> connectionParameters);

  void setConnectionManager(
    ConnectionRegistryManager<? extends ConnectionRegistry<T>> connectionManager);
}
