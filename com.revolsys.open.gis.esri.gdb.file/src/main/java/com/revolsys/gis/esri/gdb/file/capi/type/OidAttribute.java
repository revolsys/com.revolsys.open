package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class OidAttribute extends AbstractFileGdbAttribute {
  public OidAttribute(final Field field) {
    super(field.getName(), DataTypes.INT, field.getRequired() == Boolean.TRUE
      || !field.isIsNullable());
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      return row.getOid();
    }
  }

  @Override
  public Object setValue(final Row row, final Object value) {
    return null;
  }

  @Override
  public Object setUpdateValue(Row row, Object value) {
    return value;
  }

  public void setPostInsertValue(DataObject object, Row row) {
    int oid = row.getOid();
    String name = getName();
    object.setValue(name, oid);
  }

}
