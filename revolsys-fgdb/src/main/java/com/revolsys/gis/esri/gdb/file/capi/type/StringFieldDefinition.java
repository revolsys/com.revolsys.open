package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.logging.Logs;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class StringFieldDefinition extends AbstractFileGdbFieldDefinition {
  public StringFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.STRING, field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        return row.getString(this.fieldNumber);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else {
      String string = value.toString();
      final int length = getLength();
      if (length > 0 && string.length() > length) {
        Logs.warn(this, "Value is to long for: " + this + ":" + string);
        string = string.substring(0, length);
      }
      row.setString(this.fieldNumber, string);
    }
  }
}
