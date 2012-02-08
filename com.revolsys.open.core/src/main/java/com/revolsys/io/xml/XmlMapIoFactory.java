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

  public Reader<Map<String, Object>> createMapReader(Resource resource) {
    XmlMapIterator iterator = new XmlMapIterator(resource);
    Reader<Map<String, Object>> reader = new IteratorReader<Map<String, Object>>(
      iterator);
    return reader;
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
    return new XmlMapWriter(out);
  }

  public boolean isCustomAttributionSupported() {
    return true;
  }

  public boolean isGeometrySupported() {
    return true;
  }
}
