package com.revolsys.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

public class StringClob implements Clob {

  private String string;

  public StringClob(
    final String string) {
    this.string = string;
  }

  public void free()
    throws SQLException {
    string = null;
  }

  public InputStream getAsciiStream()
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public Reader getCharacterStream()
    throws SQLException {
    return new StringReader(string);
  }

  public Reader getCharacterStream(
    final long offset,
    final long length)
    throws SQLException {
    return new StringReader(string.substring((int)offset - 1, (int)length));
  }

  public String getSubString(
    final long pos,
    final int len)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public long length()
    throws SQLException {
    return string.length();
  }

  public long position(
    final Clob colb,
    final long pos)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public long position(
    final String string,
    final long pos)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public OutputStream setAsciiStream(
    final long pos)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public Writer setCharacterStream(
    final long pos)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public int setString(
    final long pos,
    final String string)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public int setString(
    final long pos,
    final String string,
    final int i,
    final int j)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }

  public void truncate(
    final long pos)
    throws SQLException {
    throw new UnsupportedOperationException("Cannot modify Clob");
  }
}
