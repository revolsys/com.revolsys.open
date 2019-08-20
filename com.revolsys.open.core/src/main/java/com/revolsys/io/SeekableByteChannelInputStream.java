package com.revolsys.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

public class SeekableByteChannelInputStream extends InputStream {

  private final SeekableByteChannel in;

  private final long position;

  private final int size;

  private final ByteBuffer buffer;

  private int count;

  public SeekableByteChannelInputStream(final SeekableByteChannel in, final long position,
    final int size) {
    super();
    this.in = in;
    this.position = position;
    this.size = size;
    this.buffer = ByteBuffer.allocate(Math.min(size, 8192));
  }

  @Override
  public int available() throws IOException {
    return this.buffer.remaining();
  }

  private ByteBuffer ensureBuffer() throws IOException, EOFException {
    final ByteBuffer buffer = this.buffer;
    if (this.count == 0 || !buffer.hasRemaining()) {
      if (this.count < this.size) {
        buffer.clear();
        final int remaining = this.size - this.count;
        if (remaining < buffer.capacity()) {
          buffer.limit(remaining);
        }
        this.in.position(this.position + this.count);
        final int read = this.in.read(buffer);
        if (read == -1) {
          return null;
        } else {
          this.count += read;
          buffer.flip();
        }
      } else {
        return null;
      }
    }
    return buffer;
  }

  @Override
  public int read() throws IOException {
    final ByteBuffer buffer = ensureBuffer();
    if (buffer == null) {
      return -1;
    } else {
      return buffer.get();
    }
  }

  @Override
  public int read(final byte[] bytes, final int offset, final int length) throws IOException {
    int totalCount = 0;
    while (totalCount < length) {
      final ByteBuffer buffer = ensureBuffer();
      if (buffer == null) {
        return totalCount;
      } else {
        final int readCount = Math.min(length - totalCount, this.buffer.remaining());
        this.buffer.get(bytes, offset + totalCount, readCount);
        totalCount += readCount;
      }
    }
    return totalCount;
  }

}
