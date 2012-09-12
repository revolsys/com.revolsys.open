package com.revolsys.io.xml;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Reader;
import com.revolsys.spring.SpringUtil;

public class XmlMapIoFactory extends AbstractMapReaderFactory implements
  MapWriterFactory {
  public static Map<String, Object> toMap(final Resource resource) {
    final XmlMapIterator iterator = new XmlMapIterator(resource, true);
    try {
      if (iterator.hasNext()) {
        return iterator.next();
      } else {
        return null;
      }
    } finally {
      iterator.close();
    }
  }

  public XmlMapIoFactory() {
    super("XML");
    addMediaTypeAndFileExtension("text/xml", "xml");
  }

  @Override
  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    final XmlMapIterator iterator = new XmlMapIterator(resource);
    final Reader<Map<String, Object>> reader = new IteratorReader<Map<String, Object>>(
      iterator);
    return reader;
  }

  @Override
  public MapWriter getWriter(final OutputStream out) {
    final Writer writer = new OutputStreamWriter(out);
    return getWriter(writer);
  }

  @Override
  public MapWriter getWriter(final Resource resource) {
    final Writer writer = SpringUtil.getWriter(resource);
    return getWriter(writer);
  }

  @Override
  public MapWriter getWriter(final Writer out) {
    return new XmlMapWriter(out);
  }

  @Override
  public boolean isCustomAttributionSupported() {
    return true;
  }

  @Override
  public boolean isGeometrySupported() {
    return true;
  }
}
