package com.revolsys.gis.esri.gdb.file.type;

import java.util.Date;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class DateAttribute extends AbstractFileGdbAttribute {
  public static final Date MIN_DATE = new Date(70, 0, 1);

  public static final Date MAX_DATE = new Date(138, 1, 19);

  /** Synchronize access to C++ date methods across all instances. */
  private static final Object LOCK = new Object();

  public DateAttribute(final Field field) {
    super(field.getName(), DataTypes.DATE, field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      long time;
      synchronized (LOCK) {
        time = row.getDate(name) * 1000;
      }
      return new Date(time);
    }
  }

  @Override
  public void setValue(final Row row, final Object value) {
    String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        row.setNull(name);
      }
    } else if (value instanceof Date) {
      Date date = (Date)value;
      if (date.before(MIN_DATE)) {
        throw new IllegalArgumentException(name + "=" + date + " is before "
          + MIN_DATE + " which is not supported by ESRI File Geodatabases");
      } else if (date.after(MAX_DATE)) {
        throw new IllegalArgumentException(name + "=" + date + " is after "
          + MAX_DATE + " which is not supported by ESRI File Geodatabases");
      } else {
        long time = date.getTime() / 1000;
        synchronized (LOCK) {
          row.setDate(name, time);
        }
      }
    } else {
      throw new IllegalArgumentException("Expecting a java,util.Date not "
        + value.getClass() + " " + value);
    }
  }

}
