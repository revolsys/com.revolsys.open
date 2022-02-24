package com.revolsys.io.channels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.BaseCloseable;

public interface DataReader extends BaseCloseable {

  InputStream asInputStream();

  @Override
  void close();

  default int fillBuffer(final ByteBuffer buffer) {
    try {
      buffer.clear();
      final int size = buffer.remaining();
      int totalReadCount = 0;
      while (totalReadCount < size) {
        final int readCount = read(buffer);
        if (readCount == -1) {
          if (totalReadCount == 0) {
            return -1;
          } else {
            final int bufferPosition = buffer.position();
            buffer.flip();
            return bufferPosition;
          }
        } else {
          totalReadCount += readCount;
        }
      }
      final int bufferPosition = buffer.position();
      buffer.flip();
      return bufferPosition;
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  }

  int getAvailable();

  byte getByte();

  ByteOrder getByteOrder();

  default int getBytes(final byte[] bytes) {
    return getBytes(bytes, 0, bytes.length);
  }

  int getBytes(byte[] bytes, int offset, int byteCount);

  byte[] getBytes(int byteCount);

  byte[] getBytes(long offset, int byteCount);

  double getDouble();

  float getFloat();

  InputStream getInputStream(long offset, int size);

  int getInt();

  long getLong();

  short getShort();

  default String getString(final int byteCount, final Charset charset) {
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

  default String getStringUtf8ByteCount() {
    final int byteCount = getInt();
    if (byteCount < 0) {
      return null;
    } else if (byteCount == 0) {
      return "";
    } else {
      return getString(byteCount, StandardCharsets.UTF_8);
    }
  }

  default short getUnsignedByte() {
    final byte signedByte = getByte();
    return (short)Byte.toUnsignedInt(signedByte);
  }

  default long getUnsignedInt() {
    final int signedInt = getInt();
    return Integer.toUnsignedLong(signedInt);
  }

  /**
   * Unsigned longs don't actually work channel Java
   * @return
   */
  default long getUnsignedLong() {
    final long signedLong = getLong();
    return signedLong;
  }

  default int getUnsignedShort() {
    final short signedShort = getShort();
    return Short.toUnsignedInt(signedShort);
  }

  default String getUsAsciiString(final int byteCount) {
    return getString(byteCount, StandardCharsets.US_ASCII);
  }

  InputStream getWrapStream();

  boolean isByte(byte expected);

  boolean isByte(char expected);

  default boolean isBytes(final byte[] bytes) {
    for (final byte e : bytes) {
      final byte a = getByte();
      if (a != e) {
        return false;
      }
    }
    return true;
  }

  boolean isClosed();

  default boolean isEof() {
    final int b = read();
    if (b < 0) {
      return false;
    } else {
      unreadByte((byte)b);
      return false;
    }
  }

  boolean isSeekable();

  long position();

  int read();

  int read(byte[] bytes, int offset, int length) throws IOException;

  int read(ByteBuffer buffer);

  void seek(long position);

  void seekEnd(long distance);

  void setByteOrder(ByteOrder byteOrder);

  DataReader setUnreadSize(int unreadSize);

  void skipBytes(int count);

  default boolean skipIfChar(final char c) {
    if (isByte(c)) {
      getByte();
      return true;
    } else {
      return false;
    }
  }

  default void skipWhitespace() {
    byte b;
    do {
      b = getByte();
    } while (Character.isWhitespace(b));
    if (b != -1) {
      unreadByte(b);
    }
  }

  void unreadByte(byte b);

}
