package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class DateAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public DateAttribute(final Field field) {
    super(field.getName(), DataTypes.DATE, field.isRequired());
  }

  @Override
  public Object getValue(final Row row) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setValue(final Row row, final Object value) {
  }

}
