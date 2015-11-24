package com.revolsys.io;

import java.util.Map;

import com.revolsys.io.map.MapWriter;
import com.revolsys.util.Booleans;
import com.revolsys.util.Property;

public abstract class AbstractMapWriter extends AbstractWriter<Map<String, ? extends Object>>
  implements MapWriter {

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }

  public boolean isIndent() {
    return Booleans.isTrue(getProperty(IoConstants.INDENT));
  }

  public boolean isWritable(final Object value) {
    return Property.hasValue(value) || isWriteNulls();
  }

  public boolean isWriteNulls() {
    return Booleans.isTrue(getProperty(IoConstants.WRITE_NULLS));
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
      setIndent(Booleans.isTrue(value));
    }
  }

  public void setWriteNulls(final boolean writeNulls) {
    final boolean oldValue = getProperty(IoConstants.INDENT, false);
    if (oldValue != writeNulls) {
      setProperty(IoConstants.WRITE_NULLS, Boolean.valueOf(writeNulls));
    }
  }
}
