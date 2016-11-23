package com.revolsys.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface Buffers {

  static void putDouble(final ByteBuffer buffer, final double value, final double scale) {
    if (Double.isFinite(value)) {
      final int intX = (int)Math.round(value * scale);
      buffer.putInt(intX);
    } else {
      buffer.putInt(Integer.MIN_VALUE);
    }
  }

  static int readAll(final ReadableByteChannel channel, final ByteBuffer buffer)
    throws IOException {
    do {
      final int readCount = channel.read(buffer);
      if (readCount == -1) {
        if (buffer.position() == 0) {
          return -1;
        }
      }
    } while (buffer.hasRemaining());
    final int position = buffer.position();
    buffer.flip();
    return position;
  }

  static void writeAll(final WritableByteChannel out, final ByteBuffer buffer)
    throws IOException {
    buffer.flip();
    while (buffer.hasRemaining()) {
      out.write(buffer);
    }
    buffer.clear();
  }
}
