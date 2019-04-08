package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;
import com.revolsys.util.Property;

public class IntegerFieldDefinition extends AbstractFileGdbFieldDefinition {
  public IntegerFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.INT,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 11;
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        return row.getInteger(this.fieldNumber);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final int intValue = number.intValue();
      synchronized (row) {
        row.setInteger(this.fieldNumber, intValue);
      }
    } else {
      final String string = value.toString().trim();
      if (Property.hasValue(string)) {
        final int intValue = Integer.parseInt(string);
        synchronized (row) {
          row.setInteger(this.fieldNumber, intValue);
        }
      } else {
        if (isRequired()) {
          throw new IllegalArgumentException(getName() + " is required and cannot be null");
        } else {
          row.setNull(this.fieldNumber);
        }
      }
    }
  }

}
