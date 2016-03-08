package com.revolsys.record.io;

import java.util.Map;
import java.util.function.Function;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Exceptions;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class RecordStoreConnection implements MapSerializer {
  private Map<String, Object> config;

  private String name;

  private RecordStore recordStore;

  private RecordStoreConnectionRegistry registry;

  public RecordStoreConnection(final RecordStoreConnectionRegistry registry,
    final String resourceName, final Map<String, ? extends Object> config) {
    this.registry = registry;
    setConfig(config);
    if (!Property.hasValue(this.name)) {
      this.name = FileUtil.getBaseName(resourceName);
    }
    if (!config.containsKey("type") || !config.containsKey(MapObjectFactory.TYPE)) {
      this.config.put(MapObjectFactory.TYPE, "recordStore");
    }
  }

  public RecordStoreConnection(final RecordStoreConnectionRegistry registry, final String name,
    final RecordStore recordStore) {
    this.registry = registry;
    this.name = name;
    this.recordStore = recordStore;
  }

  public void delete() {
    if (this.registry != null) {
      this.registry.removeConnection(this);
    }
    this.config = null;
    this.recordStore = null;
    this.name = null;
    this.registry = null;

  }

  public Map<String, Object> getConfig() {
    return JavaBeanUtil.clone(this.config);
  }

  public String getName() {
    return this.name;
  }

  public RecordStore getRecordStore() {
    synchronized (this) {
      if (this.recordStore == null || this.recordStore.isClosed()) {
        this.recordStore = null;
        final Function<RecordStoreConnection, Boolean> invalidRecordStoreFunction = RecordStoreConnectionManager
          .getInvalidRecordStoreFunction();
        Throwable savedException = null;
        do {
          try {
            this.recordStore = MapObjectFactory.toObject(this.config);
            return this.recordStore;
          } catch (final Throwable e) {
            savedException = e;
          }
        } while (invalidRecordStoreFunction != null && invalidRecordStoreFunction.apply(this));
        Exceptions.throwUncheckedException(savedException);
      }
    }
    return this.recordStore;
  }

  public RecordStoreConnectionRegistry getRegistry() {
    return this.registry;
  }

  public boolean isReadOnly() {
    if (this.registry == null) {
      return true;
    } else {
      return this.registry.isReadOnly();
    }
  }

  public boolean isSavePassword() {
    @SuppressWarnings("unchecked")
    final Map<String, Object> connection = (Map<String, Object>)this.config.get("connection");
    return Maps.getBool(connection, "savePassword");
  }

  public void setConfig(final Map<String, ? extends Object> config) {
    this.config = Maps.newLinkedHash(config);
    this.name = Maps.getString(this.config, "name", this.name);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = newTypeMap("recordStore");
    addAllToMap(map, this.config);
    if (!isSavePassword()) {
      final Map<String, Object> connection = (Map<String, Object>)map.get("connection");
      connection.remove("password");
    }
    return map;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
