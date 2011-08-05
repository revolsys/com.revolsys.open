package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class ShortAttribute extends AbstractFileGdbAttribute {
  public ShortAttribute(final Field field) {
    super(field.getName(), DataTypes.SHORT, field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getShort(name);
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
      row.setShort(name, number.shortValue());
    } else {
      final String string = value.toString();
      row.setShort(name, Short.parseShort(string));
    }
  }

}
