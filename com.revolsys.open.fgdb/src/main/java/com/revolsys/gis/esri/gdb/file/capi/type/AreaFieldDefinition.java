package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class AreaFieldDefinition extends AbstractFileGdbFieldDefinition {
  public AreaFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.DOUBLE,
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 19;
  }

  @Override
  public Object getValue(final Row row) {
    final String name = getName();
    synchronized (getSync()) {
      if (row.isNull(name)) {
        return null;
      } else {
        return row.getDouble(name);
      }
    }
  }

  @Override
  public boolean isAutoCalculated() {
    return true;
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
    double area = 0;
    final Geometry geometry = record.getGeometry();
    if (geometry != null) {
      area = geometry.getArea();
    }
    final String name = getName();
    row.setDouble(name, area);
  }
}
