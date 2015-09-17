package com.revolsys.jdbc.io;

import com.revolsys.properties.DelegatingObjectWithProperties;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;

public class JdbcWriterWrapper extends DelegatingObjectWithProperties implements RecordWriter {
  private RecordWriter writer;

  public JdbcWriterWrapper(final RecordWriter writer) {
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
