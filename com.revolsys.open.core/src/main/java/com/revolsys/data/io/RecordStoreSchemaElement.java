package com.revolsys.data.io;

import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.io.ObjectWithProperties;

public interface RecordStoreSchemaElement extends ObjectWithProperties,
Comparable<RecordStoreSchemaElement> {
  boolean equalPath(String path);

  String getName();

  /**
   * Get the path of the object type. Names are described using a path (e.g.
   * /SCHEMA/TABLE).
   *
   * @return The name.
   */
  String getPath();

  <V extends RecordStore> V getRecordStore();

  RecordStoreSchema getSchema();
}
