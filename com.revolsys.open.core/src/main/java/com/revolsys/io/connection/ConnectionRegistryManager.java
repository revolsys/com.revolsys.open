package com.revolsys.io.connection;

import java.beans.PropertyChangeListener;
import java.util.List;

import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.collection.NameProxy;
import com.revolsys.collection.Parent;

public interface ConnectionRegistryManager<T extends ConnectionRegistry<?>>
  extends PropertyChangeSupportProxy, PropertyChangeListener, Parent<T>, NameProxy {

  void addConnectionRegistry(T registry);

  @Override
  default List<T> getChildren() {
    return getVisibleConnectionRegistries();
  }

  List<T> getConnectionRegistries();

  T getConnectionRegistry(String name);

  List<T> getVisibleConnectionRegistries();

  void removeConnectionRegistry(T registry);
}
