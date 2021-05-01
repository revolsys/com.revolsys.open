package com.revolsys.io.channels;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

public class PushbackChannelReader extends ChannelReader {

  private final byte[] buf = new byte[64];

  private int pos = 64;

  public PushbackChannelReader() {
  }

  public PushbackChannelReader(final InputStream in) {
    super(in);
  }

  public PushbackChannelReader(final ReadableByteChannel channel) {
    super(channel);
  }

  public PushbackChannelReader(final ReadableByteChannel channel, final ByteBuffer buffer) {
    super(channel, buffer);
  }

  public PushbackChannelReader(final ReadableByteChannel channel, final int capacity) {
    super(channel, capacity);
  }

  public PushbackChannelReader(final ReadableByteChannel channel, final int capacity,
    final ByteOrder byteOrder) {
    super(channel, capacity, byteOrder);
  }

  @Override
  public byte getByte() {
    if (this.pos < this.buf.length) {
      return this.buf[this.pos++];
    }
    return super.getByte();
  }

  @Override
  public double getDouble() {
    final ByteBuffer tempBuffer = readTempBytes(8);
    return tempBuffer.getDouble();
  }

  @Override
  public float getFloat() {
    final ByteBuffer tempBuffer = readTempBytes(4);
    return tempBuffer.getFloat();
  }

  @Override
  public int getInt() {
    final ByteBuffer tempBuffer = readTempBytes(4);
    return tempBuffer.getInt();
  }

  @Override
  public long getLong() {
    final ByteBuffer tempBuffer = readTempBytes(8);
    return tempBuffer.getLong();
  }

  @Override
  public short getShort() {
    final ByteBuffer tempBuffer = readTempBytes(2);
    return tempBuffer.getShort();
  }

  @Override
  public void init(final byte[] bytes) {
    this.pos = this.buf.length;
    super.init(bytes);
  }

  @Override
  public void init(final byte[] bytes, final int length) {
    this.pos = this.buf.length;
    super.init(bytes, length);
  }

  public boolean isByte(final char c) {
    final byte b = getByte();
    unreadByte(b);
    return b == c;
  }

  public boolean isDigit() {
    final byte b = getByte();
    unreadByte(b);
    return b >= '0' && b <= '9';
  }

  public boolean isWhitespace() {
    final byte b = getByte();
    unreadByte(b);
    return Character.isWhitespace(b);
  }

  @Override
  public long position() {
    return super.position() - (this.buf.length - this.pos);
  }

  @Override
  protected ByteBuffer readTempBytes(final int count) {
    final ByteBuffer tempBuffer = this.tempBuffer;
    tempBuffer.clear();
    getBytes(tempBuffer.array(), 0, count);
    tempBuffer.flip();
    return tempBuffer;
  }

  @Override
  public void seek(final long position) {
    if (isSeekable()) {
      this.pos = this.buf.length;
    }
    super.seek(position);
  }

  @Override
  public void skipBytes(final int count) {
    this.pos += count;
    if (this.pos > this.buf.length) {
      final int superCount = this.pos - this.buf.length;
      this.pos = this.buf.length;
      super.skipBytes(superCount);
    }
  }

  public void unreadByte(final byte b) {
    if (this.pos == 0) {
      throw new IllegalStateException("Push back buffer is full");
    }
    this.buf[--this.pos] = b;
  }
}
