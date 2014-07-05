package com.revolsys.data.io;

import java.util.Map;
import java.util.TreeMap;

public class RecordStoreSchemaMapProxy extends
  TreeMap<String, RecordStoreSchema> {

  /**
   * 
   */
  private static final long serialVersionUID = -1711922998363200190L;

  private final Map<String, RecordStoreSchema> map;

  private final AbstractRecordStore dataObjectStore;

  public RecordStoreSchemaMapProxy(
    final AbstractRecordStore dataObjectStore,
    final Map<String, RecordStoreSchema> map) {
    this.dataObjectStore = dataObjectStore;
    this.map = map;
  }

  @Override
  public RecordStoreSchema get(final Object key) {
    RecordStoreSchema schema = super.get(key);
    if (schema == null) {
      schema = map.get(key);
      if (schema != null) {
        final String path = schema.getPath();
        schema = new RecordStoreSchemaProxy(dataObjectStore, path, schema);
        super.put(path, schema);
      }
    }
    return schema;
  }

  @Override
  public RecordStoreSchema put(final String key,
    final RecordStoreSchema schema) {
    final RecordStoreSchemaProxy schemaProxy = new RecordStoreSchemaProxy(
      dataObjectStore, key, schema);
    map.put(key, schema);
    return super.put(key, schemaProxy);
  }
}
