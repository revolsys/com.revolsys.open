package com.revolsys.format.shp;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.AbstractRecordAndGeometryIoFactory;
import com.revolsys.data.record.io.RecordIteratorReader;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.Writer;
import com.revolsys.spring.OutputStreamResource;

public class ShapefileIoFactory extends AbstractRecordAndGeometryIoFactory {
  public ShapefileIoFactory() {
    super(ShapefileConstants.DESCRIPTION, true, true);
    addMediaTypeAndFileExtension(ShapefileConstants.MIME_TYPE, ShapefileConstants.FILE_EXTENSION);
    setSingleFile(false);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource, final RecordFactory recordFactory) {
    try {
      final ShapefileIterator iterator = new ShapefileIterator(resource, recordFactory);
      return new RecordIteratorReader(iterator);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public Writer<Record> createRecordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    return new ShapefileRecordWriter(recordDefinition, resource);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream, final Charset charset) {
    return createRecordWriter(recordDefinition, new OutputStreamResource(baseName, outputStream));
  }

}
