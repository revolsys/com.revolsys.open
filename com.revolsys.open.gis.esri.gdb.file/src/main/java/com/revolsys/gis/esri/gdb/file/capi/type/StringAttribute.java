package com.revolsys.gis.esri.gdb.file.capi.type;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.CapiFileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class StringAttribute extends AbstractFileGdbAttribute {
  public StringAttribute(final Field field) {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      BooleanStringConverter.getBoolean(field.getRequired())
        || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    CapiFileGdbRecordStore recordStore = getDataStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (recordStore) {
        return row.getString(name);
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
    } else {
      String string = value.toString();
      if (string.length() > getLength()) {
        LoggerFactory.getLogger(getClass()).error(
          "Value is to long for: " + this + ":" + string);
        string = string.substring(0, getLength());
      }
      synchronized (getDataStore()) {
        row.setString(name, string);
      }
      return string;
    }
  }
}
