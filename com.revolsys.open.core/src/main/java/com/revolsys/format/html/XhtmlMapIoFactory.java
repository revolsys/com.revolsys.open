package com.revolsys.format.html;

import java.io.Writer;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;

public class XhtmlMapIoFactory extends AbstractIoFactory implements MapWriterFactory {
  public XhtmlMapIoFactory() {
    super("XHMTL");
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  @Override
  public MapWriter createMapWriter(final Writer out) {
    return new XhtmlMapWriter(out);
  }
}
