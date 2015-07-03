package com.revolsys.format.xbase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.RecordIteratorReader;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.io.RecordReaderFactory;
import com.revolsys.data.record.io.RecordWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;

public class XBaseRecordIoFactory extends AbstractIoFactoryWithCoordinateSystem
  implements RecordReaderFactory, RecordWriterFactory {
  public XBaseRecordIoFactory() {
    super("D-Base");
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    try {
      final XbaseIterator iterator = new XbaseIterator(resource, recordFactory);

      return new RecordIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public Writer<Record> createRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    return new XbaseRecordWriter(recordDefinition, resource);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return createRecordWriter(recordDefinition, new OutputStreamResource(baseName, outputStream));
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isGeometrySupported() {
    return false;
  }
}
