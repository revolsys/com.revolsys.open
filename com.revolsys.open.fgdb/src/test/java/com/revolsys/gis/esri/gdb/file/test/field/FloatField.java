package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.endian.EndianInput;

public class FloatField extends FgdbField {
  public FloatField(final String name, final boolean required) {
    super(name, DataTypes.FLOAT, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final Float value = in.readLEFloat();
    return (T)value;
  }
}
