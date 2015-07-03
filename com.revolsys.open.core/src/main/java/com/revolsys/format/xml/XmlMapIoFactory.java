package com.revolsys.format.xml;

import java.io.Writer;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.IteratorReader;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.Reader;

public class XmlMapIoFactory extends AbstractIoFactory
  implements MapReaderFactory, MapWriterFactory {
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
    final Reader<Map<String, Object>> reader = new IteratorReader<Map<String, Object>>(iterator);
    return reader;
  }

  @Override
  public MapWriter createMapWriter(final Writer out) {
    return new XmlMapWriter(out);
  }
}
