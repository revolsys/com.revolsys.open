package com.revolsys.io;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

public interface MapWriterFactory extends IoFactory {
  MapWriter getMapWriter(final OutputStream out);

  MapWriter getMapWriter(OutputStream out, Charset charset);

  MapWriter getMapWriter(final Resource resource);

  MapWriter getMapWriter(final Writer out);

  boolean isCustomAttributionSupported();

  boolean isGeometrySupported();
}
