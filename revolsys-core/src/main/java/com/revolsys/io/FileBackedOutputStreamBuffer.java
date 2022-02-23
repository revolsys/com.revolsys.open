package com.revolsys.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.jeometry.common.exception.Exceptions;

public class FileBackedOutputStreamBuffer extends OutputStream {

  private final class ReadChannel extends AbstractInterruptibleChannel
    implements ReadableByteChannel {

    private int offset;

    private FileChannel fileChannel;

    @Override
    protected void implCloseChannel() throws IOException {
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      if (offset < bufferSize) {
        int count = Math.min(bufferSize - offset, dst.remaining());
        buffer.limit(buffer.position() + count);
        dst.put(buffer);
        buffer.limit(buffer.capacity());
        offset += count;
        return count;
      } else {
        if (fileChannel == null) {
          if (file == null) {
            return -1;
          } else {
            fileChannel = FileChannel.open(file, StandardOpenOption.READ);
          }
        }
        return fileChannel.read(dst);
      }
    }
  }

  final ByteBuffer buffer;

  private OutputStream out;

  private boolean closed;

  final int bufferSize;

  private long size = 0;

  Path file;

  public FileBackedOutputStreamBuffer(final int bufferSize) {
    this.bufferSize = bufferSize;
    this.buffer = ByteBuffer.allocate(bufferSize);
  }

  @Override
  public synchronized void close() throws IOException {
    if (!this.closed) {
      this.closed = true;
      try {
        if (this.out != null) {
          this.out.close();
        }
      } finally {
        if (this.file != null) {
          Files.deleteIfExists(this.file);
        }
      }
      this.out = null;
    }
  }

  public void flip() {

    try {
      buffer.flip();
      if (out != null) {
        this.out.flush();
        this.out.close();
        this.out = null;
      }
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public synchronized void flush() throws IOException {
    if (!this.closed) {
      this.out.flush();
    }
  }

  public long getSize() {
    return this.size;
  }

  public ReadableByteChannel newReadChannel() {
    return new ReadChannel();
  }

  private void requireFile() throws IOException {
    if (this.file == null) {
      this.file = Files.createTempFile("file", ".bin");
      this.out = new BufferedOutputStream(Files.newOutputStream(this.file));
    }
  }

  @Override
  public synchronized void write(final byte[] source, int offset, int length) throws IOException {

    if (this.closed) {
      throw new IOException("Closed");
    } else {
      if (length > 0) {
        int remaining = buffer.remaining();
        if (remaining > 0) {
          int count = Math.min(remaining, length);
          buffer.put(source, offset, count);
          size += count;
          length -= count;
          offset += count;
        }
        if (length > 0) {
          requireFile();
          this.out.write(source, offset, length);
          size += length;
        }
      }
    }
  }

  @Override
  public synchronized void write(final int b) throws IOException {
    if (this.closed) {
      throw new IOException("Closed");
    } else {
      int remaining = buffer.remaining();
      if (remaining > 0) {
        buffer.put((byte)b);
      } else {
        requireFile();
        this.out.write(b);

      }
      size += 1;
    }
  }

}
