package com.revolsys.gis.esri.gdb.file.capi.type;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbDataObjectStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class IntegerAttribute extends AbstractFileGdbAttribute {
  public IntegerAttribute(final Field field) {
    super(field.getName(), DataTypes.INT,
      BooleanStringConverter.getBoolean(field.getRequired())
        || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 11;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    CapiFileGdbDataObjectStore dataStore = getDataStore();
    if (dataStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (dataStore) {
        return row.getInteger(name);
      }
    }
  }

  @Override
  public Object setValue(final Record object, final Row row,
    final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getDataStore().setNull(row, name);
      }
      return null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final int intValue = number.intValue();
      synchronized (getDataStore()) {
        row.setInteger(name, intValue);
      }
      return intValue;
    } else {
      final String string = value.toString().trim();
      if (StringUtils.hasText(string)) {
        final int intValue = Integer.parseInt(string);
        synchronized (getDataStore()) {
          row.setInteger(name, intValue);
        }
        return intValue;
      } else {
        if (isRequired()) {
          throw new IllegalArgumentException(name
            + " is required and cannot be null");
        } else {
          getDataStore().setNull(row, name);
          return null;
        }
      }
    }
  }

}
