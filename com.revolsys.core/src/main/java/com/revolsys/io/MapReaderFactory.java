package com.revolsys.io;

import java.io.Reader;

public interface MapReaderFactory extends IoFactory {
  MapReader getReader(
    final Reader in);
}
