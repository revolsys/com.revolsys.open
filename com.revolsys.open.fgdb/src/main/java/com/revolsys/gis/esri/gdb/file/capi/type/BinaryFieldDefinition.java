package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.record.Record;
import com.revolsys.record.io.format.esri.gdb.xml.model.Field;
import com.revolsys.util.Booleans;

public class BinaryFieldDefinition extends AbstractFileGdbFieldDefinition {

  public BinaryFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.BASE64_BINARY, field.getLength(),
      Booleans.getBoolean(field.getRequired()) || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 40;
  }

  @Override
  public Object getValue(final Row row) {
    return null;
  }

  @Override
  public void setValue(final Record record, final Row row, final Object value) {
  }
}
