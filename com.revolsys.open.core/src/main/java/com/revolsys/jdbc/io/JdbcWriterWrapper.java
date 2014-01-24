package com.revolsys.jdbc.io;

import com.revolsys.gis.data.model.DataObject;
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
    writer = null;
  }

  @Override
  public void flush() {
    if (writer != null) {
      writer.flush();
    }
  }

  @Override
  public void open() {

  }

  @Override
  public void write(final DataObject record) {
    if (writer != null) {
      writer.write(record);
    }
  }
}
