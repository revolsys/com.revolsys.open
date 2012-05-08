package com.revolsys.gis.esri.gdb.file.capi.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class IntegerAttribute extends AbstractFileGdbAttribute {
  public IntegerAttribute(final Field field) {
    super(field.getName(), DataTypes.INT, field.getRequired() == Boolean.TRUE
      || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getInteger(name);
    }
  }

  @Override
  public Object setValue(
    final DataObject object,
    final Row row,
    final Object value) {
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
      final int intValue = number.intValue();
      row.setInteger(name, intValue);
      return intValue;
    } else {
      final String string = value.toString().trim();
      if (StringUtils.hasText(string)) {
        final int intValue = Integer.parseInt(string);
        row.setInteger(name, intValue);
        return intValue;
      } else {
        if (isRequired()) {
          throw new IllegalArgumentException(name
            + " is required and cannot be null");
        } else {
          row.setNull(name);
          return null;
        }
      }
    }
  }

}
