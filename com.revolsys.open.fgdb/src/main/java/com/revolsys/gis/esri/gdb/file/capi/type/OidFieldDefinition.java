package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;

public class OidFieldDefinition extends AbstractFileGdbFieldDefinition {
  public OidFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.INT,
      BooleanStringConverter.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (getSync()) {
        return row.getOid();
      }
    }
  }

  @Override
  public void setPostInsertValue(final Record record, final Row row) {
    synchronized (getSync()) {
      final int oid = row.getOid();
      final String name = getName();
      record.setValue(name, oid);
    }
  }

  @Override
  public void setUpdateValue(final Record record, final Row row, final Object value) {
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
  }

}
