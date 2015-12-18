package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.gis.esri.gdb.file.FileGdbRecordStore;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class LengthFieldDefinition extends AbstractFileGdbFieldDefinition {
  public LengthFieldDefinition(final Field field) {
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
    final FileGdbRecordStore recordStore = getRecordStore();
    if (recordStore.isNull(row, name)) {
      return null;
    } else {
      synchronized (getSync()) {
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
    double length = 0;
    final Geometry geometry = record.getGeometry();
    if (geometry != null) {
      length = geometry.getLength();
    }
    final String name = getName();
    row.setDouble(name, length);
  }
}
