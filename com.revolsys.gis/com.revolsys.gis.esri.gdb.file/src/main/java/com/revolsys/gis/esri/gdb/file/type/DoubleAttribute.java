package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public class DoubleAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public DoubleAttribute(String name, int length, boolean required) {
    super(name, DataTypes.DOUBLE, length, required);
  }

  @Override
  public Object getValue(Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getDouble(name);
    }
  }

  public void setValue(Row row, Object object) {
    final String name = getName();
    row.SetDouble(name, ((Number)object).doubleValue());
  }

}
