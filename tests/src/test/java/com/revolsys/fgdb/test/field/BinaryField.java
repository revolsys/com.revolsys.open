package com.revolsys.fgdb.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jeometry.common.datatype.DataTypes;

public class BinaryField extends FgdbField {
  public BinaryField(final String name, final int length, final boolean required) {
    super(name, DataTypes.BLOB, length, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final ByteBuffer buffer) throws IOException {
    // final int numBytes = (int)FgdbReader.readVarUInt(buffer);
    // final byte[] bytes = new byte[numBytes];
    // buffer.read(bytes);
    // return (T)bytes;
    return null;
  }
}
