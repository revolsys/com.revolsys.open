package com.revolsys.io;

import java.io.IOException;

public class StringBufferWriter extends java.io.Writer {

  private final StringBuffer buffer;

  public StringBufferWriter(final StringBuffer buffer) {
    this.buffer = buffer;
  }

  @Override
  public void close() throws IOException {
  }

  /**
   * Flush the stream.
   */
  @Override
  public void flush() {
  }

  public StringBuffer getBuffer() {
    return buffer;
  }

  @Override
  public String toString() {
    return buffer.toString();
  }

  @Override
  public void write(final char[] cbuf, final int off, final int len) {
    if ((off < 0) || (off > cbuf.length) || (len < 0)
      || ((off + len) > cbuf.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return;
    }
    buffer.append(cbuf, off, len);
  }

  @Override
  public void write(final int c) {
    buffer.append((char)c);
  }

  @Override
  public void write(final String str) {
    buffer.append(str);
  }

  @Override
  public void write(final String str, final int off, final int len) {
    buffer.append(str.substring(off, off + len));
  }

}
