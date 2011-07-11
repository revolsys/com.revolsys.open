package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public class ShortAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public ShortAttribute(String name, int length, boolean required) {
    super(name, DataTypes.SHORT, length, required);
  }

  @Override
  public Object getValue(Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getShort(name);
    }
}

  public void setValue(Row row, Object object) {
  }

}
