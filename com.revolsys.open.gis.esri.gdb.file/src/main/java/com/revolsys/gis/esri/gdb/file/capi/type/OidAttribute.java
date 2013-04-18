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
    if (getDataStore().isNull(row, name)) {
      return null;
    } else {
      synchronized (getDataStore()) {
        return row.getOid();
      }
    }
  }

  @Override
  public int getMaxStringLength() {
    return 10;
  }

  @Override
  public void setPostInsertValue(final DataObject object, final Row row) {
    synchronized (getDataStore()) {
      final int oid = row.getOid();
      final String name = getName();
      object.setValue(name, oid);
    }
  }

  @Override
  public Object setUpdateValue(final DataObject object, final Row row,
    final Object value) {
    return value;
  }

  @Override
  public Object setValue(final DataObject object, final Row row,
    final Object value) {
    return null;
  }

}
