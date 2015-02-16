package com.revolsys.gis.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.revolsys.io.EndianInput;

public class EndianInputStream extends DataInputStream implements EndianInput {
  public EndianInputStream(final InputStream in) {
    super(in);
  }

  @Override
  public double readLEDouble() throws IOException {
    final long value = readLELong();
    return Double.longBitsToDouble(value);
  }

  @Override
  public float readLEFloat() throws IOException {
    final int value = readLEInt();
    return Float.intBitsToFloat(value);
  }

  @Override
  public int readLEInt() throws IOException {
    final int b1 = read();
    final int b2 = read();
    final int b3 = read();
    final int b4 = read();
    if ((b1 | b2 | b3 | b4) < 0) {
      throw new EOFException();
    }
    final int value = (b4 << 24) + (b3 << 16) + (b2 << 8) + b1;

    return value;
  }

  @Override
  public long readLELong() throws IOException {
    long value = 0;
    for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
      value |= (long)(read() & 0xff) << shiftBy;
    }
    return value;
  }

  @Override
  public short readLEShort() throws IOException {
    final int b1 = read();
    final int b2 = read();
    if ((b1 | b2) < 0) {
      throw new EOFException();
    }
    final int value = (b2 << 8) + b1;
    return (short)value;
  }
}
