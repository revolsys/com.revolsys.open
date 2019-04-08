package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class ShortFieldDefinition extends AbstractFileGdbFieldDefinition {
  public ShortFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.SHORT,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 6;
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        return row.getShort(this.fieldNumber);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final short shortValue = number.shortValue();
      synchronized (row) {
        row.setShort(this.fieldNumber, shortValue);
      }
    } else {
      final String string = value.toString();
      final short shortValue = Short.parseShort(string);
      synchronized (row) {
        row.setShort(this.fieldNumber, shortValue);
      }
    }
  }
}
