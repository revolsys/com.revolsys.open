package com.revolsys.io;

import java.nio.ByteBuffer;

public interface Buffers {

  static void putDouble(final ByteBuffer buffer, final double value, final double scale) {
    if (Double.isFinite(value)) {
      final int intX = (int)Math.round(value * scale);
      buffer.putInt(intX);
    } else {
      buffer.putInt(Integer.MIN_VALUE);
    }
  }

}
