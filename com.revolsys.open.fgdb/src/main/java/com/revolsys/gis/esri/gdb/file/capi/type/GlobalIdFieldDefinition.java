package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.Guid;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class GlobalIdFieldDefinition extends AbstractFileGdbFieldDefinition {
  public GlobalIdFieldDefinition(final int fieldNumber, final Field field) {
    this(fieldNumber, field.getName(), field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  public GlobalIdFieldDefinition(final int fieldNumber, final String name, final int length,
    final boolean required) {
    super(fieldNumber, name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      final Guid guid = row.getGlobalId();
      return guid.toString();
    }
  }

  @Override
  public boolean isAutoCalculated() {
    return true;
  }

  @Override
  public void setPostInsertValue(final Record record, final Row row) {
    synchronized (row) {
      final Guid guid = row.getGlobalId();
      final String name = getName();
      final String string = guid.toString();
      record.setValue(name, string);
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
  }
}
