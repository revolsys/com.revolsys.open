package com.revolsys.gis.esri.gdb.file.capi.type;

import java.util.Date;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordLog;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;
import com.revolsys.util.DateUtil;

public class DateAttribute extends AbstractFileGdbAttribute {
  @SuppressWarnings("deprecation")
  public static final Date MIN_DATE = new Date(70, 0, 1);

  @SuppressWarnings("deprecation")
  public static final Date MAX_DATE = new Date(138, 1, 19);

  /** Synchronize access to C++ date methods across all instances. */
  private static final Object LOCK = new Object();

  public DateAttribute(final Field field) {
    super(field.getName(), DataTypes.DATE,
      BooleanStringConverter.getBoolean(field.getRequired())
        || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    CapiFileGdbDataObjectStore dataStore = getDataStore();
    if (dataStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (dataStore) {
        long time;
        synchronized (LOCK) {
          time = row.getDate(name) * 1000;
        }
        return new Date(time);
      }
    }
  }

  @Override
  public Object setValue(final Record object, final Row row, Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getDataStore().setNull(row, name);
      }
      return null;
    } else {
      if (value instanceof String) {
        try {
          value = DateUtil.getDate("yyyy-MM-dd", (String)value);
        } catch (final Exception e) {
          throw new IllegalArgumentException(
            "Data must be in the format YYYY-MM-DD " + value);
        }
      }
      if (value instanceof Date) {
        Date date = (Date)value;
        if (date.before(MIN_DATE)) {
          RecordLog.warn(getClass(), name + "=" + date + " is before "
            + MIN_DATE + " which is not supported by ESRI File Geodatabases",
            object);
          if (isRequired()) {
            date = MIN_DATE;
          } else {
            getDataStore().setNull(row, name);
            return null;
          }
        } else if (date.after(MAX_DATE)) {
          RecordLog.warn(getClass(), name + "=" + date + " is after "
            + MAX_DATE + " which is not supported by ESRI File Geodatabases",
            object);
          if (isRequired()) {
            date = MAX_DATE;
          } else {
            getDataStore().setNull(row, name);
            return null;
          }
        }
        synchronized (getDataStore()) {
          final long time = date.getTime() / 1000;
          synchronized (LOCK) {
            row.setDate(name, time);
          }
          return time;
        }
      } else {
        throw new IllegalArgumentException("Expecting a java,util.Date not "
          + value.getClass() + " " + value);
      }
    }
  }
}
