package com.revolsys.jdbc.io;

import org.springframework.transaction.support.ResourceHolderSupport;

public class JdbcWriterResourceHolder extends ResourceHolderSupport {
  private JdbcWriterImpl writer;

  public JdbcWriterResourceHolder(final JdbcWriterImpl writer) {
    this.writer = writer;
  }

  protected void close() {
    if (writer != null) {
      writer.close();
      writer = null;
    }
  }

  public JdbcWriterImpl getWriter() {
    return writer;
  }

  public boolean hasWriter() {
    return writer != null;
  }

  @Override
  public void released() {
    super.released();
    if (!isOpen()) {
      close();
    }
  }

  public void setWriter(final JdbcWriterImpl writer) {
    this.writer = writer;
  }

  public boolean writerEquals(final JdbcWriterImpl writer) {
    return this.writer == writer;
  }
}
