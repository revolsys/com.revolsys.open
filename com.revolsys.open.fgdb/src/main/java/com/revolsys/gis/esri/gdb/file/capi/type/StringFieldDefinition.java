package com.revolsys.gis.esri.gdb.file.capi.type;

import org.slf4j.LoggerFactory;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;

public class StringFieldDefinition extends AbstractFileGdbFieldDefinition {
  public StringFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      BooleanStringConverter.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    final FileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (getSync()) {
        return row.getString(name);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      setNull(row);
    } else {
      String string = value.toString();
      if (string.length() > getLength()) {
        LoggerFactory.getLogger(getClass()).warn("Value is to long for: " + this + ":" + string);
        string = string.substring(0, getLength());
      }
      row.setString(name, string);
    }
  }
}
