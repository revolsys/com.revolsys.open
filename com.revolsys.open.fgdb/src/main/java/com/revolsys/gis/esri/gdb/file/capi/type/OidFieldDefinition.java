package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class OidFieldDefinition extends AbstractFileGdbFieldDefinition {
  public OidFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.INT, BooleanStringConverter.getBoolean(field.getRequired())
      || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final CapiFileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (recordStore) {
        return row.getOid();
      }
    }
  }

  @Override
  public void setPostInsertValue(final Record object, final Row row) {
    synchronized (getRecordStore()) {
      final int oid = row.getOid();
      final String name = getName();
      object.setValue(name, oid);
    }
  }

  @Override
  public Object setUpdateValue(final Record object, final Row row, final Object value) {
    return value;
  }

  @Override
  public Object setValue(final Record object, final Row row, final Object value) {
    return null;
  }

}
