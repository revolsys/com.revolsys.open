package com.revolsys.io.map;

import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.IteratorReader;

public class IteratorMapReader extends IteratorReader<Map<String, Object>>implements MapReader {

  public IteratorMapReader(final Iterator<Map<String, Object>> iterator) {
    super(iterator);
  }

}
