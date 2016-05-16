package com.revolsys.record.io;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.connection.AbstractConnection;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;
import com.revolsys.record.schema.RecordStoreSchemaElement;
import com.revolsys.util.Exceptions;

public class RecordStoreConnection
  extends AbstractConnection<RecordStoreConnection, RecordStoreConnectionRegistry>
  implements Parent<RecordStoreSchemaElement> {
  private RecordStore recordStore;

  private boolean savePassword;

  public RecordStoreConnection(final RecordStoreConnectionRegistry registry,
    final String resourceName, final Map<String, ? extends Object> config) {
    super(registry, resourceName, config);
    if (MapObjectFactory.getType(this) == null) {
      setProperty(MapObjectFactory.TYPE, "recordStore");
    }
  }

  public RecordStoreConnection(final RecordStoreConnectionRegistry registry, final String name,
    final RecordStore recordStore) {
    super(registry, name);
    this.recordStore = recordStore;
  }

  @Override
  public void deleteConnection() {
    super.deleteConnection();
    this.recordStore = null;
  }

  @Override
  public List<RecordStoreSchemaElement> getChildren() {
    final RecordStore recordStore = getRecordStore();
    if (recordStore != null) {
      final RecordStoreSchema rootSchema = recordStore.getRootSchema();
      if (rootSchema != null) {
        return rootSchema.getElements();
      }
    }
    return Collections.emptyList();
  }

  @Override
  public String getIconName() {
    return "database";
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
            this.recordStore = MapObjectFactory.toObject(this.getProperties());
            this.recordStore.setRecordStoreConnection(this);
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

  public boolean isSavePassword() {
    return this.savePassword;
  }

  public void setSavePassword(final boolean savePassword) {
    this.savePassword = savePassword;
  }

  @SuppressWarnings("unchecked")
  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap("recordStore");
    addAllToMap(map, getProperties());
    if (!isSavePassword()) {
      final Map<String, Object> connection = (Map<String, Object>)map.get("connection");
      connection.remove("password");
    }
    return map;
  }
}
