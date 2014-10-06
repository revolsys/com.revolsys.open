package com.revolsys.gis.esri.gdb.file.capi.type;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public abstract class AbstractFileGdbFieldDefinition extends FieldDefinition {

  private Reference<CapiFileGdbRecordStore> recordStore;

  public AbstractFileGdbFieldDefinition(final String name, final DataType dataType,
    final boolean required) {
    super(name, dataType, required);
  }

  public AbstractFileGdbFieldDefinition(final String name, final DataType dataType,
    final int length, final boolean required) {
    super(name, dataType, length, required);
  }

  public CapiFileGdbRecordStore getRecordStore() {
    if (recordStore == null) {
      return null;
    } else {
      return recordStore.get();
    }
  }

  public abstract Object getValue(Row row);

  public void setRecordStore(final CapiFileGdbRecordStore recordStore) {
    this.recordStore = new WeakReference<CapiFileGdbRecordStore>(recordStore);
  }

  public Object setInsertValue(final Record object, final Row row,
    final Object value) {
    return setValue(object, row, value);
  }

  public void setPostInsertValue(final Record object, final Row row) {
  }

  public Object setUpdateValue(final Record object, final Row row,
    final Object value) {
    return setValue(object, row, value);
  }

  public abstract Object setValue(Record object, Row row, Object value);
}
