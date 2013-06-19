package com.revolsys.gis.data.store;

import java.util.List;
import java.util.Map;

import com.revolsys.beans.PropertyChangeSupportProxy;

public interface ConnectionRegistry<T> extends PropertyChangeSupportProxy {
  void createConnection(Map<String, ? extends Object> connectionParameters);

  List<T> getConections();

  T getConnection(final String connectionName);

  List<String> getConnectionNames();

  String getName();
}
