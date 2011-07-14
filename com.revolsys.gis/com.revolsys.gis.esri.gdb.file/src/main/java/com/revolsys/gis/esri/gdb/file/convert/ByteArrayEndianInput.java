package com.revolsys.gis.esri.gdb.file.convert;

import java.io.EOFException;
import java.io.IOException;

import com.revolsys.gis.esri.gdb.file.swig.UnsignedCharArray;
import com.revolsys.io.EndianInput;

public class ByteArrayEndianInput implements EndianInput {
  private int index;

  private UnsignedCharArray array;

  private final byte readBuffer[] = new byte[8];

  public ByteArrayEndianInput(final UnsignedCharArray array) {
    this.array = array;
  }

  public void close() throws IOException {
    array = null;
  }

  public int read() throws IOException {
    final int b = array.get(index);
    index++;
    return b;
  }

  public final int read(final byte b[]) throws IOException {
    return read(b, 0, b.length);
  }

  public int read(final byte b[], final int off, final int len)
    throws IOException {
    if (b == null) {
      throw new NullPointerException();
    } else if ((off < 0) || (off > b.length) || (len < 0)
      || ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    } else if (len == 0) {
      return 0;
    }

    int c = read();
    if (c == -1) {
      return -1;
    }
    b[off] = (byte)c;

    int i = 1;
    try {
      for (; i < len; i++) {
        c = read();
        if (c == -1) {
          break;
        }
        if (b != null) {
          b[off + i] = (byte)c;
        }
      }
    } catch (final IOException ee) {
    }
    return i;
  }

  public final double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  public final float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public final void readFully(final byte b[]) throws IOException {
    readFully(b, 0, b.length);
  }

  public final void readFully(final byte b[], final int off, final int len)
    throws IOException {
    if (len < 0) {
      throw new IndexOutOfBoundsException();
    }
    int n = 0;
    while (n < len) {
      final int count = read(b, off + n, len - n);
      if (count < 0) {
        throw new EOFException();
      }
      n += count;
    }
  }

  public final int readInt() throws IOException {
    final int ch1 = read();
    final int ch2 = read();
    final int ch3 = read();
    final int ch4 = read();
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException();
    }
    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
  }

  public double readLEDouble() throws IOException {
    final long value = readLELong();
    return Double.longBitsToDouble(value);
  }

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

  public long readLELong() throws IOException {
    long value = 0;
    for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
      value |= ((long)(read() & 0xff)) << shiftBy;
    }
    return value;
  }

  public short readLEShort() throws IOException {
    final int b1 = read();
    final int b2 = read();
    if ((b1 | b2) < 0) {
      throw new EOFException();
    }
    final int value = (b2 << 8) + b1;
    return (short)value;
  }

  public final long readLong() throws IOException {
    readFully(readBuffer, 0, 8);
    return (((long)readBuffer[0] << 56) + ((long)(readBuffer[1] & 255) << 48)
      + ((long)(readBuffer[2] & 255) << 40)
      + ((long)(readBuffer[3] & 255) << 32)
      + ((long)(readBuffer[4] & 255) << 24) + ((readBuffer[5] & 255) << 16)
      + ((readBuffer[6] & 255) << 8) + ((readBuffer[7] & 255) << 0));
  }

  public final short readShort() throws IOException {
    final int ch1 = read();
    final int ch2 = read();
    if ((ch1 | ch2) < 0) {
      throw new EOFException();
    }
    return (short)((ch1 << 8) + (ch2 << 0));
  }

  public int skipBytes(final int i) throws IOException {
    this.index += i;
    return i;
  }
}
