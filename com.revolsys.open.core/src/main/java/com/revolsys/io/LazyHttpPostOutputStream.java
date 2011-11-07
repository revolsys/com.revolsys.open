package com.revolsys.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LazyHttpPostOutputStream extends OutputStream {
  private HttpURLConnection connection;

  private final String contentType;

  private InputStream in;

  private OutputStream out;

  private final String url;

  public LazyHttpPostOutputStream(final String url, final String contentType) {
    this.url = url;
    this.contentType = contentType;
  }

  @Override
  public void close() throws IOException {
    out.flush();
    out.close();
    in = connection.getInputStream();
    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new RuntimeException("Result data not accepted by server "
        + connection.getResponseCode() + " " + connection.getResponseMessage());
    }
    in.close();
  }

  private void init() throws IOException {
    connection = (HttpURLConnection)new URL(url).openConnection();
    connection.setRequestMethod("POST");
    connection.setRequestProperty("Content-Type", contentType);
    connection.setChunkedStreamingMode(4096);
    connection.setDoOutput(true);
    connection.setDoInput(true);
    out = connection.getOutputStream();

  }

  @Override
  public void write(final byte[] b) throws IOException {
    if (out == null) {
      init();
    }
    out.write(b);
  }

  @Override
  public void write(final byte[] b, final int off, final int len)
    throws IOException {
    if (out == null) {
      init();
    }
    out.write(b, off, len);
  }

  @Override
  public void write(final int b) throws IOException {
    if (out == null) {
      init();
    }
    out.write(b);
  }
}
