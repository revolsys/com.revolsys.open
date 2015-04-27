package com.revolsys.gis.postgresql.type;

import java.io.IOException;
import java.io.InputStream;

public class StringByteInputStream extends InputStream {

  private final String buffer;

  private int index;

  public StringByteInputStream(final String buffer) {
    this.buffer = buffer;
  }

  private byte getHexPart() {
    final char c = this.buffer.charAt(this.index++);
    if (c >= '0' && c <= '9') {
      return (byte)(c - '0');
    } else if (c >= 'A' && c <= 'F') {
      return (byte)(c - 'A' + 10);
    } else if (c >= 'a' && c <= 'f') {
      return (byte)(c - 'a' + 10);
    } else {
      throw new IllegalArgumentException("No valid Hex char " + c);
    }
  }

  @Override
  public int read() throws IOException {
    if (this.index < this.buffer.length() - 1) {
      final int high = getHexPart();
      final int low = getHexPart();
      return (high << 4) + low;
    } else {
      return -1;
    }
  }

}
