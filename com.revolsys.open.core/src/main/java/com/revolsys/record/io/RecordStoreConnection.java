package com.revolsys.record.io;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.record.schema.RecordStore;
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
    this.config = new LinkedHashMap<String, Object>(config);
    this.name = Maps.getString(config, "name");
    if (!Property.hasValue(this.name)) {
      this.name = FileUtil.getBaseName(resourceName);
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
      if (this.recordStore == null) {
        try {
          final Map<String, Object> connectionProperties = Maps.get(this.config, "connection",
            Collections.<String, Object> emptyMap());
          if (connectionProperties.isEmpty()) {
            LoggerFactory.getLogger(getClass())
              .error("Record store must include a 'connection' map property: " + this.name);
          } else {
            this.recordStore = RecordStoreFactoryRegistry.newRecordStore(connectionProperties);
            this.recordStore.initialize();
          }
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error("Error creating record store for: " + this.name,
            e);
        }
      }
    }
    return this.recordStore;
  }

  public boolean isReadOnly() {
    if (this.registry == null) {
      return true;
    } else {
      return this.registry.isReadOnly();
    }
  }

  @Override
  public Map<String, Object> toMap() {
    return this.config;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
