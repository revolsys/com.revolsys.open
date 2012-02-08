package com.revolsys.io;

import java.io.OutputStream;
import java.io.Writer;

import org.springframework.core.io.Resource;

public interface MapWriterFactory extends IoFactory {
  MapWriter getWriter(final Writer out);

  MapWriter getWriter(final OutputStream out);

  MapWriter getWriter(final Resource resource);

  boolean isGeometrySupported();

  boolean isCustomAttributionSupported();
}
