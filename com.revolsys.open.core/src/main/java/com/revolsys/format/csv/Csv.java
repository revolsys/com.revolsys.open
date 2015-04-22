package com.revolsys.format.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.AbstractRecordAndGeometryIoFactory;
import com.revolsys.data.io.IteratorReader;
import com.revolsys.data.io.RecordIteratorReader;
import com.revolsys.data.io.RecordReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.WrappedException;

public class Csv extends AbstractRecordAndGeometryIoFactory implements MapWriterFactory {

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

  public Csv() {
    super(CsvConstants.DESCRIPTION, false, true);
    addMediaTypeAndFileExtension(CsvConstants.MEDIA_TYPE, CsvConstants.FILE_EXTENSION);
  }

  @Override
  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    try {
      final CsvMapIterator iterator = new CsvMapIterator(SpringUtil.getReader(resource));
      return new IteratorReader<>(iterator);
    } catch (final IOException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public RecordReader createRecordReader(final Resource resource, final RecordFactory recordFactory) {
    final CsvRecordIterator iterator = new CsvRecordIterator(resource, recordFactory);
    return new RecordIteratorReader(iterator);
  }

  @Override
  public Writer<Record> createRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(outputStream, charset);

    return new CsvRecordWriter(recordDefinition, writer, true);
  }

  @Override
  public MapWriter getMapWriter(final java.io.Writer out) {
    return new CsvMapWriter(out);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out) {
    final java.io.Writer writer = FileUtil.createUtf8Writer(out);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(out, charset);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final Resource resource) {
    final java.io.Writer writer = SpringUtil.getWriter(resource);
    return getMapWriter(writer);
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
