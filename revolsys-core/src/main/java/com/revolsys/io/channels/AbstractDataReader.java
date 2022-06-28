package com.revolsys.io.channels;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jeometry.common.exception.Exceptions;

import com.revolsys.io.DelegatingInputStream;
import com.revolsys.io.EndOfFileException;

public abstract class AbstractDataReader extends InputStream implements DataReader {

  public static final int DEFAULT_BUFFER_SIZE = 8192;

  public static String toString(final ByteBuffer buffer) {
    try {
      final int i = buffer.position();
      final int min = Math.max(0, i - 50);
      final int max = buffer.limit();
      buffer.position(min);
      final byte[] before = new byte[i - min];
      buffer.get(before);
      char c;
      byte[] after;
      try {
        c = (char)buffer.get();
        after = new byte[max - i - 1];
        buffer.get(after);
        buffer.position(i);
      } catch (final Exception e) {
        c = ' ';
        after = new byte[0];
      }
      return new String(before, StandardCharsets.US_ASCII) + " |"
        + ((Character)c).toString().replaceAll("[\\n\\r]", "\\\\n") + "| "
        + new String(after, StandardCharsets.US_ASCII);
    } catch (final Exception e) {
      e.printStackTrace();
    }
    return "sb";
  }

  protected ByteBuffer buffer;

  protected ByteBuffer inBuffer;

  protected final int inSize;

  private long readPosition = 0;

  private final boolean seekable;

  private final byte[] tempBytes = new byte[8];

  private boolean closed;

  protected ByteBuffer tempBuffer = ByteBuffer.wrap(this.tempBytes);

  private byte[] unreadBytes;

  private ByteBuffer unreadBuffer;

  private InputStream wrapStream;

  public AbstractDataReader() {
    this(null, false);
  }

  public AbstractDataReader(final ByteBuffer buffer, final boolean seekable) {
    if (buffer == null) {
      this.inBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    } else {
      this.inBuffer = buffer;
      this.inBuffer.clear();
    }
    this.inBuffer.flip();
    this.inSize = this.inBuffer.capacity();
    this.buffer = this.inBuffer;
    this.tempBuffer.order(this.inBuffer.order());
    this.seekable = seekable;
  }

  protected void afterSeek() {
    clearUnreadBuffer();
    this.buffer.clear();
    this.buffer.flip();
  }

  @Override
  public InputStream asInputStream() {
    return this;
  }

  private ByteBuffer clearUnreadBuffer() {
    if (this.buffer == this.unreadBuffer) {
      if (this.unreadBuffer != null) {
        this.unreadBuffer.position(this.unreadBuffer.capacity());
      }
      this.buffer = this.inBuffer;
    }
    return this.buffer;
  }

  @Override
  public void close() {
    this.closed = true;
    this.buffer = null;
    this.unreadBuffer = null;
    this.inBuffer = null;
    this.tempBuffer = null;
  }

  private int ensureRemaining() {
    ByteBuffer buffer = this.buffer;
    int remaining = buffer.remaining();
    if (remaining <= 0) {
      if (buffer == this.unreadBuffer) {
        buffer = clearUnreadBuffer();
        buffer = this.buffer;
        remaining = buffer.remaining();
        if (remaining > 0) {
          return remaining;
        }
      }
      try {
        buffer.clear();
        while (remaining <= 0) {
          final int readCount = readInternal(buffer);
          buffer.flip();
          if (readCount == -1) {
            return -1;
          } else if (readCount == 0) {
          } else {
            this.readPosition += readCount;
            remaining = buffer.remaining();
          }
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    }
    return remaining;
  }

  @Override
  public int getAvailable() {
    return this.buffer.remaining();
  }

  @Override
  public byte getByte() {
    if (ensureRemaining() == -1) {
      throw new EndOfFileException();
    }
    return this.buffer.get();
  }

  @Override
  public ByteOrder getByteOrder() {
    return this.buffer.order();
  }

  @Override
  public int getBytes(final byte[] bytes, final int offset, final int byteCount) {
    int remaining = ensureRemaining();
    if (remaining == -1) {
      return -1;
    }
    if (remaining < byteCount) {
      int readOffset = remaining;
      this.buffer.get(bytes, offset, readOffset);
      do {
        int bytesToRead = byteCount - readOffset;
        final int limit = this.buffer.limit();
        if (bytesToRead > limit) {
          bytesToRead = limit;
        }
        remaining = ensureRemaining();
        if (remaining == -1) {
          if (readOffset == 0) {
            return -1;
          } else {
            return readOffset;
          }
        }
        if (bytesToRead > remaining) {
          bytesToRead = remaining;
        }
        this.buffer.get(bytes, offset + readOffset, bytesToRead);
        readOffset += bytesToRead;
      } while (readOffset < byteCount);
    } else {
      this.buffer.get(bytes);
    }
    return byteCount;
  }

  @Override
  public byte[] getBytes(final int byteCount) {
    final byte[] bytes = new byte[byteCount];
    getBytes(bytes);
    return bytes;
  }

  @Override
  public byte[] getBytes(final long offset, final int byteCount) {
    throw new IllegalArgumentException("getBytes at offset Not supported");
  }

  @Override
  public double getDouble() {
    if (this.buffer.remaining() < 8) {
      final ByteBuffer tempBuffer = readTempBytes(8);
      return tempBuffer.getDouble();
    } else {
      return this.buffer.getDouble();
    }
  }

  @Override
  public float getFloat() {
    if (this.buffer.remaining() < 4) {
      final ByteBuffer tempBuffer = readTempBytes(4);
      return tempBuffer.getFloat();
    } else {
      return this.buffer.getFloat();
    }
  }

  @Override
  public InputStream getInputStream(final long offset, final int size) {
    throw new IllegalArgumentException("Channel not seekable");
  }

  @Override
  public int getInt() {
    if (this.buffer.remaining() < 4) {
      final ByteBuffer tempBuffer = readTempBytes(4);
      return tempBuffer.getInt();
    } else {
      return this.buffer.getInt();
    }
  }

  @Override
  public long getLong() {
    if (this.buffer.remaining() < 8) {
      final ByteBuffer tempBuffer = readTempBytes(8);
      return tempBuffer.getLong();
    } else {
      return this.buffer.getLong();
    }
  }

  @Override
  public short getShort() {
    if (this.buffer.remaining() < 2) {
      final ByteBuffer tempBuffer = readTempBytes(2);
      return tempBuffer.getShort();
    } else {
      return this.buffer.getShort();
    }
  }

  @Override
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

  @Override
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

  @Override
  public InputStream getWrapStream() {
    if (this.wrapStream == null) {
      this.wrapStream = new DelegatingInputStream(this) {
        @Override
        public void close() throws IOException {
        }
      };
    }
    return this.wrapStream;
  }

  @Override
  public boolean isByte(final byte expected) {
    if (ensureRemaining() == -1) {
      return false;
    }
    final byte b = this.buffer.get();
    unreadByte(b);
    return expected == b;
  }

  @Override
  public boolean isByte(final char expected) {
    if (ensureRemaining() == -1) {
      return false;
    }
    final byte b = this.buffer.get();
    unreadByte(b);
    return expected == ((char)b & 0xFF);
  }

  @Override
  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public boolean isSeekable() {
    return this.seekable;
  }

  @Override
  public long position() {
    long position = this.readPosition - this.buffer.remaining();
    if (this.buffer != this.inBuffer) {
      position -= this.inBuffer.remaining();
    }
    return position;
  }

  @Override
  public int read() {
    if (ensureRemaining() == -1) {
      return -1;
    }
    final byte b = this.buffer.get();
    return b & 0xff;
  }

  @Override
  public int read(final byte[] bytes, final int offset, int length) throws IOException {
    final int remaining = ensureRemaining();
    if (remaining == -1) {
      return -1;
    }
    if (length > remaining) {
      length = remaining;
    }
    this.buffer.get(bytes, offset, length);
    return length;
  }

  @Override
  public int read(final ByteBuffer buffer) {
    final int readRemaining = ensureRemaining();
    if (readRemaining == -1) {
      return -1;
    }
    final ByteBuffer readBuffer = this.buffer;
    final int writerRemaining = buffer.remaining();
    if (readRemaining <= writerRemaining) {
      buffer.put(readBuffer);
      return readRemaining;
    } else {
      final int readLimit = readBuffer.limit();
      readBuffer.limit(readBuffer.position() + writerRemaining);
      buffer.put(readBuffer);
      readBuffer.limit(readLimit);
      return writerRemaining;
    }
  }

  protected abstract int readInternal(ByteBuffer buffer) throws IOException;

  protected ByteBuffer readTempBytes(final int count) {
    if (getBytes(this.tempBytes, 0, count) == -1) {
      throw new EndOfFileException();
    }
    this.tempBuffer.clear();
    this.tempBuffer.limit(count);
    return this.tempBuffer;
  }

  @Override
  public void seek(final long position) {
    final long currentPosition = position();
    if (position >= currentPosition) {
      final long offset = position - currentPosition;

      skipBytes((int)offset);
    } else {
      throw new IllegalArgumentException("Seek no supported");
    }
  }

  @Override
  public void seekEnd(final long distance) {
    throw new IllegalArgumentException("Seek not supported");
  }

  @Override
  public void setByteOrder(final ByteOrder byteOrder) {
    this.buffer.order(byteOrder);
    this.tempBuffer.order(byteOrder);
  }

  @Override
  public DataReader setUnreadSize(final int unreadSize) {
    if (this.unreadBuffer == null) {
      this.unreadBytes = new byte[unreadSize];
      this.unreadBuffer = ByteBuffer.wrap(this.unreadBytes)
        .order(this.inBuffer.order())
        .position(unreadSize);
    } else {
      throw new IllegalArgumentException("Cannot change unread size");
    }
    return this;
  }

  @Override
  public void skipBytes(int count) {
    int remaining = this.buffer.remaining();
    if (count < remaining) {
    } else if (isSeekable()) {
      final long newPosition = position() + count;
      seek(newPosition);
      return;
    } else {
      while (count > remaining) {
        count -= remaining;
        this.buffer.position(this.buffer.limit());
        remaining = ensureRemaining();
        if (remaining == -1) {
          return;
        }
      }
    }
    final int position = this.buffer.position() + count;
    this.buffer.position(position);
  }

  @Override
  public String toString() {
    return toString(this.buffer);
  }

  @Override
  public void unreadByte(final byte b) {
    ByteBuffer buffer = this.buffer;
    final ByteBuffer unreadBuffer = this.unreadBuffer;
    if (buffer.remaining() == buffer.limit()) {
      if (buffer == unreadBuffer) {
        throw new IllegalArgumentException("Exceeded unread capacity");
      } else {
        buffer = this.buffer = this.unreadBuffer;
      }
    }

    final int newPosition = this.buffer.position() - 1;
    if (buffer == unreadBuffer) {
      this.unreadBytes[newPosition] = b;
      unreadBuffer.position(newPosition);
    } else {
      this.buffer.position(newPosition);
      this.buffer.put(b);
      this.buffer.position(newPosition);
    }
  }
}
