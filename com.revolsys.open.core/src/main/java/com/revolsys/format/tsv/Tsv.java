package com.revolsys.format.tsv;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryIoFactory;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.format.csv.CsvMapWriter;
import com.revolsys.format.csv.CsvRecordIterator;
import com.revolsys.format.csv.CsvRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.spring.SpringUtil;

public class Tsv extends AbstractRecordAndGeometryIoFactory implements MapWriterFactory {
  public static final String DESCRIPTION = "Tab-Separated Values";

  public static final char FIELD_SEPARATOR = '\t';

  public static final String FILE_EXTENSION = "tsv";

  public static final String MEDIA_TYPE = "text/tab-separated-values";

  public static final char QUOTE_CHARACTER = '"';

  public static TsvWriter plainWriter(final File file) {
    if (file == null) {
      throw new NullPointerException("File must not be null");
    } else {
      final Writer writer = FileUtil.createUtf8Writer(file);
      return plainWriter(writer);
    }
  }

  public static TsvWriter plainWriter(final Writer writer) {
    return new TsvWriter(writer);
  }

  public Tsv() {
    super(Tsv.DESCRIPTION, false, true);
    addMediaTypeAndFileExtension(Tsv.MEDIA_TYPE, Tsv.FILE_EXTENSION);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource, final RecordFactory recordFactory) {
    final CsvRecordIterator iterator = new CsvRecordIterator(resource, recordFactory,
      Tsv.FIELD_SEPARATOR);
    return new RecordIteratorReader(iterator);
  }

  @Override
  public com.revolsys.io.Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);

    return new CsvRecordWriter(recordDefinition, writer, Tsv.FIELD_SEPARATOR, true, true);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out) {
    final Writer writer = FileUtil.createUtf8Writer(out);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(out, charset);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final Resource resource) {
    final Writer writer = SpringUtil.getWriter(resource);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final Writer out) {
    return new CsvMapWriter(out, Tsv.FIELD_SEPARATOR, true);
  }

  @Override
  public boolean isCustomAttributionSupported() {
    return true;
  }

  @Override
  public boolean isGeometrySupported() {
    return true;
  }
}
