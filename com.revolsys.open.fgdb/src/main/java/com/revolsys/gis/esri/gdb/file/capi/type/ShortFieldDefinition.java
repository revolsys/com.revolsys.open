package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class ShortFieldDefinition extends AbstractFileGdbFieldDefinition {
  public ShortFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.SHORT,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 6;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    synchronized (getSync()) {
      if (row.isNull(name)) {
        return null;
      } else {
        return row.getShort(name);
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
      final short shortValue = number.shortValue();
      synchronized (getSync()) {
        row.setShort(name, shortValue);
      }
    } else {
      final String string = value.toString();
      final short shortValue = Short.parseShort(string);
      synchronized (getSync()) {
        row.setShort(name, shortValue);
      }
    }
  }
}
