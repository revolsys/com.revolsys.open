package com.revolsys.gis.esri.gdb.file.type;

import java.util.Date;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class DateAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public DateAttribute(final Field field) {
    super(field.getName(), DataTypes.DATE, field.isRequired());
  }

  @Override
  public Object getValue(final Row row) {
    String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      int time = row.getDate(name) * 1000;
      return new Date(time);
    }
  }

  @Override
  public void setValue(final Row row, final Object value) {
    String name = getName();
    if (value == null) {
      row.SetNull(name);
    } else if (value instanceof Date) {
      Date date = (Date)value;
      int time = (int)(date.getTime() / 1000);
      row.setDate(name, time);
    } else {
      throw new IllegalArgumentException("Expecting a java,util.Date not "
        + value.getClass() + " " + value);
    }
  }

}
