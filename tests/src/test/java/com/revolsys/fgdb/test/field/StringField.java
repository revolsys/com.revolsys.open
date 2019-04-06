package com.revolsys.fgdb.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jeometry.common.datatype.DataTypes;

public class StringField extends FgdbField {
  public StringField(final String name, final int length, final boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final ByteBuffer buffer) throws IOException {
    // final int numBytes = (int)FgdbReader.readVarUInt(buffer);
    // final byte[] bytes = new byte[numBytes];
    // buffer.read(bytes);
    // return (T)new String(bytes, "UTF-8");
    return null;
  }
}
