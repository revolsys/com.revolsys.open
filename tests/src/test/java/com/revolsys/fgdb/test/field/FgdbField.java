package com.revolsys.fgdb.test.field;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jeometry.common.datatype.DataType;

import com.revolsys.record.Record;
import com.revolsys.record.schema.FieldDefinition;

public class FgdbField extends FieldDefinition {

  public FgdbField(final String name, final DataType type, final boolean required) {
    super(name, type, required);
  }

  public FgdbField(final String name, final DataType type, final int length,
    final boolean required) {
    super(name, type, length, required);
  }

  public <T> T read(final ByteBuffer buffer) throws IOException {
    throw new UnsupportedOperationException();
  }

  public boolean setValue(final Record record, final ByteBuffer buffer) throws IOException {
    final Object value = read(buffer);
    final String name = getName();
    record.setValue(name, value);
    return true;
  }
}
