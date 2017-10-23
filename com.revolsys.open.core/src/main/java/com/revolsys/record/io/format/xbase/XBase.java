package com.revolsys.record.io.format.xbase;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.OutputStreamResource;
import com.revolsys.spring.resource.Resource;

public class XBase extends AbstractIoFactoryWithCoordinateSystem
  implements RecordReaderFactory, RecordWriterFactory {
  public XBase() {
    super("D-Base");
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  @Override
  public boolean isBinary() {
    return true;
  }

  @Override
  public boolean isGeometrySupported() {
    return false;
  }

  @Override
  public boolean isReadFromZipFileSupported() {
    return true;
  }

  @Override
  public RecordReader newRecordReader(final Resource resource,
    final RecordFactory<? extends Record> recordFactory, Map<String, ? extends Object> properties) {
    try {
      return new XbaseRecordReader(resource, recordFactory);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    return new XbaseRecordWriter(recordDefinition, resource);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    return newRecordWriter(recordDefinition, new OutputStreamResource(baseName, outputStream));
  }
}
