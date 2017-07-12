package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.datatype.DataTypes;
import com.revolsys.gis.esri.gdb.file.test.FgdbReader;
import com.revolsys.io.endian.EndianInput;

public class StringField extends FgdbField {
  public StringField(final String name, final int length, final boolean required) {
    super(name, DataTypes.STRING, length, required);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T read(final EndianInput in) throws IOException {
    final int numBytes = (int)FgdbReader.readVarUInt(in);
    final byte[] bytes = new byte[numBytes];
    in.read(bytes);
    return (T)new String(bytes, "UTF-8");
  }
}
