package com.revolsys.io.map;

import java.util.Map;

import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;

public interface MapReader extends Reader<Map<String, Object>> {
  static boolean isReadable(final Object source) {
    return IoFactory.isAvailable(MapReaderFactory.class, source);
  }

  static MapReader newMapReader(final Object source) {
    final MapReaderFactory factory = IoFactory.factory(MapReaderFactory.class, source);
    if (factory == null) {
      return null;
    } else {
      return factory.newMapreader(source);
    }
  }
}
