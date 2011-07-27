package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class XmlAttribute extends AbstractEsriFileGeodatabaseAttribute {

  public XmlAttribute(final Field field) {
    super(field.getName(), DataTypes.STRING, field.getLength(),
      field.getRequired() == Boolean.TRUE);
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
  public void setValue(final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      row.SetNull(name);
    } else {
      final String string = value.toString();
      row.SetXML(name, string);
    }
  }

}
