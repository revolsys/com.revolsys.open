package com.revolsys.gis.esri.gdb.file.type;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.swig.Guid;
import com.revolsys.gis.esri.gdb.file.swig.Row;
import com.revolsys.gis.esri.gdb.xml.model.Field;

public class GuidAttribute extends AbstractEsriFileGeodatabaseAttribute {
  public GuidAttribute(final Field field) {
    this(field.getName(), field.getLength(),field.getRequired() == Boolean.TRUE);
  }

  public GuidAttribute(final String name, final int length,
    final boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    if (row.isNull(name)) {
      return null;
    } else {
      final Guid guid = row.getGuid(name);
      return guid.toString();
    }
  }

  @Override
  public void setValue(final Row row, final Object value) {
    final String name = getName();
    if (value == null) {
      row.SetNull(name);
    } else {
      final Guid guid = new Guid();
      final String string = value.toString();
      guid.FromString(string);
      row.SetGUID(name, guid);
    }
  }

}
