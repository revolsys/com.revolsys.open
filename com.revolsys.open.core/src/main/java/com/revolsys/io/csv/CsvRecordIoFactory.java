package com.revolsys.io.csv;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryIoFactory;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;

public class CsvRecordIoFactory extends AbstractRecordAndGeometryIoFactory {
  public CsvRecordIoFactory() {
    super(CsvConstants.DESCRIPTION, false, true);
    addMediaTypeAndFileExtension(CsvConstants.MEDIA_TYPE,
      CsvConstants.FILE_EXTENSION);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    final CsvRecordIterator iterator = new CsvRecordIterator(resource,
      recordFactory);
    return new RecordIteratorReader(iterator);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);

    return new CsvRecordWriter(recordDefinition, writer, true);
  }

}
