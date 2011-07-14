package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class IntegerAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public IntegerAttribute(final Field field) {
    super(field.getName(), DataTypes.INT, field.isRequired());
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
  public void setValue(final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      row.SetNull(name);
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      row.SetInteger(name, number.intValue());
    } else {
      final String string = value.toString();
      row.SetInteger(name, Integer.parseInt(string));
    }
  }

}
