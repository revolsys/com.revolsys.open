package com.revolsys.io.connection;

import java.io.File;

import com.revolsys.collection.NameProxy;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.IconNameProxy;

public interface Connection extends MapSerializer, NameProxy, IconNameProxy {
  void deleteConnection();

  File getConnectionFile();

  default boolean isEditable() {
    return !isReadOnly();
  }

  default boolean isExists() {
    return true;
  }

  boolean isReadOnly();
}
