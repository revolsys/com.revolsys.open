package com.revolsys.gis.esri.gdb.file.capi.type;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class DoubleAttribute extends AbstractFileGdbAttribute {
  public DoubleAttribute(final Field field) {
    super(field.getName(), DataTypes.DOUBLE,
      field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (getDataStore().isNull(row, name)) {
      return null;
    } else {
      synchronized (getDataStore()) {
        return row.getDouble(name);
      }
    }
  }

  @Override
  public int getMaxStringLength() {
    return 19;
  }

  @Override
  public Object setValue(final DataObject object, final Row row,
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
      final double doubleValue = number.doubleValue();
      synchronized (getDataStore()) {
        row.setDouble(name, doubleValue);
      }
      return doubleValue;
    } else {
      final String string = value.toString();
      if (StringUtils.hasText(string)) {
        final double doubleValue = Double.parseDouble(string);
        synchronized (getDataStore()) {
          row.setDouble(name, doubleValue);
        }
        return doubleValue;
      } else if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        getDataStore().setNull(row, name);
        return null;
      }
    }
  }

}
