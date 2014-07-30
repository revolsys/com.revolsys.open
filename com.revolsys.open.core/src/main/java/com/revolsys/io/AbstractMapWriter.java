package com.revolsys.io;

import java.util.Map;

import com.revolsys.converter.string.BooleanStringConverter;
import com.revolsys.util.Property;

public abstract class AbstractMapWriter extends
AbstractWriter<Map<String, ? extends Object>> implements MapWriter {

  @Override
  public void close() {
  }

  @Override
  public void flush() {
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
    final boolean oldValue = getProperty(IoConstants.INDENT, false);
    if (indent != oldValue) {
      setProperty(IoConstants.INDENT, Boolean.valueOf(indent));
    }
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (IoConstants.INDENT.equals(name)) {
      setIndent(BooleanStringConverter.isTrue(value));
    }
  }

  public void setWriteNulls(final boolean writeNulls) {
    final boolean oldValue = getProperty(IoConstants.INDENT, false);
    if (oldValue != writeNulls) {
      setProperty(IoConstants.WRITE_NULLS, Boolean.valueOf(writeNulls));
    }
  }
}
