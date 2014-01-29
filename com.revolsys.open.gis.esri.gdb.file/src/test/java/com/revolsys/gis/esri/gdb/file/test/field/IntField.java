package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.EndianInput;

public class IntField extends FgdbField {
  public IntField(final String name, final boolean required) {
    super(name, DataTypes.INT, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final Integer value = in.readLEInt();
    return (T)value;
  }
}
