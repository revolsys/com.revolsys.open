package com.revolsys.record.io;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreRecordWriter extends AbstractRecordWriter {

  private final RecordStore recordStore;

  private final Writer<Record> writer;

  public RecordStoreRecordWriter(final RecordStore recordStore) {
    this.recordStore = recordStore;
    this.writer = recordStore.createWriter();
  }

  @Override
  public void close() {
    try {
      this.writer.close();
    } finally {
      this.recordStore.close();
    }
  }

  @Override
  public void flush() {
    this.writer.flush();
  }

  @Override
  public void write(final Record record) {
    if (this.writer != null) {
      this.writer.write(record);
    }
  }
}
