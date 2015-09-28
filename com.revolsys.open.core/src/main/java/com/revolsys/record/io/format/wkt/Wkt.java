package com.revolsys.record.io.format.wkt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.revolsys.io.FileUtil;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;

public class Wkt extends AbstractRecordIoFactory implements RecordWriterFactory, WktConstants {
  public Wkt() {
    super(WktConstants.DESCRIPTION);
    addMediaTypeAndFileExtension(MEDIA_TYPE, FILE_EXTENSION);
  }

  @Override
  public boolean isCustomFieldsSupported() {
    return false;
  }

  @Override
  public RecordReader newRecordReader(final Resource resource, final RecordFactory factory) {
    try {
      return new WktRecordIterator(factory, resource);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to create reader for " + resource, e);
    }
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new WktRecordWriter(recordDefinition, writer);
  }
}
