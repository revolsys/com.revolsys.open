package com.revolsys.gis.esri.gdb.file.test.field;

import java.io.IOException;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.FieldDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.EndianInput;

public class FgdbField extends FieldDefinition {

  public FgdbField(final String name, final DataType type,
    final boolean required) {
    super(name, type, required);
  }

  public FgdbField(final String name, final DataType type, final int length,
    final boolean required) {
    super(name, type, length, required);
  }

  public <T> T read(final EndianInput in) throws IOException {
    throw new UnsupportedOperationException();
  }

  public boolean setValue(final Record record, final EndianInput in)
      throws IOException {
    final Object value = read(in);
    final String name = getName();
    record.setValue(name, value);
    return true;
  }
}
