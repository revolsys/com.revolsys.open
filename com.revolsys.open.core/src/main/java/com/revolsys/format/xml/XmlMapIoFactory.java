package com.revolsys.format.xml;

import java.io.Writer;
import java.util.Map;

import com.revolsys.spring.resource.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.map.IteratorMapReader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;

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
  public MapReader createMapReader(final Resource resource) {
    final XmlMapIterator iterator = new XmlMapIterator(resource);
    return new IteratorMapReader(iterator);
  }

  @Override
  public MapWriter createMapWriter(final Writer out) {
    return new XmlMapWriter(out);
  }
}
