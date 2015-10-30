package com.revolsys.io.connection;

import java.util.List;
import java.util.Map;

import com.revolsys.beans.PropertyChangeSupportProxy;

public interface ConnectionRegistry<T> extends PropertyChangeSupportProxy {
  List<T> getConections();

  T getConnection(final String connectionName);

  ConnectionRegistryManager<ConnectionRegistry<T>> getConnectionManager();

  List<String> getConnectionNames();

  String getName();

  boolean isVisible();

  void newConnection(Map<String, ? extends Object> connectionParameters);

  void setConnectionManager(
    ConnectionRegistryManager<? extends ConnectionRegistry<T>> connectionManager);
}
