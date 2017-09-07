package com.revolsys.io.channels;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class Channels {

  public static void copy(final FileChannel in, final WritableByteChannel out) throws IOException {
    final long size = in.size();
    long count = 0;
    while (count < size) {
      count += in.transferTo(count, size, out);
    }
  }

}
