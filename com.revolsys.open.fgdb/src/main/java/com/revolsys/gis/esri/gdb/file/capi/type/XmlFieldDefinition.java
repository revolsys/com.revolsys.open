package com.revolsys.gis.esri.gdb.file.capi.type;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class XmlFieldDefinition extends AbstractFileGdbFieldDefinition {

  public XmlFieldDefinition(final int fieldNumber, final Field field) {
    super(fieldNumber, field.getName(), DataTypes.STRING, field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    synchronized (row) {
      if (row.isNull(this.fieldNumber)) {
        return null;
      } else {
        return row.getXML(this.fieldNumber);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    if (value == null) {
      setNull(row);
    } else {
      final String string = value.toString();
      synchronized (row) {
        row.setXML(this.fieldNumber, string);
      }
    }
  }

}
