package com.revolsys.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.AbstractReader;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;

public class JsonMapReader extends AbstractReader<Map<String, Object>>
  implements Reader<Map<String, Object>> {

  private final java.io.Reader in;

  private Iterator<Map<String, Object>> iterator;

  public JsonMapReader(final InputStream in) {
    this.in = new InputStreamReader(in, Charset.forName("UTF-8"));
  }

  public JsonMapReader(final java.io.Reader in) {
    this.in = in;
  }

  public void close() {
    FileUtil.closeSilent(in);
  }

  public Iterator<Map<String, Object>> iterator() {
    if (iterator == null) {
      try {
        iterator = new JsonMapIterator(in);
      } catch (final IOException e) {
        throw new IllegalArgumentException("Unable to create Iterator:"
          + e.getMessage(), e);
      }
    }
    return iterator;
  }

  public void open() {
  }
}
