package com.revolsys.data.record.io;

import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.map.MapReader;

public class RecordMapReader implements MapReader {

  private final RecordReader reader;

  public RecordMapReader(final RecordReader reader) {
    this.reader = reader;
  }

  @Override
  public void close() {
    this.reader.close();
  }

  @Override
  public Map<String, Object> getProperties() {
    return this.reader.getProperties();
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public Iterator<Map<String, Object>> iterator() {
    return (Iterator)this.reader.iterator();
  }

  @Override
  public void open() {
    this.reader.open();
  }

}
