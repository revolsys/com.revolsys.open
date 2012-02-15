package com.revolsys.jdbc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

public class ByteArrayBlob implements Blob {

  private final byte[] content;

  private final long contentLength;

  public ByteArrayBlob(final byte[] content) {
    this.content = content;
    this.contentLength = content.length;
  }

  public void free() throws SQLException {
    throw new UnsupportedOperationException();
  }

  public InputStream getBinaryStream() throws SQLException {
    if (content == null) {
      return null;
    } else {
      return new ByteArrayInputStream(content);
    }
  }

  public InputStream getBinaryStream(final long pos, final long length)
    throws SQLException {
    return new ByteArrayInputStream(content, (int)pos - 1, (int)length);
  }

  public byte[] getBytes(final long pos, final int length) throws SQLException {
    final byte[] bytes = new byte[length];
    System.arraycopy(content, 0, bytes, (int)pos - 1, length);
    throw new UnsupportedOperationException();
  }

  public long length() throws SQLException {
    return contentLength;
  }

  public long position(final Blob pattern, final long start)
    throws SQLException {
    throw new UnsupportedOperationException();
  }

  public long position(final byte pattern[], final long start)
    throws SQLException {
    throw new UnsupportedOperationException();
  }

  public OutputStream setBinaryStream(final long pos) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public int setBytes(final long pos, final byte[] bytes) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public int setBytes(
    final long pos,
    final byte[] bytes,
    final int offset,
    final int len) throws SQLException {
    throw new UnsupportedOperationException();
  }

  public void truncate(final long len) throws SQLException {
    throw new UnsupportedOperationException();
  }

}
