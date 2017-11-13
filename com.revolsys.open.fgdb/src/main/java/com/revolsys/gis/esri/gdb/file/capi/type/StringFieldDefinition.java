package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.logging.Logs;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class StringFieldDefinition extends AbstractFileGdbFieldDefinition {
  public StringFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    synchronized (getSync()) {
      if (row.isNull(name)) {
        return null;
      } else {
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
        Logs.warn(this, "Value is to long for: " + this + ":" + string);
        string = string.substring(0, getLength());
      }
      row.setString(name, string);
    }
  }
}
