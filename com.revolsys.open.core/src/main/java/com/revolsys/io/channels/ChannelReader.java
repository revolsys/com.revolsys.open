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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;
import com.revolsys.io.EndOfFileException;
import com.revolsys.io.SeekableByteChannelInputStream;
import com.revolsys.spring.resource.Resource;

public class ChannelReader implements BaseCloseable {

  public static ChannelReader newChannelReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    if (resource == null) {
      return null;
    } else {
      return resource.newChannelReader();
    }
  }

  private ByteBuffer buffer;

  private ReadableByteChannel channel;

  private int available = 0;

  private ByteBuffer tempBuffer = ByteBuffer.allocate(8);

  public ChannelReader() {
    this((ReadableByteChannel)null);
  }

  public ChannelReader(final InputStream in) {
    this(Channels.newChannel(in));
  }

  public ChannelReader(final ReadableByteChannel channel) {
    this(channel, 8192);
  }

  public ChannelReader(final ReadableByteChannel channel, final ByteBuffer buffer) {
    this.channel = channel;
    if (buffer == null) {
      this.buffer = ByteBuffer.allocateDirect(8192);
    } else {
      this.buffer = buffer;
      this.buffer.clear();
    }
    this.tempBuffer.order(this.buffer.order());
  }

  public ChannelReader(final ReadableByteChannel channel, final int capacity) {
    this(channel, ByteBuffer.allocateDirect(capacity));
  }

  public ChannelReader(final ReadableByteChannel channel, final int capacity,
    final ByteOrder byteOrder) {
    this(channel, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  @Override
  public void close() {
    final ReadableByteChannel channel = this.channel;
    this.channel = null;
    if (channel != null) {
      try {
        channel.close();
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
      int offset = this.available;
      this.buffer.get(bytes, 0, offset);
      this.available = 0;
      do {
        int bytesToRead = byteCount - offset;
        final int limit = this.buffer.limit();
        if (bytesToRead > limit) {
          bytesToRead = limit;
        }
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

  public byte[] getBytes(final long offset, final int byteCount) {
    if (this.channel instanceof SeekableByteChannel) {
      final byte[] bytes = new byte[byteCount];
      try {
        final SeekableByteChannel seekChannel = (SeekableByteChannel)this.channel;
        seekChannel.position(offset);
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        final int count = seekChannel.read(buffer);
        if (count == -1) {
          throw new EndOfFileException();
        } else if (count == byteCount) {
          return bytes;
        } else {
          final byte[] subBytes = new byte[count];
          System.arraycopy(bytes, 0, subBytes, 0, count);
          return subBytes;
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      throw new IllegalArgumentException("Not supported");
    }
  }

  public ReadableByteChannel getChannel() {
    return this.channel;
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

  public InputStream getInputStream(final long offset, final int size) {
    if (this.channel instanceof SeekableByteChannel) {
      final SeekableByteChannel seekableChannel = (SeekableByteChannel)this.channel;
      return new SeekableByteChannelInputStream(seekableChannel, offset, size);
    } else {
      throw new IllegalArgumentException("Channel not seekable");
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

  public String getStringUtf8ByteCount() {
    final int byteCount = getInt();
    if (byteCount < 0) {
      return null;
    } else if (byteCount == 0) {
      return "";
    } else {
      return getString(byteCount, StandardCharsets.UTF_8);
    }
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
   * Unsigned longs don't actually work channel Java
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

  public void init(final byte[] bytes) {
    this.available = 0;
    if (bytes == null) {
      this.channel = null;
    } else {
      this.channel = Channels.newChannel(new ByteArrayInputStream(bytes));
    }
  }

  public void init(final byte[] bytes, final int length) {
    this.available = 0;
    if (bytes == null) {
      this.channel = null;
    } else {
      this.channel = Channels.newChannel(new ByteArrayInputStream(bytes, 0, length));
    }
  }

  public boolean isSeekable() {
    return this.channel instanceof SeekableByteChannel;
  }

  public long position() {
    if (this.channel instanceof SeekableByteChannel) {
      final SeekableByteChannel channel = (SeekableByteChannel)this.channel;
      try {
        return channel.position() - this.available;
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    } else {
      return -1;
    }
  }

  private void read(final int minCount) {
    final ReadableByteChannel channel = this.channel;
    final ByteBuffer buffer = this.buffer;
    int available = this.available;
    try {
      buffer.clear();
      while (available < minCount) {
        final int readCount = channel.read(buffer);
        if (readCount == -1) {
          throw new EndOfFileException();
        } else {
          available += readCount;
        }
      }
      buffer.flip();
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    } finally {
      this.available = available;
    }
  }

  public ByteBuffer readByteByteBuffer(final long offset, final int size) {
    final long position = position();
    final ByteBuffer buffer = ByteBuffer.allocateDirect(size);
    seek(offset);
    do {
      try {
        this.channel.read(buffer);
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    } while (buffer.hasRemaining());
    seek(position);
    buffer.flip();
    return buffer;
  }

  private ByteBuffer readTempBytes(final int count) {
    final ByteBuffer buffer = this.buffer;
    final ByteBuffer tempBuffer = this.tempBuffer;
    tempBuffer.clear();
    if (this.available > 0) {
      tempBuffer.put(buffer);
    }
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

  public void seek(final long position) {
    try {
      if (this.channel instanceof SeekableByteChannel) {
        final long currentPosition = position();
        if (position != currentPosition) {
          final SeekableByteChannel channel = (SeekableByteChannel)this.channel;
          channel.position(position);
          this.available = 0;
          this.buffer.clear();
        }
      } else {
        throw new IllegalArgumentException("Seek not supported");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public void seekEnd(final long distance) {
    try {
      if (this.channel instanceof SeekableByteChannel) {
        final SeekableByteChannel channel = (SeekableByteChannel)this.channel;
        final long position = channel.size() - distance;
        seek(position);
      } else {
        throw new IllegalArgumentException("Seek not supported");
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
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
