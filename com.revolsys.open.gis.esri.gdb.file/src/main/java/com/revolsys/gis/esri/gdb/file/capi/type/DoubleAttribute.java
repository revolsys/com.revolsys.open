package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class DoubleAttribute extends AbstractFileGdbAttribute {
  public DoubleAttribute(final Field field) {
    super(field.getName(), DataTypes.DOUBLE, field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getDouble(name);
    }
  }

  @Override
  public Object setValue(final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        row.setNull(name);
      }
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      double doubleValue = number.doubleValue();
      row.setDouble(name, doubleValue);
      return doubleValue;
    } else {
      final String string = value.toString();
      double doubleValue = Double.parseDouble(string);
      row.setDouble(name, doubleValue);
      return doubleValue;
    }
  }

}
