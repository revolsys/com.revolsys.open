package com.revolsys.io;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.util.Property;

public abstract class AbstractRecordWriter extends AbstractWriter<Record> {
  public RecordDefinition getRecordDefinition() {
    return null;
  }

  public boolean isIndent() {
    return BooleanStringConverter.isTrue(getProperty(IoConstants.INDENT));
  }

  public boolean isWritable(final Object value) {
    return Property.hasValue(value) || isWriteNulls();
  }

  public boolean isWriteNulls() {
    return BooleanStringConverter.isTrue(getProperty(IoConstants.WRITE_NULLS));
  }

  public void setIndent(final boolean indent) {
    setProperty(IoConstants.INDENT, Boolean.valueOf(indent));
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (IoConstants.INDENT.equals(name)) {
      setIndent(BooleanStringConverter.isTrue(value));
    }
  }

  public void setWriteNulls(final boolean writeNulls) {
    setProperty(IoConstants.WRITE_NULLS, Boolean.valueOf(writeNulls));
  }
}
