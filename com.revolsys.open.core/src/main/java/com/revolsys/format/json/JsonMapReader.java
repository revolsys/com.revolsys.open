package com.revolsys.format.json;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.AbstractReader;
import com.revolsys.io.FileUtil;
import com.revolsys.io.Reader;

public class JsonMapReader extends AbstractReader<Map<String, Object>> implements
  Reader<Map<String, Object>> {

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
    FileUtil.closeSilent(this.in);
  }

  @Override
  public Iterator<Map<String, Object>> iterator() {
    if (this.iterator == null) {
      try {
        this.iterator = new JsonMapIterator(this.in, this.single);
      } catch (final IOException e) {
        throw new IllegalArgumentException("Unable to create Iterator:" + e.getMessage(), e);
      }
    }
    return this.iterator;
  }

  @Override
  public void open() {
  }
}
