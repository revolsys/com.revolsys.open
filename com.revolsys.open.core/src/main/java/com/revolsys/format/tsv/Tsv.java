package com.revolsys.format.tsv;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.io.AbstractRecordIoFactory;
import com.revolsys.data.record.io.RecordIteratorReader;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.io.RecordWriter;
import com.revolsys.data.record.io.RecordWriterFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.format.csv.CsvMapWriter;
import com.revolsys.format.csv.CsvRecordIterator;
import com.revolsys.format.csv.CsvRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;

public class Tsv extends AbstractRecordIoFactory implements RecordWriterFactory, MapWriterFactory {
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
    super(Tsv.DESCRIPTION);
    addMediaTypeAndFileExtension(Tsv.MEDIA_TYPE, Tsv.FILE_EXTENSION);
  }

  @Override
  public MapWriter createMapWriter(final Writer out) {
    return new CsvMapWriter(out, Tsv.FIELD_SEPARATOR, true);
  }

  @Override
  public RecordReader createRecordReader(final Resource resource,
    final RecordFactory recordFactory) {
    final CsvRecordIterator iterator = new CsvRecordIterator(resource, recordFactory,
      Tsv.FIELD_SEPARATOR);
    return new RecordIteratorReader(iterator);
  }

  @Override
  public RecordWriter createRecordWriter(final RecordDefinition recordDefinition, final Path path) {
    return new CsvRecordWriter(recordDefinition, path, Tsv.FIELD_SEPARATOR, false, true);
  }

  @Override
  public RecordWriter createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);

    return new CsvRecordWriter(recordDefinition, writer, Tsv.FIELD_SEPARATOR, false, true);
  }
}
