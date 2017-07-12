package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.endian.EndianInput;
import com.revolsys.record.Record;

public class ObjectIdField extends FgdbField {
  public ObjectIdField(final String name, final boolean required) {
    super(name, DataTypes.INT, required);
  }

  @Override
  public <T> T read(final EndianInput in) throws IOException {
    return null;
  }

  @Override
  public boolean setValue(final Record record, final EndianInput in) throws IOException {
    return false;
  }

  @Override
  public Object validate(final Object value) {
    return value;
  }
}
