package com.revolsys.util;

import java.nio.charset.Charset;

public class Base64EncodingWriter extends java.io.PrintWriter {
  private byte[] buffer;

  private final int bufferLength;

  final Charset charset = Charset.forName("UTF-8");

  private int position;

  public Base64EncodingWriter(
    final java.io.Writer out) {
    super(out);
    this.bufferLength = 3;
    this.buffer = new byte[bufferLength];
    this.position = 0;
  }

  @Override
  public void close() {
    flush();
    super.close();
    buffer = null;
  }

  @Override
  public void flush() {
    if (position > 0) {
      writeBuffer();
      position = 0;
    }

  }

  public void print(
    final byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      final byte b = bytes[i];
      write(b);
    }
  }

  public void write(
    final byte b) {
    buffer[position++] = b;
    if (position >= bufferLength) {
      writeBuffer();
      position = 0;
    }
  }

  @Override
  public void write(
    final char[] characters,
    final int off,
    final int len) {
    final byte[] bytes = String.valueOf(characters).getBytes(charset);
    for (int i = 0; i < len; i++) {
      write(bytes[off + i]);
    }
  }

  @Override
  public void write(
    final int character) {
    final byte[] bytes = String.valueOf(character).getBytes(charset);
    print(bytes);
  }

  private void writeBuffer() {
    final int inBuff = ((buffer[0] << 24) >>> 8) | ((buffer[1] << 24) >>> 16)
      | ((buffer[2] << 24) >>> 24);
    write(Base64Constants.URL_SAFE_ALPHABET[(inBuff >>> 18)]);
    write(Base64Constants.URL_SAFE_ALPHABET[(inBuff >>> 12) & 0x3f]);
    write(Base64Constants.URL_SAFE_ALPHABET[(inBuff >>> 6) & 0x3f]);
    write(Base64Constants.URL_SAFE_ALPHABET[(inBuff) & 0x3f]);
  }

}
