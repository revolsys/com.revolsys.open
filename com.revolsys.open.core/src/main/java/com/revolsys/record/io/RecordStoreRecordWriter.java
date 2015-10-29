package com.revolsys.record.io;

import java.util.Map;

import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.record.Record;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;

public class RecordStoreRecordWriter extends AbstractRecordWriter {
  private final RecordStore recordStore;

  private final RecordWriter writer;

  private final RecordDefinition recordDefinition;

  public RecordStoreRecordWriter(final RecordStore recordStore,
    final RecordDefinition recordDefinition) {
    this.recordStore = recordStore;
    this.recordDefinition = recordStore.getRecordDefinition(recordDefinition);
    if (this.recordDefinition == null) {
      throw new IllegalArgumentException(
        "Cannot find recordDefinition=" + recordDefinition.getPathName() + " for " + recordStore);
    }
    this.writer = recordStore.newRecordWriter(recordDefinition);

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
  public RecordDefinition getRecordDefinition() {
    return this.recordDefinition;
  }

  @Override
  public Record newRecord() {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return this.recordStore.newRecord(recordDefinition);
  }

  @Override
  public Record newRecord(final Map<String, ? extends Object> values) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return this.recordStore.newRecord(recordDefinition, values);
  }

  @Override
  public void write(final Record record) {
    if (this.writer != null) {
      this.writer.write(record);
    }
  }
}
