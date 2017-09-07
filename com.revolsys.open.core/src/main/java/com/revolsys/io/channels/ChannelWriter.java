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
import java.nio.channels.WritableByteChannel;

import com.revolsys.io.BaseCloseable;
import com.revolsys.util.Exceptions;

public class ChannelWriter implements BaseCloseable {

  private int available = 0;

  private ByteBuffer buffer;

  private final int capacity;

  private WritableByteChannel out;

  public ChannelWriter(final WritableByteChannel out) {
    this(out, 8192);
  }

  public ChannelWriter(final WritableByteChannel out, final ByteBuffer buffer) {
    this.out = out;
    this.buffer = buffer;
    this.capacity = buffer.capacity();
  }

  public ChannelWriter(final WritableByteChannel out, final int capacity) {
    this(out, ByteBuffer.allocateDirect(capacity));
  }

  public ChannelWriter(final WritableByteChannel out, final int capacity,
    final ByteOrder byteOrder) {
    this(out, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  @Override
  public void close() {
    final WritableByteChannel out = this.out;
    if (out != null) {
      write();
      this.out = null;
      try {
        out.close();
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
    this.buffer = null;
  }

  public ByteOrder getByteOrder() {
    return this.buffer.order();
  }

  public void putByte(final byte b) {
    if (this.available == 0) {
      write();
    }
    this.available--;
    this.buffer.put(b);
  }

  public void putBytes(final byte[] bytes) {
    putBytes(bytes, bytes.length);
  }

  public void putBytes(final byte[] bytes, final int length) {
    if (length < this.available) {
      this.available -= length;
      this.buffer.put(bytes, 0, length);
    } else {
      this.buffer.put(bytes, 0, this.available);
      int offset = this.available;
      do {
        int bytesToWrite = length - offset;
        write();
        if (bytesToWrite > this.available) {
          bytesToWrite = this.available;
        }
        this.available -= bytesToWrite;
        this.buffer.put(bytes, offset, bytesToWrite);
        offset += bytesToWrite;
      } while (offset < length);
    }
  }

  public void putDouble(final double d) {
    if (this.available < 8) {
      write();
    }
    this.available -= 8;
    this.buffer.putDouble(d);
  }

  public void putFloat(final float f) {
    if (this.available < 4) {
      write();
    }
    this.available -= 4;
    this.buffer.putFloat(f);
  }

  public void putInt(final int i) {
    if (this.available < 4) {
      write();
    }
    this.available -= 4;
    this.buffer.putInt(i);
  }

  public void putLong(final long l) {
    if (this.available < 8) {
      write();
    }
    this.available -= 8;
    this.buffer.putLong(l);
  }

  public void putShort(final short s) {
    if (this.available < 2) {
      write();
    }
    this.available -= 2;
    this.buffer.putShort(s);
  }

  public void putUnsignedByte(final short b) {
    putByte((byte)b);
  }

  public void putUnsignedInt(final long i) {
    putInt((int)i);
  }

  /**
   * Unsigned longs don't actually work in Java
   * @return
   */
  public void putUnsignedLong(final long l) {
    putLong(l);
  }

  public void putUnsignedShort(final int s) {
    putShort((short)s);
  }

  public void setByteOrder(final ByteOrder byteOrder) {
    this.buffer.order(byteOrder);
  }

  private void write() {
    try {
      final WritableByteChannel out = this.out;
      final ByteBuffer buffer = this.buffer;
      buffer.flip();
      final int size = buffer.remaining();
      int totalWritten = 0;
      while (totalWritten < size) {
        final int written = out.write(buffer);
        if (written == -1) {
          throw new EOFException();
        }
        totalWritten += written;
      }
      buffer.clear();
      this.available = this.capacity;
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
