package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.EndianInput;

public class ShortField extends FgdbField {
  public ShortField(final String name, final boolean required) {
    super(name, DataTypes.SHORT, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final Short value = in.readLEShort();
    return (T)value;
  }
}
