package com.revolsys.record.io.format.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IteratorReader;
import com.revolsys.io.Reader;
import com.revolsys.io.map.IteratorMapReader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.RecordFactory;
import com.revolsys.record.io.AbstractRecordIoFactory;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Exceptions;
import com.revolsys.util.WrappedException;

public class Csv extends AbstractRecordIoFactory implements RecordWriterFactory, MapWriterFactory {
  public static final char FIELD_SEPARATOR = ',';

  public static Reader<Map<String, Object>> mapReader(final File file) {
    try {
      final FileInputStream in = new FileInputStream(file);
      return mapReader(in);
    } catch (final FileNotFoundException e) {
      throw new WrappedException(e);
    }
  }

  public static Reader<Map<String, Object>> mapReader(final InputStream in) {
    final InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
    return mapReader(reader);
  }

  public static Reader<Map<String, Object>> mapReader(final java.io.Reader reader) {
    try {
      final CsvMapIterator iterator = new CsvMapIterator(reader);
      return new IteratorReader<>(iterator);
    } catch (final IOException e) {
      throw new WrappedException(e);
    }
  }

  public static Reader<Map<String, Object>> mapReader(final String string) {
    final StringReader reader = new StringReader(string);
    return mapReader(reader);
  }

  public static CsvWriter plainWriter(final File file) {
    if (file == null) {
      throw new NullPointerException("File must not be null");
    } else {
      final java.io.Writer writer = FileUtil.createUtf8Writer(file);
      return plainWriter(writer);
    }
  }

  public static CsvWriter plainWriter(final java.io.Writer writer) {
    return new CsvWriter(writer);
  }

  /**
   * Convert a to a CSV string with a header row and a data row.
   *
   * @param map The to convert to CSV
   * @return The CSV string.
   */
  public static String toCsv(final Map<String, ? extends Object> map) {
    final StringWriter csvString = new StringWriter();
    final CsvMapWriter csvMapWriter = new CsvMapWriter(csvString);
    csvMapWriter.write(map);
    return csvString.toString();
  }

  public static Map<String, String> toMap(final String businessApplicationParameters) {
    final HashMap<String, String> map = new LinkedHashMap<String, String>();
    final CsvIterator iterator = new CsvIterator(new StringReader(businessApplicationParameters));
    if (iterator.hasNext()) {
      final List<String> keys = iterator.next();
      if (iterator.hasNext()) {
        final List<String> values = iterator.next();
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
          map.put(keys.get(i), values.get(i));
        }
      }
    }
    return map;
  }

  public static Map<String, Object> toObjectMap(final String businessApplicationParameters) {
    final HashMap<String, Object> map = new LinkedHashMap<>();
    final CsvIterator iterator = new CsvIterator(new StringReader(businessApplicationParameters));
    if (iterator.hasNext()) {
      final List<String> keys = iterator.next();
      if (iterator.hasNext()) {
        final List<String> values = iterator.next();
        for (int i = 0; i < keys.size() && i < values.size(); i++) {
          map.put(keys.get(i), values.get(i));
        }
      }
    }
    return map;
  }

  /*
   * Replaces whitespace with spaces
   */
  public static void writeColumns(final StringWriter out,
    final Collection<? extends Object> columns, final char fieldSeparator,
    final char recordSeparator) {
    boolean first = true;
    for (final Object value : columns) {
      if (first) {
        first = false;
      } else {
        out.write(fieldSeparator);
      }
      if (value != null) {
        String text = StringConverterRegistry.toString(value);
        text = text.replaceAll("\\s", " ");

        out.write(text);
      }
    }
    out.write(recordSeparator);
  }

  public Csv() {
    super("Comma-Separated Values");
    addMediaTypeAndFileExtension("text/csv", "csv");
  }

  @Override
  public MapReader newMapreader(final Resource resource) {
    try {
      final CsvMapIterator iterator = new CsvMapIterator(resource.newReader());
      return new IteratorMapReader(iterator);
    } catch (final IOException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  public MapWriter newMapWriter(final java.io.Writer out) {
    return new CsvMapWriter(out);
  }

  @Override
  public RecordReader newRecordReader(final Resource resource, final RecordFactory recordFactory) {
    return new CsvRecordReader(resource, recordFactory);
  }

  @Override
  public RecordWriter newRecordWriter(final RecordDefinition recordDefinition, final Path path) {
    return new CsvRecordWriter(recordDefinition, path, Csv.FIELD_SEPARATOR, true, true);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);

    return new CsvRecordWriter(recordDefinition, writer, Csv.FIELD_SEPARATOR, true, true);
  }
}
