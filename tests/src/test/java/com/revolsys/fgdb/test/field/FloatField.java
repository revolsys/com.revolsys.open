package com.revolsys.fgdb.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jeometry.common.data.type.DataTypes;

public class FloatField extends FgdbField {
  public FloatField(final String name, final boolean required) {
    super(name, DataTypes.FLOAT, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final ByteBuffer buffer) throws IOException {
    final Float value = buffer.getFloat();
    return (T)value;
  }
}
