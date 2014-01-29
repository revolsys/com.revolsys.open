package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.esri.gdb.file.test.FgdbReader;
import com.revolsys.io.EndianInput;

public class BinaryField extends FgdbField {
  public BinaryField(final String name, final int length, final boolean required) {
    super(name, DataTypes.BLOB, length, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final int numBytes = (int)FgdbReader.readVarUInt(in);
    final byte[] bytes = new byte[numBytes];
    in.read(bytes);
    return (T)bytes;
  }
}
