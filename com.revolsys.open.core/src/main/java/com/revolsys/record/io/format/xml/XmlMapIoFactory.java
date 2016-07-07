package com.revolsys.record.io.format.xml;

import java.io.Writer;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.map.IteratorMapReader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.spring.resource.Resource;

public class XmlMapIoFactory extends AbstractIoFactory
  implements MapReaderFactory, MapWriterFactory {
  public static MapEx toMap(final Resource resource) {
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
  public MapReader newMapReader(final Resource resource) {
    final XmlMapIterator iterator = new XmlMapIterator(resource);
    return new IteratorMapReader(iterator);
  }

  @Override
  public MapWriter newMapWriter(final Writer out) {
    return new XmlMapWriter(out);
  }
}
