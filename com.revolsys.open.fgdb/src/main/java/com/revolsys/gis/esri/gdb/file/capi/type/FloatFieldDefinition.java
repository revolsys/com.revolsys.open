package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class FloatFieldDefinition extends AbstractFileGdbFieldDefinition {
  public FloatFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.FLOAT,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 19;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    synchronized (getSync()) {
      if (row.isNull(name)) {
        return null;
      } else {
        return row.getFloat(name);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      setNull(row);
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final float floatValue = number.floatValue();
      synchronized (getSync()) {
        row.setFloat(name, floatValue);
      }
    } else {
      final String string = value.toString();
      final float floatValue = Float.parseFloat(string);
      synchronized (getSync()) {
        row.setFloat(name, floatValue);
      }
    }
  }

}
