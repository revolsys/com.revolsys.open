package com.revolsys.io.connection;

import java.beans.PropertyChangeListener;
import java.util.List;

import com.revolsys.beans.PropertyChangeSupportProxy;

public interface ConnectionRegistryManager<T extends ConnectionRegistry<?>> extends
  PropertyChangeSupportProxy, PropertyChangeListener {

  void addConnectionRegistry(T registry);

  List<T> getConnectionRegistries();

  T getConnectionRegistry(String name);

  String getName();

  List<T> getVisibleConnectionRegistries();

  void removeConnectionRegistry(T registry);
}
