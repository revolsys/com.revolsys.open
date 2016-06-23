package com.revolsys.io.connection;

import java.io.File;
import java.util.Map;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.io.FileUtil;
import com.revolsys.properties.BaseObjectWithProperties;
import com.revolsys.util.Property;

public abstract class AbstractConnection<C extends Connection, R extends ConnectionRegistry<C>>
  extends BaseObjectWithProperties implements Connection {
  private MapEx config = new LinkedHashMapEx();

  private String name;

  private R registry;

  private File connectionFile;

  public AbstractConnection(final R registry, final String name) {
    this.registry = registry;
    this.name = name;
  }

  public AbstractConnection(final R registry, final String resourceName,
    final Map<String, ? extends Object> config) {
    this.registry = registry;
    setProperties(config);
    if (!Property.hasValue(this.name)) {
      this.name = FileUtil.getBaseName(resourceName);
    }
  }

  @Override
  public void deleteConnection() {
    if (this.registry != null) {
      this.registry.removeConnection(this);
    }
    this.config = null;
    this.name = null;
    this.registry = null;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof AbstractConnection) {
      final AbstractConnection<?, ?> connection = (AbstractConnection<?, ?>)obj;
      if (this.registry == connection.getRegistry()) {
        if (DataType.equal(this.name, connection.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  public File getConnectionFile() {
    return this.connectionFile;
  }

  @Override
  public String getName() {
    return this.name;
  }

  public R getRegistry() {
    return this.registry;
  }

  @Override
  public int hashCode() {
    if (this.name == null) {
      return 0;
    } else {
      return this.name.hashCode();
    }
  }

  @Override
  public boolean isReadOnly() {
    if (this.registry == null) {
      return true;
    } else {
      return this.registry.isReadOnly();
    }
  }

  public void setConnectionFile(final File connectionFile) {
    this.connectionFile = connectionFile;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public MapEx toMap() {
    return this.config;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
