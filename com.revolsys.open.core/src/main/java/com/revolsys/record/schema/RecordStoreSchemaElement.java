package com.revolsys.record.schema;

import com.revolsys.collection.NameProxy;
import com.revolsys.geometry.model.GeometryFactoryProxy;
import com.revolsys.io.PathName;
import com.revolsys.io.PathNameProxy;
import com.revolsys.properties.ObjectWithProperties;
import com.revolsys.util.IconNameProxy;

public interface RecordStoreSchemaElement extends GeometryFactoryProxy, ObjectWithProperties,
  PathNameProxy, Comparable<RecordStoreSchemaElement>, NameProxy, IconNameProxy {

  default boolean equalPath(final PathName path) {
    return getPathName().equals(path);
  }

  /**
   * Get the path of the object type. Names are described using a path (e.g.
   * /SCHEMA/TABLE).
   *
   * @return The name.
   */
  String getPath();

  <R extends RecordStore> R getRecordStore();

  RecordStoreSchema getSchema();

  default boolean isClosed() {
    final RecordStoreSchema schema = getSchema();
    if (schema == null) {
      return true;
    } else {
      return false;
    }
  }
}
