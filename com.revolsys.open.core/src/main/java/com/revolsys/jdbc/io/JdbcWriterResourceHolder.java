package com.revolsys.jdbc.io;

import org.springframework.transaction.support.ResourceHolderSupport;

public class JdbcWriterResourceHolder extends ResourceHolderSupport {
  private JdbcWriter writer;

  public JdbcWriterResourceHolder(JdbcWriter writer) {
    this.writer = writer;
  }

  public JdbcWriter getWriter() {
    return writer;
  }

  public void setWriter(JdbcWriter writer) {
    this.writer = writer;
  }

  public boolean hasWriter() {
    return writer != null;
  }

  public boolean writerEquals(JdbcWriter writer) {
    if (hasWriter()) {
      return this.writer == writer || this.equals(writer);
    } else {
      return false;
    }
  }
  
  @Override
  public void released() {
    super.released();
    if (!isOpen() && writer != null) {
      writer.flush();
      writer = null;
    }
  }
}
