package com.revolsys.jdbc.io;

import org.springframework.transaction.support.ResourceHolderSupport;

public class JdbcWriterResourceHolder extends ResourceHolderSupport {
  private JdbcWriter writer;

  public JdbcWriterResourceHolder(final JdbcWriter writer) {
    this.writer = writer;
  }

  public JdbcWriter getWriter() {
    return writer;
  }

  public boolean hasWriter() {
    return writer != null;
  }

  @Override
  public void released() {
    super.released();
    if (!isOpen() && writer != null) {
      writer.flush();
      writer = null;
    }
  }

  public void setWriter(final JdbcWriter writer) {
    this.writer = writer;
  }

  public boolean writerEquals(final JdbcWriter writer) {
    if (hasWriter()) {
      return this.writer == writer || this.equals(writer);
    } else {
      return false;
    }
  }
}
