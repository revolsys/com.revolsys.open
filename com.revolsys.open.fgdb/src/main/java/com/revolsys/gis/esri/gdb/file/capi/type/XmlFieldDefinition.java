package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.esri.filegdb.jni.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class XmlFieldDefinition extends AbstractFileGdbFieldDefinition {

  public XmlFieldDefinition(final Field field) {
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
        return row.getXML(name);
      }
    }
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      setNull(row);
    } else {
      final String string = value.toString();
      synchronized (getSync()) {
        row.setXML(name, string);
      }
    }
  }

}
