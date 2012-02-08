package com.revolsys.io.html;

import java.io.OutputStreamWriter;
import java.io.Writer;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.spring.SpringUtil;

import java.io.OutputStream;

import org.springframework.core.io.Resource;

public class XhtmlMapIoFactory extends AbstractIoFactory implements
  MapWriterFactory {
  public XhtmlMapIoFactory() {
    super("XHMTL");
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  public MapWriter getWriter(Resource resource) {
    Writer writer = SpringUtil.getWriter(resource);
    return getWriter(writer);
  }

  public MapWriter getWriter(OutputStream out) {
    Writer writer = new OutputStreamWriter(out);
    return getWriter(writer);
  }

  public MapWriter getWriter(final Writer out) {
    return new XhtmlMapWriter(out);
  }

  public boolean isCustomAttributionSupported() {
    return true;
  }

  public boolean isGeometrySupported() {
    return true;
  }

}
