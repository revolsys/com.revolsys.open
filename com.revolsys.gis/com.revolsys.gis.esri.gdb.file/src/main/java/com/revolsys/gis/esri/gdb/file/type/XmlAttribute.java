package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public class XmlAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public XmlAttribute(String name, int length, boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getXML(name);
    }
  }

  public void setValue(Row row, Object object) {
  }

}
