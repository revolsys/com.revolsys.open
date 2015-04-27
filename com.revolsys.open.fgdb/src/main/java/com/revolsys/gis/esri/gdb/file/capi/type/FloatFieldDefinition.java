package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.format.esri.gdb.xml.model.Field;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;

public class FloatFieldDefinition extends AbstractFileGdbFieldDefinition {
  public FloatFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.FLOAT,
      BooleanStringConverter.getBoolean(field.getRequired())
      || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 19;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final CapiFileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (recordStore) {
        return row.getFloat(name);
      }
    }
  }

  @Override
  public Object setValue(final Record object, final Row row,
    final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getRecordStore().setNull(row, name);
      }
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final float floatValue = number.floatValue();
      synchronized (getRecordStore()) {
        row.setFloat(name, floatValue);
      }
      return floatValue;
    } else {
      final String string = value.toString();
      final float floatValue = Float.parseFloat(string);
      synchronized (getRecordStore()) {
        row.setFloat(name, floatValue);
      }
      return floatValue;
    }
  }

}
