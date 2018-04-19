package com.revolsys.io.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class Channels {

  public static void copy(final FileChannel in, final WritableByteChannel out) throws IOException {
    final long size = in.size();
    long count = 0;
    while (count < size) {
      count += in.transferTo(count, size, out);
    }
  }

  public static void copy(final ReadableByteChannel in, final FileChannel out, final long size)
    throws IOException {
    if (size < 0) {
      final ByteBuffer buffer = ByteBuffer.allocateDirect(8192);
      for (int readCount = in.read(buffer); readCount != -1; readCount = in.read(buffer)) {
        buffer.flip();
        for (int writeCount = out.write(buffer); writeCount < readCount; writeCount += out
          .write(buffer)) {
        }
        buffer.clear();
      }
    } else {
      long count = 0;
      while (count < size) {
        count += out.transferFrom(in, count, size - count);
      }
    }
  }

}
