package com.revolsys.format.gpx;

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

public class Gpx extends AbstractRecordIoFactory implements RecordWriterFactory {
  public Gpx() {
    super("GPS Exchange Format");
    addMediaTypeAndFileExtension(GpxConstants.MEDIA_TYPE, GpxConstants.FILE_EXTENSION);
  }

  public RecordReader createRecordReader(final RecordDefinition recordDefinition,
    final Resource resource, final RecordFactory factory) {
    throw new UnsupportedOperationException();
  }

  /**
   * Create a reader for the file using the specified data object factory.
   *
   * @param inputStream The file to read.
   * @param factory The factory used to create data objects.
   * @return The reader for the file.
   */
  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    try {
      return new GpxIterator(resource, recordFactory, null);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource, e);
    }
  }

  @Override
  public RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new GpxWriter(writer);
  }

  @Override
  public boolean isCustomFieldsSupported() {
    return false;
  }
}
