package com.revolsys.io.connection;

import java.util.List;
import java.util.Map;

import com.revolsys.beans.PropertyChangeSupportProxy;

public interface ConnectionRegistry<T> extends PropertyChangeSupportProxy {
  void createConnection(Map<String, ? extends Object> connectionParameters);

  List<T> getConections();

  T getConnection(final String connectionName);

  ConnectionRegistryManager<ConnectionRegistry<T>> getConnectionManager();

  List<String> getConnectionNames();

  String getName();

  boolean isVisible();
}
