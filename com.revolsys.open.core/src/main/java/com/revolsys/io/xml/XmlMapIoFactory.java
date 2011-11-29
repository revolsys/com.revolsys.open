package com.revolsys.io.xml;

import java.io.OutputStreamWriter;
import java.io.Writer;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import java.io.OutputStream;

public class XmlMapIoFactory extends AbstractIoFactory implements
  MapWriterFactory {
  public XmlMapIoFactory() {
    super("XML");
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  public MapWriter getWriter(OutputStream out) {
    Writer writer = new OutputStreamWriter(out);
    return getWriter(writer);
  }

  public MapWriter getWriter(final Writer out) {
    return new XmlMapWriter(out);
  }

  public boolean isCustomAttributionSupported() {
    return true;
  }

  public boolean isGeometrySupported() {
    return true;
  }
}
