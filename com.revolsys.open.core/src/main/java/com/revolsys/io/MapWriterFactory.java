package com.revolsys.io;

import java.io.Writer;

import java.io.OutputStream;

public interface MapWriterFactory extends IoFactory {
  MapWriter getWriter(final Writer out);

  MapWriter getWriter(final OutputStream out);

  boolean isGeometrySupported();

  boolean isCustomAttributionSupported();
}
