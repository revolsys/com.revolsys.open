package com.revolsys.csv;

import java.io.Reader;
import java.io.Writer;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapReader;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;

public class CsvMapIoFactory extends AbstractIoFactory implements
  MapReaderFactory, MapWriterFactory {
  public CsvMapIoFactory() {
    super("Comma Seprated Variable");
    addMediaTypeAndFileExtension(CsvConstants.MEDIA_TYPE,
      CsvConstants.FILE_EXTENSION);
  }

  public MapReader getReader(
    final Reader in) {
    return new CsvMapReader(in);
  }

  public MapWriter getWriter(
    final Writer out) {
    return new CsvMapWriter(out);
  }
}
