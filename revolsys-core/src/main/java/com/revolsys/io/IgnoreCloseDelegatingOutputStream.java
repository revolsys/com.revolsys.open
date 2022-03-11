package com.revolsys.io;

import java.io.IOException;
import java.io.OutputStream;

public class IgnoreCloseDelegatingOutputStream extends OutputStream {

  private final OutputStream out;

  public IgnoreCloseDelegatingOutputStream(final OutputStream out) {
    this.out = out;
  }

  private void checkOpen() {
    if (this.out == null) {
      throw new IllegalStateException("Closed");
    }
  }

  @Override
  public void close() throws IOException {
    final OutputStream out = this.out;
    if (out != null) {
      out.flush();
    }
  }

  @Override
  public void flush() throws IOException {
    if (this.out != null) {
      this.out.flush();
    }
  }

  @Override
  public void write(final byte[] b) throws IOException {
    checkOpen();
    this.out.write(b);
  }

  @Override
  public void write(final byte[] b, final int off, final int len) throws IOException {
    checkOpen();
    this.out.write(b, off, len);
  }

  @Override
  public void write(final int b) throws IOException {
    checkOpen();
    this.out.write(b);
  }
}
