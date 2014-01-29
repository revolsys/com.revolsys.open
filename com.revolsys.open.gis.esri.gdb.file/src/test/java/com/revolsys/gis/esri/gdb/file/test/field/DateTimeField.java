package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.io.EndianInput;

public class DateTimeField extends FgdbField {
  public DateTimeField(final String name, final boolean required) {
    super(name, DataTypes.DATE_TIME, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final Float value = in.readLEFloat();
    // TODO convert to date
    return (T)value;
  }
}
