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

  private WritableByteChannel channel;

  private final boolean closeChannel;

  /**
   * <p>Create a new ChannelWriter with buffer of 8192 bytes.<p>
   *
   * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
   *
   * @param channel The channel.
   */
  public ChannelWriter(final WritableByteChannel channel) {
    this(channel, 8192);
  }

  /**
   * <p>Create a new ChannelWriter with buffer of 8192 bytes.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel) {
    this(channel, closeChannel, 8192);
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   * @param buffer The temporary buffer used to write to the channel.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel,
    final ByteBuffer buffer) {
    this.channel = channel;
    this.closeChannel = closeChannel;
    this.buffer = buffer;
    this.capacity = buffer.capacity();
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   * @param capacity The size of the temporary buffer.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel,
    final int capacity) {
    this(channel, closeChannel, ByteBuffer.allocateDirect(capacity));
  }

  /**
   * <p>Create a new ChannelWriter.<p>
    *
   * @param channel The channel.
   * @param closeChannel Flag indicating if the channel should automatically be closed.
   * @param capacity The size of the temporary buffer.
   * @param byteOrder The byte order of the buffer.
   */
  public ChannelWriter(final WritableByteChannel channel, final boolean closeChannel,
    final int capacity, final ByteOrder byteOrder) {
    this(channel, closeChannel, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
   *
   * @param channel The channel.
   * @param buffer The temporary buffer used to write to the channel.
   */
  public ChannelWriter(final WritableByteChannel channel, final ByteBuffer buffer) {
    this(channel, false, buffer);
  }

  /**
  * <p>Create a new ChannelWriter.<p>
  *
  * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
  *
  * @param channel The channel.
  * @param capacity The size of the temporary buffer.
  */
  public ChannelWriter(final WritableByteChannel channel, final int capacity) {
    this(channel, ByteBuffer.allocateDirect(capacity));
  }

  /**
   * <p>Create a new ChannelWriter.<p>
   *
   * <p><b>NOTE: The underlying channel will not be automatically closed.</p>
   *
   * @param channel The channel.
   * @param capacity The size of the temporary buffer.
   * @param byteOrder The byte order of the buffer.
   */
  public ChannelWriter(final WritableByteChannel channel, final int capacity,
    final ByteOrder byteOrder) {
    this(channel, ByteBuffer.allocateDirect(capacity));
    setByteOrder(byteOrder);
  }

  /**
   * Close this writer but not the underlying channel.
   */
  @Override
  public void close() {
    write();
    final WritableByteChannel channel = this.channel;
    if (channel != null) {
      this.channel = null;
      if (this.closeChannel) {
        try {
          channel.close();
        } catch (final IOException e) {
          throw Exceptions.wrap(e);
        }
      }
    }
    this.buffer = null;
  }

  public void flush() {
    write();
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
      final ByteBuffer buffer = this.buffer;
      final WritableByteChannel channel = this.channel;
      if (channel != null) {
        buffer.flip();
        final int size = buffer.remaining();
        int totalWritten = 0;
        while (totalWritten < size) {
          final int written = channel.write(buffer);
          if (written == -1) {
            throw new EOFException();
          }
          totalWritten += written;
        }
        buffer.clear();
        this.available = this.capacity;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }
}
