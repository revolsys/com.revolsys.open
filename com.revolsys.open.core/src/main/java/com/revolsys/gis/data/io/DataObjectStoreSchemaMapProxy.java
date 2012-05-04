package com.revolsys.gis.data.io;

import java.util.Map;
import java.util.TreeMap;

public class DataObjectStoreSchemaMapProxy extends
  TreeMap<String, DataObjectStoreSchema> {

  /**
   * 
   */
  private static final long serialVersionUID = -1711922998363200190L;

  private final Map<String, DataObjectStoreSchema> map;

  private final AbstractDataObjectStore dataObjectStore;

  public DataObjectStoreSchemaMapProxy(
    final AbstractDataObjectStore dataObjectStore,
    final Map<String, DataObjectStoreSchema> map) {
    this.dataObjectStore = dataObjectStore;
    this.map = map;
  }

  @Override
  public DataObjectStoreSchema get(final Object key) {
    DataObjectStoreSchema schema = super.get(key);
    if (schema == null) {
      schema = map.get(key);
      if (schema != null) {
        final String path = schema.getPath();
        schema = new DataObjectStoreSchemaProxy(dataObjectStore, path, schema);
        super.put(path, schema);
      }
    }
    return schema;
  }

  @Override
  public DataObjectStoreSchema put(
    final String key,
    final DataObjectStoreSchema schema) {
    final DataObjectStoreSchemaProxy schemaProxy = new DataObjectStoreSchemaProxy(
      dataObjectStore, key, schema);
    map.put(key, schema);
    return super.put(key, schemaProxy);
  }
}
