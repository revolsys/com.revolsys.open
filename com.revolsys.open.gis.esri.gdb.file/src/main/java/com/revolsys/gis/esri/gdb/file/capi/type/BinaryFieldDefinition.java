package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class BinaryFieldDefinition extends AbstractFileGdbFieldDefinition {

  public BinaryFieldDefinition(final Field field) {
    super(field.getName(), DataTypes.BASE64_BINARY, field.getLength(),
      BooleanStringConverter.getBoolean(field.getRequired())
      || !field.isIsNullable());
  }

  @Override
  public int getMaxStringLength() {
    return 40;
  }

  @Override
  public Object getValue(final Row row) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Object setValue(final Record object, final Row row,
    final Object value) {
    return null;
  }

}
