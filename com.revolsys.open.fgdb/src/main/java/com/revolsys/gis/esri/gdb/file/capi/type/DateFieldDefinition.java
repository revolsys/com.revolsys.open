package com.revolsys.gis.esri.gdb.file.capi.type;

import java.util.Date;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;
import org.jeometry.common.logging.Logs;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class DateFieldDefinition extends AbstractFileGdbFieldDefinition {
  /** Synchronize access to C++ date methods across all instances. */
  private static final Object LOCK = new Object();

  @SuppressWarnings("deprecation")
  public static final Date MAX_DATE = new Date(138, 1, 19);

  @SuppressWarnings("deprecation")
  public static final Date MIN_DATE = new Date(70, 0, 1);

  public DateFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.DATE,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        long time;
        synchronized (LOCK) {
          time = row.getDate(this.fieldNumber) * 1000;
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
          Logs.error(this, getName() + "=" + date + " is before " + MIN_DATE
            + " which is not supported by ESRI File Geodatabases\n" + record);
          if (isRequired()) {
            date = MIN_DATE;
          } else {
            row.setNull(this.fieldNumber);
          }
        } else if (date.after(MAX_DATE)) {
          Logs.error(this, getName() + "=" + date + " is after " + MAX_DATE
            + " which is not supported by ESRI File Geodatabases\n" + record);
          if (isRequired()) {
            date = MAX_DATE;
          } else {
            row.setNull(this.fieldNumber);
          }
        }
        final long time = date.getTime() / 1000;
        synchronized (LOCK) {
          synchronized (row) {
            row.setDate(this.fieldNumber, time);
          }
        }
      } else {
        throw new IllegalArgumentException(
          "Expecting a java.util.Date not " + value.getClass() + " " + value);
      }
    }
  }
}
