package com.revolsys.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.http.entity.AbstractHttpEntity;

import com.revolsys.io.IgnoreCloseDelegatingOutputStream;
import com.revolsys.io.channels.Channels;

public class ReadChannelHttpEntity extends AbstractHttpEntity {
  public static interface WriteTo {
    void writeTo(OutputStream out) throws IOException;
  }

  private final long length;

  private final ReadableByteChannel channel;

  public ReadChannelHttpEntity(final ReadableByteChannel channel, final long length) {
    this.channel = channel;
    this.length = length;
  }

  @Override
  public InputStream getContent() throws IOException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getContentLength() {
    return this.length;
  }

  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public boolean isStreaming() {
    return true;
  }

  @Override
  public void writeTo(final OutputStream out) throws IOException {
    try (
      WritableByteChannel outChannel = java.nio.channels.Channels
        .newChannel(new IgnoreCloseDelegatingOutputStream(out))) {
      Channels.copy(this.channel, outChannel, this.length);
    }
  }

}
