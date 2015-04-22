package com.revolsys.format.gpx;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryIoFactory;
import com.revolsys.data.io.RecordIterator;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Writer;

public class GpxReaderFactory extends AbstractRecordAndGeometryIoFactory {
  public GpxReaderFactory() {
    super("GPS Exchange Format", false, false);
    addMediaTypeAndFileExtension(GpxConstants.MEDIA_TYPE,
      GpxConstants.FILE_EXTENSION);
    setCustomAttributionSupported(false);
  }

  public RecordReader createRecordReader(
    final RecordDefinition recordDefinition, final Resource resource,
    final RecordFactory factory) {
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
      final RecordIterator iterator = new GpxIterator(resource, recordFactory,
        null);
      return new RecordIteratorReader(iterator);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to open resource " + resource,
        e);
    }
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.createUtf8Writer(outputStream);
    return new GpxWriter(writer);
  }
}
