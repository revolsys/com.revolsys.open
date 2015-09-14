package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.EndianInput;

public class DoubleField extends FgdbField {
  public DoubleField(final String name, final boolean required) {
    super(name, DataTypes.DOUBLE, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final Double value = in.readLEDouble();
    return (T)value;
  }
}
