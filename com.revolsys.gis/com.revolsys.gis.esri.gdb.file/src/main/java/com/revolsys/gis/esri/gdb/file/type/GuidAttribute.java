package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Guid;
import com.revolsys.gis.esri.gdb.file.swig.Row;

public class GuidAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public GuidAttribute(String name, int length, boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      Guid guid = row.getGuid(name);
      return guid.toString();
    }
  }

  public void setValue(Row row, Object object) {
    final String name = getName();
    if (object == null) {
      row.SetNull(name);
    } else {
      Guid guid = new Guid();
      guid.FromString(object.toString());
      row.SetGUID(name, guid);
    }
  }

}
