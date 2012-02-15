package com.revolsys.io;

import java.io.OutputStream;
import java.io.Writer;

import org.springframework.core.io.Resource;

public interface MapWriterFactory extends IoFactory {
  MapWriter getWriter(final OutputStream out);

  MapWriter getWriter(final Resource resource);

  MapWriter getWriter(final Writer out);

  boolean isCustomAttributionSupported();

  boolean isGeometrySupported();
}
