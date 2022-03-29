package com.revolsys.io;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.charset.StandardCharsets;
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
    public int read(final ByteBuffer dst) throws IOException {
      if (!isOpen()) {
        throw new ClosedChannelException();
      }
      if (this.offset < FileBackedOutputStreamBuffer.this.bufferSize) {
        final int count = Math.min(FileBackedOutputStreamBuffer.this.bufferSize - this.offset,
          dst.remaining());
        FileBackedOutputStreamBuffer.this.buffer
          .limit(FileBackedOutputStreamBuffer.this.buffer.position() + count);
        dst.put(FileBackedOutputStreamBuffer.this.buffer);
        FileBackedOutputStreamBuffer.this.buffer
          .limit(FileBackedOutputStreamBuffer.this.bufferSize);
        this.offset += count;
        return count;
      } else {
        if (this.fileChannel == null) {
          if (FileBackedOutputStreamBuffer.this.file == null) {
            return -1;
          } else {
            this.fileChannel = FileChannel.open(FileBackedOutputStreamBuffer.this.file,
              StandardOpenOption.READ);
          }
        }
        return this.fileChannel.read(dst);
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
      this.buffer.flip();
      if (this.out != null) {
        this.out.flush();
        this.out.close();
        this.out = null;
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public synchronized void flush() throws IOException {
    if (!this.closed) {
      if (this.out != null) {
        this.out.flush();
      }
    }
  }

  public long getSize() {
    return this.size;
  }

  public InputStream newInputStream() {
    return Channels.newInputStream(newReadChannel());
  }

  public ReadableByteChannel newReadChannel() {
    this.buffer.flip();
    return new ReadChannel();
  }

  public java.io.Reader newReader() {
    return Channels.newReader(newReadChannel(), StandardCharsets.UTF_8);
  }

  public java.io.Writer newWriter() {
    return new OutputStreamWriter(new IgnoreCloseDelegatingOutputStream(this),
      StandardCharsets.UTF_8);
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
        final int remaining = this.buffer.remaining();
        if (remaining > 0) {
          final int count = Math.min(remaining, length);
          this.buffer.put(source, offset, count);
          this.size += count;
          length -= count;
          offset += count;
        }
        if (length > 0) {
          requireFile();
          this.out.write(source, offset, length);
          this.size += length;
        }
      }
    }
  }

  @Override
  public synchronized void write(final int b) throws IOException {
    if (this.closed) {
      throw new IOException("Closed");
    } else {
      final int remaining = this.buffer.remaining();
      if (remaining > 0) {
        this.buffer.put((byte)b);
      } else {
        requireFile();
        this.out.write(b);

      }
      this.size += 1;
    }
  }

}
