package com.revolsys.io;

import java.io.Writer;

public interface MapWriterFactory extends IoFactory {
  MapWriter getWriter(
    final Writer out);
}
