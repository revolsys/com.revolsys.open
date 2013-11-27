package com.revolsys.io.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.AbstractReader;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;

public class JsonMapReader extends AbstractReader<Map<String, Object>>
  implements Reader<Map<String, Object>> {

  private final java.io.Reader in;

  private Iterator<Map<String, Object>> iterator;

  private boolean single = false;

  public JsonMapReader(final InputStream in) {
    this.in = FileUtil.createUtf8Reader(in);
  }

  public JsonMapReader(final java.io.Reader in) {
    this.in = in;
  }

  public JsonMapReader(final java.io.Reader in, final boolean single) {
    this.in = in;
    this.single = single;
  }

  @Override
  public void close() {
    FileUtil.closeSilent(in);
  }

  @Override
  public Iterator<Map<String, Object>> iterator() {
    if (iterator == null) {
      try {
        iterator = new JsonMapIterator(in, single);
      } catch (final IOException e) {
        throw new IllegalArgumentException("Unable to create Iterator:"
          + e.getMessage(), e);
      }
    }
    return iterator;
  }

  @Override
  public void open() {
  }
}
