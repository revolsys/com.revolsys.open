package com.revolsys.io.connection;

import java.nio.file.Path;

import com.revolsys.collection.NameProxy;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.IconNameProxy;

public interface Connection extends MapSerializer, NameProxy, IconNameProxy {
  void deleteConnection();

  Path getConnectionFile();

  ConnectionRegistry<?> getRegistry();

  default boolean isEditable() {
    return !isReadOnly();
  }

  default boolean isExists() {
    return true;
  }

  boolean isReadOnly();
}
