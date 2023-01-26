package com.revolsys.record.io;

import java.util.function.Supplier;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinitionProxy;

public class LazyRecordWriter extends AbstractRecordWriter {

  private RecordWriter writer;

  private final Supplier<RecordWriter> supplier;

  public LazyRecordWriter(final RecordDefinitionProxy recordDefinition,
    final Supplier<RecordWriter> supplier) {
    super(recordDefinition);
    this.supplier = supplier;
  }

  @Override
  public void close() {
    if (this.writer != null) {
      this.writer.close();
    }
  }

  @Override
  public void write(final Record object) {
    if (this.writer == null) {
      this.writer = this.supplier.get();
    }
    this.writer.write(object);
  }
}
