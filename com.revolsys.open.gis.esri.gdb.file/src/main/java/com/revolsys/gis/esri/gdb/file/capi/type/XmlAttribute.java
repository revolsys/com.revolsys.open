package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class XmlAttribute extends AbstractFileGdbAttribute {

  public XmlAttribute(final Field field) {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      field.getRequired() == Boolean.TRUE || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getXML(name);
    }
  }

  @Override
  public Object setValue(
    final DataObject object,
    final Row row,
    final Object value) {
    final String name = getName();
    if (value == null) {
      if (isRequired()) {
        throw new IllegalArgumentException(name
          + " is required and cannot be null");
      } else {
        row.setNull(name);
      }
      return null;
    } else {
      final String string = value.toString();
      row.setXML(name, string);
      return string;
    }
  }

}
