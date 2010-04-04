package com.revolsys.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.FileUtil;
import com.revolsys.io.MapReader;

public class CsvMapReader implements MapReader {

  private final Reader in;

  private Iterator<Map<String, Object>> iterator;

  public CsvMapReader(
    final InputStream in) {
    this.in = new InputStreamReader(in, CsvConstants.CHARACTER_SET);
  }

  public CsvMapReader(
    final Reader in) {
    this.in = in;
  }

  public void close() {
    FileUtil.closeSilent(in);
  }

  public Iterator<Map<String, Object>> iterator() {
    if (iterator == null) {
      try {
        iterator = new CsvMapIterator(in);
      } catch (final IOException e) {
        throw new IllegalArgumentException("Unable to create Iterator:"
          + e.getMessage(), e);
      }
    }
    return iterator;
  }
}
