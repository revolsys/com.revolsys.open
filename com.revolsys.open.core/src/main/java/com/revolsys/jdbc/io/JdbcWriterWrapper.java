package com.revolsys.jdbc.io;

import com.revolsys.data.record.Record;
import com.revolsys.io.DelegatingObjectWithProperties;

public class JdbcWriterWrapper extends DelegatingObjectWithProperties implements
JdbcWriter {
  private JdbcWriter writer;

  public JdbcWriterWrapper(final JdbcWriter writer) {
    super(writer);
    this.writer = writer;
  }

  @Override
  public void close() throws RuntimeException {
    flush();
    setObject(null);
    this.writer = null;
  }

  @Override
  public void flush() {
    if (this.writer != null) {
      this.writer.flush();
    }
  }

  @Override
  public void open() {

  }

  @Override
  public void write(final Record record) {
    if (this.writer != null) {
      this.writer.write(record);
    }
  }
}
