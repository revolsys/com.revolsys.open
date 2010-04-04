package com.revolsys.xhtml;

import java.io.Writer;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;

public class XhtmlMapIoFactory extends AbstractIoFactory implements
  MapWriterFactory {
  public XhtmlMapIoFactory() {
    super("XHMTL");
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  public MapWriter getWriter(
    final Writer out) {
    return new XhtmlMapWriter(out);
  }
}
