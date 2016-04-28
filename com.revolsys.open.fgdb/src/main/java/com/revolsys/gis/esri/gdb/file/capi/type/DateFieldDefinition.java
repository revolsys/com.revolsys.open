package com.revolsys.gis.esri.gdb.file.capi.type;

import java.util.Date;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;
import com.revolsys.util.Dates;

public class DateFieldDefinition extends AbstractFileGdbFieldDefinition {
  /** Synchronize access to C++ date methods across all instances. */
  private static final Object LOCK = new Object();

  @SuppressWarnings("deprecation")
  public static final Date MAX_DATE = new Date(138, 1, 19);

  @SuppressWarnings("deprecation")
  public static final Date MIN_DATE = new Date(70, 0, 1);

  public DateFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.DATE,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (getSync()) {
        long time;
        synchronized (LOCK) {
          time = row.getDate(name) * 1000;
        }
        return new Date(time);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, Object value) {
    if (value == null) {
      setNull(row);
    } else {
      final String name = getName();
      if (value instanceof String) {
        try {
          value = Dates.getDate("yyyy-MM-dd", (String)value);
        } catch (final Exception e) {
          throw new IllegalArgumentException("Data must be in the format YYYY-MM-DD " + value);
        }
      }
      if (value instanceof Date) {
        Date date = (Date)value;
        if (date.before(MIN_DATE)) {
          Logs.error(getClass(), name + "=" + date + " is before " + MIN_DATE
            + " which is not supported by ESRI File Geodatabases\n" + record);
          if (isRequired()) {
            date = MIN_DATE;
          } else {
            row.setNull(name);
          }
        } else if (date.after(MAX_DATE)) {
          Logs.error(getClass(), name + "=" + date + " is after " + MAX_DATE
            + " which is not supported by ESRI File Geodatabases\n" + record);
          if (isRequired()) {
            date = MAX_DATE;
          } else {
            row.setNull(name);
          }
        }
        synchronized (getSync()) {
          final long time = date.getTime() / 1000;
          synchronized (LOCK) {
            row.setDate(name, time);
          }
        }
      } else {
        throw new IllegalArgumentException(
          "Expecting a java,util.Date not " + value.getClass() + " " + value);
      }
    }
  }
}
