package com.revolsys.fgdb.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.revolsys.datatype.DataTypes;

public class IntField extends FgdbField {
  public IntField(final String name, final boolean required) {
    super(name, DataTypes.INT, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final ByteBuffer buffer) throws IOException {
    final Integer value = buffer.getInt();
    return (T)value;
  }
}
