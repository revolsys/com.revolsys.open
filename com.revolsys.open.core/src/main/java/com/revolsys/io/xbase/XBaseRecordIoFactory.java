package com.revolsys.io.xbase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordIoFactory;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;

public class XBaseRecordIoFactory extends AbstractRecordIoFactory {
  public XBaseRecordIoFactory() {
    super("D-Base", true, false, true);
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory dataObjectFactory) {
    try {
      final XbaseIterator iterator = new XbaseIterator(resource,
        dataObjectFactory);

      return new RecordIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public Writer<Record> createRecordWriter(
    final RecordDefinition metaData, final Resource resource) {
    return new XbaseRecordWriter(metaData, resource);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition metaData, final OutputStream outputStream,
    final Charset charset) {
    return createRecordWriter(metaData, new OutputStreamResource(baseName,
      outputStream));
  }

}
