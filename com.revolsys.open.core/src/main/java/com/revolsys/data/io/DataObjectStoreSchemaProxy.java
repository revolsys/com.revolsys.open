package com.revolsys.data.io;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;

public class DataObjectStoreSchemaProxy extends DataObjectStoreSchema {
  private final DataObjectStoreSchema schema;

  public DataObjectStoreSchemaProxy(
    final AbstractDataObjectStore dataObjectStore, final String name,
    final DataObjectStoreSchema schema) {
    super(dataObjectStore, name);
    this.schema = schema;
  }

  @Override
  public synchronized RecordDefinition findMetaData(final String typePath) {
    RecordDefinition metaData = super.findMetaData(typePath);
    if (metaData == null) {
      metaData = schema.findMetaData(typePath);
      if (metaData != null) {
        metaData = new RecordDefinitionImpl(getDataStore(), this,
          metaData);
        addMetaData(typePath, metaData);
      }
    }
    return metaData;
  }

  @Override
  public synchronized RecordDefinition getMetaData(final String typePath) {
    RecordDefinition metaData = findMetaData(typePath);
    if (metaData == null) {
      metaData = schema.getMetaData(typePath);
      if (metaData != null) {
        metaData = new RecordDefinitionImpl(getDataStore(), this,
          metaData);
        addMetaData(typePath, metaData);
      }
    }
    return metaData;
  }
}
