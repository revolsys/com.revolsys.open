package com.revolsys.data.record.io;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordStore;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.Writer;

public class RecordStoreRecordWriter extends AbstractWriter<Record> {

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
