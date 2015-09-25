package com.revolsys.record.io.format.zip;

import java.io.File;
import java.io.OutputStream;

import com.revolsys.io.ZipWriter;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.schema.RecordDefinition;

public class ZipRecordWriter extends ZipWriter<Record>implements RecordWriter {

  public ZipRecordWriter(final File tempDirectory, final RecordWriter writer,
    final OutputStream out) {
    super(tempDirectory, writer, out);
  }

  @Override
  public RecordDefinition getRecordDefinition() {
    final RecordWriter writer = getWriter();
    return writer.getRecordDefinition();
  }

  @Override
  public RecordWriter getWriter() {
    return (RecordWriter)super.getWriter();
  }

}
