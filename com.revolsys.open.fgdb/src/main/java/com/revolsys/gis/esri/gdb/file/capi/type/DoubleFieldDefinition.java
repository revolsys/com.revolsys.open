package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;
import com.revolsys.util.Property;

public class DoubleFieldDefinition extends AbstractFileGdbFieldDefinition {
  public DoubleFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.DOUBLE,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 19;
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        return row.getDouble(this.fieldNumber);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final double doubleValue = number.doubleValue();
      synchronized (row) {
        row.setDouble(this.fieldNumber, doubleValue);
      }
    } else {
      final String string = value.toString();
      if (Property.hasValue(string)) {
        final double doubleValue = Double.parseDouble(string);
        synchronized (row) {
          row.setDouble(this.fieldNumber, doubleValue);
        }
      } else if (isRequired()) {
        throw new IllegalArgumentException(getName() + " is required and cannot be null");
      } else {
        setNull(row);
      }
    }
  }

}
