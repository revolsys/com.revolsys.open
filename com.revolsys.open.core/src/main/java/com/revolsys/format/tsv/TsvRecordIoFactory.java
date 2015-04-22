package com.revolsys.format.tsv;

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
import com.revolsys.format.csv.CsvRecordIterator;
import com.revolsys.format.csv.CsvRecordWriter;
import com.revolsys.io.Writer;

public class TsvRecordIoFactory extends AbstractRecordAndGeometryIoFactory {
  public TsvRecordIoFactory() {
    super(TsvConstants.DESCRIPTION, false, true);
    addMediaTypeAndFileExtension(TsvConstants.MEDIA_TYPE,
      TsvConstants.FILE_EXTENSION);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    final CsvRecordIterator iterator = new CsvRecordIterator(resource,
      recordFactory, TsvConstants.FIELD_SEPARATOR);
    return new RecordIteratorReader(iterator);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream,
      charset);

    return new CsvRecordWriter(recordDefinition, writer,
      TsvConstants.FIELD_SEPARATOR, true, true);
  }
}
