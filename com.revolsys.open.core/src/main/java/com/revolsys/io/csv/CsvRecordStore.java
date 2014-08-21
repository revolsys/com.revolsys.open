package com.revolsys.io.csv;

import java.io.File;

import com.revolsys.data.query.Query;
import com.revolsys.data.record.ArrayRecord;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.AbstractRecordStore;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.record.schema.RecordStoreSchema;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class CsvRecordStore extends AbstractRecordStore {

  private final File directory;

  private CsvDirectoryWriter writer;

  public CsvRecordStore(final File directory) {
    this.directory = directory;
    directory.mkdirs();
    this.writer = new CsvDirectoryWriter(directory);
  }

  @Override
  public void close() {
    super.close();
    FileUtil.closeSilent(this.writer);
    this.writer = null;
  }

  @Override
  public Record create(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final RecordDefinition savedRecordDefinition = getRecordDefinition(typePath);
    if (savedRecordDefinition == null) {
      return new ArrayRecord(recordDefinition);
    } else {
      return new ArrayRecord(savedRecordDefinition);
    }
  }

  @Override
  public Writer<Record> createWriter() {
    return this.writer;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return this.writer.getRecordDefinition(typePath);
  }

  @Override
  public int getRowCount(final Query query) {
    return 0;
  }

  @Override
  public void insert(final Record record) {
    this.writer.write(record);
  }

  @Override
  protected void refreshSchema(final RecordStoreSchema schema) {
  }

  @Override
  public String toString() {
    return this.directory.toString();
  }

}
