package com.revolsys.gis.esri.gdb.file.capi.type;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.capi.swig.Row;
import com.revolsys.io.esri.gdb.xml.model.Field;

public class BinaryAttribute extends AbstractFileGdbAttribute {

  public BinaryAttribute(final Field field) {
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
  public Object setValue(final DataObject object, final Row row,
    final Object value) {
    return null;
  }

}
