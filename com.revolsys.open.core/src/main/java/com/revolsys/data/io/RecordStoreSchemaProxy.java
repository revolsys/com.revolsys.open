package com.revolsys.data.io;

import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordDefinitionImpl;

public class RecordStoreSchemaProxy extends RecordStoreSchema {
  private final RecordStoreSchema schema;

  public RecordStoreSchemaProxy(
    final AbstractRecordStore recordStore, final String name,
    final RecordStoreSchema schema) {
    super(recordStore, name);
    this.schema = schema;
  }

  @Override
  public synchronized RecordDefinition findMetaData(final String typePath) {
    RecordDefinition recordDefinition = super.findMetaData(typePath);
    if (recordDefinition == null) {
      recordDefinition = schema.findMetaData(typePath);
      if (recordDefinition != null) {
        recordDefinition = new RecordDefinitionImpl(getRecordStore(), this,
          recordDefinition);
        addMetaData(typePath, recordDefinition);
      }
    }
    return recordDefinition;
  }

  @Override
  public synchronized RecordDefinition getRecordDefinition(final String typePath) {
    RecordDefinition recordDefinition = findMetaData(typePath);
    if (recordDefinition == null) {
      recordDefinition = schema.getRecordDefinition(typePath);
      if (recordDefinition != null) {
        recordDefinition = new RecordDefinitionImpl(getRecordStore(), this,
          recordDefinition);
        addMetaData(typePath, recordDefinition);
      }
    }
    return recordDefinition;
  }
}
