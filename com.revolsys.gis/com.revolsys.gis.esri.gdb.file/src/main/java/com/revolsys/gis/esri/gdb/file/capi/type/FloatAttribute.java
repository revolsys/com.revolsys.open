package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class FloatAttribute extends AbstractFileGdbAttribute {
  public FloatAttribute(final Field field) {
    super(field.getName(), DataTypes.FLOAT, field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getFloat(name);
    }
  }

  @Override
  public void setValue(final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        row.setNull(name);
      }
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      row.setFloat(name, number.floatValue());
    } else {
      final String string = value.toString();
      row.setFloat(name, Float.parseFloat(string));
    }
  }

}
