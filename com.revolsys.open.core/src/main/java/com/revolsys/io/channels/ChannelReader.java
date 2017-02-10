/*
 * Copyright 2007-2013, martin isenburg, rapidlasso - fast tools to catch reality
 *
 * This is free software; you can redistribute and/or modify it under the
 * terms of the GNU Lesser General Licence as published by the Free Software
 * Foundation. See the LICENSE.txt file for more information.
 *
 * This software is distributed WITHOUT ANY WARRANTY and without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.revolsys.io.channels;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.revolsys.io.BaseCloseable;
import com.revolsys.util.Exceptions;

public class ChannelReader implements BaseCloseable {

  private ByteBuffer buffer;

  private ReadableByteChannel in;

  private int available = 0;

  private ByteBuffer tempBuffer = ByteBuffer.allocate(8);

  public ChannelReader(final ReadableByteChannel in, final ByteBuffer buffer) {
    this.in = in;
    this.buffer = buffer;
    this.tempBuffer.order(buffer.order());
  }

  public ChannelReader(final ReadableByteChannel in, final int capacity) {
    this(in, ByteBuffer.allocateDirect(capacity));
  }

  public ChannelReader(final ReadableByteChannel in, final int capacity,
    final ByteOrder byteOrder) {
    this(in, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  @Override
  public void close() {
    final ReadableByteChannel in = this.in;
    this.in = null;
    if (in != null) {
      try {
        in.close();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
    this.buffer = null;
    this.tempBuffer = null;
  }

  public byte getByte() {
    if (this.available == 0) {
      read(1);
    }
    this.available--;
    return this.buffer.get();
  }

  public ByteOrder getByteOrder() {
    return this.buffer.order();
  }

  public byte[] getBytes(final byte[] bytes) {
    final int byteCount = bytes.length;
    if (this.available < byteCount) {
      this.buffer.get(bytes, 0, this.available);
      int offset = this.available;
      do {
        int bytesToRead = byteCount - offset;
        read(bytesToRead);
        if (bytesToRead > this.available) {
          bytesToRead = this.available;
        }
        this.available -= bytesToRead;
        this.buffer.get(bytes, offset, bytesToRead);
        offset += bytesToRead;
      } while (offset < byteCount);
    } else {
      this.available -= byteCount;
      this.buffer.get(bytes);
    }
    return bytes;
  }

  public byte[] getBytes(final int byteCount) {
    final byte[] bytes = new byte[byteCount];
    return getBytes(bytes);
  }

  public double getDouble() {
    if (this.available < 8) {
      final ByteBuffer tempBuffer = readTempBytes(8);
      return tempBuffer.getDouble();
    } else {
      this.available -= 8;
      return this.buffer.getDouble();
    }
  }

  public float getFloat() {
    if (this.available < 4) {
      final ByteBuffer tempBuffer = readTempBytes(4);
      return tempBuffer.getFloat();
    } else {
      this.available -= 4;
      return this.buffer.getFloat();
    }
  }

  public int getInt() {
    if (this.available < 4) {
      final ByteBuffer tempBuffer = readTempBytes(4);
      return tempBuffer.getInt();
    } else {
      this.available -= 4;
      return this.buffer.getInt();
    }
  }

  public long getLong() {
    if (this.available < 8) {
      final ByteBuffer tempBuffer = readTempBytes(8);
      return tempBuffer.getLong();
    } else {
      this.available -= 8;
      return this.buffer.getLong();
    }
  }

  public short getShort() {
    if (this.available < 2) {
      final ByteBuffer tempBuffer = readTempBytes(2);
      return tempBuffer.getShort();
    } else {
      this.available -= 2;
      return this.buffer.getShort();
    }
  }

  public String getString(final int byteCount, final Charset charset) {
    final byte[] bytes = getBytes(byteCount);
    int i = 0;
    for (; i < bytes.length; i++) {
      final byte character = bytes[i];
      if (character == 0) {
        return new String(bytes, 0, i, charset);
      }
    }
    return new String(bytes, 0, i, charset);
  }

  public short getUnsignedByte() {
    final byte signedByte = getByte();
    return (short)Byte.toUnsignedInt(signedByte);
  }

  public long getUnsignedInt() {
    final int signedInt = getInt();
    return Integer.toUnsignedLong(signedInt);
  }

  /**
   * Unsigned longs don't actually work in Java
   * @return
   */
  public long getUnsignedLong() {
    final long signedLong = getLong();
    return signedLong;
  }

  public int getUnsignedShort() {
    final short signedShort = getShort();
    return Short.toUnsignedInt(signedShort);
  }

  public String getUsAsciiString(final int byteCount) {
    return getString(byteCount, StandardCharsets.US_ASCII);
  }

  private void read(final int minCount) {
    try {
      this.buffer.clear();
      while (this.available < minCount) {
        final int readCount = this.in.read(this.buffer);
        if (readCount == -1) {
          throw new EOFException();
        } else {
          this.available += readCount;
        }
      }
      this.buffer.flip();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  private ByteBuffer readTempBytes(final int count) {
    final ByteBuffer buffer = this.buffer;
    final ByteBuffer tempBuffer = this.tempBuffer;
    tempBuffer.clear();
    tempBuffer.put(buffer);
    final int readCount = count - this.available;
    this.available = 0;
    read(readCount);
    this.available -= readCount;
    for (int i = 0; i < readCount; i++) {
      final byte b = buffer.get();
      tempBuffer.put(b);
    }
    tempBuffer.flip();
    return tempBuffer;
  }

  public void setByteOrder(final ByteOrder byteOrder) {
    this.buffer.order(byteOrder);
    this.tempBuffer.order(byteOrder);
  }

  public void skipBytes(int count) {
    while (count > this.available) {
      count -= this.available;
      this.available = 0;
      read(count);
    }
    this.available -= count;
    final int position = this.buffer.position();
    this.buffer.position(position + count);
  }
}
