package com.revolsys.gis.esri.gdb.file.capi.type;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.datatype.DataType;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;

public abstract class AbstractFileGdbFieldDefinition extends FieldDefinition {

  private Reference<FileGdbRecordStore> recordStore;

  public AbstractFileGdbFieldDefinition(final String name, final DataType dataType,
    final boolean required) {
    super(name, dataType, required);
  }

  public AbstractFileGdbFieldDefinition(final String name, final DataType dataType,
    final int length, final boolean required) {
    super(name, dataType, length, required);
  }

  public FileGdbRecordStore getRecordStore() {
    if (this.recordStore == null) {
      return null;
    } else {
      return this.recordStore.get();
    }
  }

  public Object getSync() {
    return getRecordStore().getApiSync();
  }

  public abstract Object getValue(Row row);

  public boolean isAutoCalculated() {
    return false;
  }

  public void setInsertValue(final Record record, final Row row, final Object value) {
    setValue(record, row, value);
  }

  protected void setNull(final Row row) {
    final String name = getName();
    if (isRequired()) {
      throw new IllegalArgumentException(name + " is required and cannot be null");
    } else {
      row.setNull(name);
    }
  }

  public void setPostInsertValue(final Record record, final Row row) {
  }

  public void setRecordStore(final FileGdbRecordStore recordStore) {
    this.recordStore = new WeakReference<FileGdbRecordStore>(recordStore);
  }

  public void setUpdateValue(final Record record, final Row row, final Object value) {
    setValue(record, row, value);
  }

  public abstract void setValue(Record record, Row row, Object value);
}
