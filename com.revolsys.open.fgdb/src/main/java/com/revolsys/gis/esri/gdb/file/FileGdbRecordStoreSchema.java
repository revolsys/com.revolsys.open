package com.revolsys.gis.esri.gdb.file;

import org.jeometry.common.io.PathName;

import com.revolsys.record.schema.RecordStore;
import com.revolsys.record.schema.RecordStoreSchema;

public class FileGdbRecordStoreSchema extends RecordStoreSchema {

  private final String catalogPath;

  private final FileGdbRecordStore recordStore;

  public FileGdbRecordStoreSchema(final FileGdbRecordStore recordStore) {
    super(recordStore);
    this.recordStore = recordStore;
    this.catalogPath = "\\";
  }

  public FileGdbRecordStoreSchema(final FileGdbRecordStoreSchema parentSchema,
    final PathName childSchemaPath) {
    super(parentSchema, childSchemaPath);
    this.recordStore = parentSchema.getRecordStore();
    this.catalogPath = FileGdbRecordStore.toCatalogPath(childSchemaPath);
  }

  public String getCatalogPath() {
    return this.catalogPath;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends RecordStore> V getRecordStore() {
    return (V)this.recordStore;
  }

  @Override
  public boolean equalsRecordStore(final RecordStore recordStore) {
    return recordStore == this.recordStore;
  }

}
