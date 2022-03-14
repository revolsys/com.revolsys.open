package com.revolsys.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IgnoreCloseDelegatingInputStream extends InputStream {

  private final InputStream in;

  public IgnoreCloseDelegatingInputStream(final InputStream in) {
    this.in = in;
  }

  @Override
  public int available() throws IOException {
    checkOpen();
    return this.in.available();
  }

  @Override
  public synchronized void mark(final int readlimit) {
    checkOpen();
    this.in.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    checkOpen();
    return this.in.markSupported();
  }

  @Override
  public int read() throws IOException {
    checkOpen();
    return this.in.read();
  }

  @Override
  public int read(final byte[] b) throws IOException {
    checkOpen();
    return this.in.read(b);
  }

  @Override
  public int read(final byte[] b, final int off, final int len) throws IOException {
    checkOpen();
    return this.in.read(b, off, len);
  }

  @Override
  public byte[] readAllBytes() throws IOException {
    checkOpen();
    return this.in.readAllBytes();
  }

  @Override
  public int readNBytes(final byte[] b, final int off, final int len) throws IOException {
    checkOpen();
    return this.in.readNBytes(b, off, len);
  }

  @Override
  public byte[] readNBytes(final int len) throws IOException {
    checkOpen();
    return this.in.readNBytes(len);
  }

  @Override
  public synchronized void reset() throws IOException {
    checkOpen();
    this.in.reset();
  }

  @Override
  public long skip(final long n) throws IOException {
    checkOpen();
    return this.in.skip(n);
  }

  @Override
  public long transferTo(final OutputStream out) throws IOException {
    checkOpen();
    return this.in.transferTo(out);
  }

  private void checkOpen() {
    if (this.in == null) {
      throw new IllegalStateException("Closed");
    }
  }

}
