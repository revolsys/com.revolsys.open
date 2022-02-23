package com.revolsys.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class Channels {

  public static void copy(final FileChannel in, final FileChannel out, final long size)
    throws IOException {
    long count = 0;
    while (count < size) {
      count += out.transferFrom(in, count, size - count);
    }
  }

  public static void copy(final FileChannel in, final WritableByteChannel out) throws IOException {
    final long size = in.size();
    long count = 0;
    while (count < size) {
      count += in.transferTo(count, size, out);
    }
  }

  public static void copy(final ReadableByteChannel in, final FileChannel out, final long size)
    throws IOException {
    if (in instanceof FileChannel) {
      copy((FileChannel)in, out, size);
    } else {
      long ofset = 0;
      final int blockSize = 8196;
      while (ofset < size) {
        long remaining = size - ofset;
        long readCount;
        if (remaining < blockSize) {
          readCount = out.transferFrom(in, ofset, remaining);
        } else {
          readCount = out.transferFrom(in, ofset, blockSize);
        }
        remaining -= readCount;
        ofset += readCount;
      }
    }
  }

  public static long copy(final ReadableByteChannel in, final WritableByteChannel out, long size)
    throws IOException {
    if (in instanceof FileChannel) {
      copy(in, out, size);
      return size;
    } else {
      final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
      long remaining = size;
      while (remaining > 0) {
        buffer.clear();
        if (remaining < 8192) {
          buffer.limit((int)remaining);
        }
        int readCount = in.read(buffer);
        if (readCount < 0) {
          break;
        }
        remaining -= readCount;
        buffer.flip();
        int writeCount = 0;
        while (writeCount < readCount) {
          writeCount += out.write(buffer);
        }
      }
      buffer.clear();
      return size - remaining;
    }
  }
}
