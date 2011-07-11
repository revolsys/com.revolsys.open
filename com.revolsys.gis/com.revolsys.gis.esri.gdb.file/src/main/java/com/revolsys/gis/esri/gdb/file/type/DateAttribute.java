package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public class DateAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public DateAttribute(String name, int length, boolean required) {
    super(name, DataTypes.DATE, length, required);
  }

  @Override
  public Object getValue(Row row) {
    // TODO Auto-generated method stub
    return null;
  }

  public void setValue(Row row, Object object) {
  }

}
