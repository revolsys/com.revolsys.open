package com.revolsys.elevation.cloud.las.zip;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteOrder;

import com.revolsys.io.channels.ChannelReader;
import com.revolsys.math.arithmeticcoding.ArithmeticDecoder;

public class ArithmeticDecoderByteArray extends ArithmeticDecoder {

  private int size;

  public ArithmeticDecoderByteArray() {
  }

  public int initializeBytes(final ChannelReader reader, final boolean enabled) {
    if (this.size <= 0) {
      return 0;
    } else if (enabled) {
      if (this.size > 0) {
        final byte[] bytes = new byte[this.size];
        reader.getBytes(bytes);
        final InputStream in = new ByteArrayInputStream(bytes);
        final ChannelReader newReader = new ChannelReader(in);
        newReader.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        init(newReader, true);
      }
      return this.size;
    } else {
      reader.skipBytes(this.size);
      return 0;
    }
  }

  public int readSize(final ChannelReader in) {
    this.size = in.getInt();
    return this.size;
  }

}
