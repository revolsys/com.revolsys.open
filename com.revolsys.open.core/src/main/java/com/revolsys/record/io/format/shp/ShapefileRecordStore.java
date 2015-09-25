package com.revolsys.record.io.format.shp;

import java.io.File;

import com.revolsys.io.FileUtil;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.AbstractRecordStore;
import com.revolsys.record.schema.RecordDefinition;

public class ShapefileRecordStore extends AbstractRecordStore {

  private ShapefileDirectoryWriter writer;

  public ShapefileRecordStore(final File directory) {
    directory.mkdirs();
    this.writer = new ShapefileDirectoryWriter(directory);
    this.writer.setLogCounts(false);
  }

  @Override
  public void close() {
    super.close();
    FileUtil.closeSilent(this.writer);
    this.writer = null;
  }

  @Override
  public Record newRecord(final RecordDefinition recordDefinition) {
    final String typePath = recordDefinition.getPath();
    final RecordDefinition savedRecordDefinition = getRecordDefinition(typePath);
    if (savedRecordDefinition == null) {
      return new ArrayRecord(recordDefinition);
    } else {
      return new ArrayRecord(savedRecordDefinition);
    }
  }

  @Override
  public RecordWriter createWriter() {
    return this.writer;
  }

  @Override
  public RecordDefinition getRecordDefinition(final String typePath) {
    return this.writer.getRecordDefinition(typePath);
  }

  @Override
  public int getRowCount(final Query query) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void insert(final Record record) {
    this.writer.write(record);
  }

}
