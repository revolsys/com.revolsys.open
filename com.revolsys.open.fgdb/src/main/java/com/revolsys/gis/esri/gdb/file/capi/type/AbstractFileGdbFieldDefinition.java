package com.revolsys.gis.esri.gdb.file.capi.type;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStoreImpl;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public abstract class AbstractFileGdbFieldDefinition extends FieldDefinition {

  private Reference<FileGdbRecordStoreImpl> recordStore;

  public AbstractFileGdbFieldDefinition(final String name, final DataType dataType,
    final boolean required) {
    super(name, dataType, required);
  }

  public AbstractFileGdbFieldDefinition(final String name, final DataType dataType,
    final int length, final boolean required) {
    super(name, dataType, length, required);
  }

  public FileGdbRecordStoreImpl getRecordStore() {
    if (this.recordStore == null) {
      return null;
    } else {
      return this.recordStore.get();
    }
  }

  public abstract Object getValue(Row row);

  public Object setInsertValue(final Record record, final Row row, final Object value) {
    return setValue(record, row, value);
  }

  public void setPostInsertValue(final Record record, final Row row) {
  }

  public void setRecordStore(final FileGdbRecordStoreImpl recordStore) {
    this.recordStore = new WeakReference<FileGdbRecordStoreImpl>(recordStore);
  }

  public Object setUpdateValue(final Record record, final Row row, final Object value) {
    return setValue(record, row, value);
  }

  public abstract Object setValue(Record record, Row row, Object value);
}
