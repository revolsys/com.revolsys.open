package com.revolsys.io.datastore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.data.io.RecordStoreFactoryRegistry;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.CollectionUtil;
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
    this.name = CollectionUtil.getString(config, "name");
    if (!Property.hasValue(this.name)) {
      this.name = FileUtil.getBaseName(resourceName);
    }
  }

  public RecordStoreConnection(final RecordStoreConnectionRegistry registry,
    final String name, final RecordStore recordStore) {
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

  public String getName() {
    return this.name;
  }

  public RecordStore getRecordStore() {
    synchronized (this) {
      if (this.recordStore == null) {
        try {
          final Map<String, Object> connectionProperties = CollectionUtil.get(
            this.config, "connection", Collections.<String, Object> emptyMap());
          if (connectionProperties.isEmpty()) {
            LoggerFactory.getLogger(getClass()).error(
              "Data store must include a 'connection' map property: "
                + this.name);
          } else {
            this.recordStore = RecordStoreFactoryRegistry.createRecordStore(connectionProperties);
            this.recordStore.initialize();
          }
        } catch (final Throwable e) {
          LoggerFactory.getLogger(getClass()).error(
            "Error creating data store for: " + this.name, e);
        }
      }
    }
    return this.recordStore;
  }

  public List<RecordStoreSchema> getSchemas() {
    final RecordStore recordStore = getRecordStore();
    if (recordStore == null) {
      return Collections.emptyList();
    } else {
      return recordStore.getSchemas();
    }
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
