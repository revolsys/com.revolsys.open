package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class OidFieldDefinition extends AbstractFileGdbFieldDefinition {
  public OidFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.INT,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        return row.getOid();
      }
    }
  }

  @Override
  public boolean isAutoCalculated() {
    return true;
  }

  @Override
  public void setPostInsertValue(final Record record, final Row row) {
    synchronized (row) {
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

  @Override
  public Object validate(final Object value) {
    return value;
  }
}
