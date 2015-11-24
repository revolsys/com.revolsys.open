package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;
import com.revolsys.util.Property;

public class DoubleFieldDefinition extends AbstractFileGdbFieldDefinition {
  public DoubleFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.DOUBLE,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 19;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (getSync()) {
        return row.getDouble(name);
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
      final double doubleValue = number.doubleValue();
      synchronized (getSync()) {
        row.setDouble(name, doubleValue);
      }
    } else {
      final String string = value.toString();
      if (Property.hasValue(string)) {
        final double doubleValue = Double.parseDouble(string);
        synchronized (getSync()) {
          row.setDouble(name, doubleValue);
        }
      } else if (isRequired()) {
        throw new IllegalArgumentException(name + " is required and cannot be null");
      } else {
        row.setNull(name);
      }
    }
  }

}
