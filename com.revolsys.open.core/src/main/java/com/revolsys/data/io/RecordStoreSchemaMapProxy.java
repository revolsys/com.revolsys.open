package com.revolsys.data.io;

import java.util.Map;
import java.util.TreeMap;

import com.revolsys.data.record.schema.RecordStoreSchema;

public class RecordStoreSchemaMapProxy extends
TreeMap<String, RecordStoreSchema> {

  /**
   *
   */
  private static final long serialVersionUID = -1711922998363200190L;

  private final Map<String, RecordStoreSchema> map;

  public RecordStoreSchemaMapProxy(final Map<String, RecordStoreSchema> map) {
    this.map = map;
  }

  @Override
  public RecordStoreSchema get(final Object key) {
    RecordStoreSchema schema = super.get(key);
    if (schema == null) {
      schema = this.map.get(key);
      if (schema != null) {
        final String path = schema.getPath();
        schema = new RecordStoreSchemaProxy(path, schema);
        super.put(path, schema);
      }
    }
    return schema;
  }

  @Override
  public RecordStoreSchema put(final String path, final RecordStoreSchema schema) {
    final RecordStoreSchemaProxy schemaProxy = new RecordStoreSchemaProxy(path,
      schema);
    this.map.put(path, schema);
    return super.put(path, schemaProxy);
  }
}
