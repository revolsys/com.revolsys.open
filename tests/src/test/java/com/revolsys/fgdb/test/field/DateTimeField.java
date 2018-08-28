package com.revolsys.fgdb.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.datatype.DataTypes;

public class DateTimeField extends FgdbField {
  public DateTimeField(final String name, final boolean required) {
    super(name, DataTypes.DATE_TIME, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final ByteBuffer buffer) throws IOException {
    final Float value = buffer.getFloat();
    // TODO convert to date
    return (T)value;
  }
}
