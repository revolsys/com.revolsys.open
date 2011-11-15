package com.revolsys.io;

import java.util.Map;

import org.springframework.core.io.Resource;

public abstract class AbstractMapReaderFactory extends AbstractIoFactory
  implements MapReaderFactory {
  public AbstractMapReaderFactory(String name) {
    super(name);
  }

  public static Reader<Map<String, Object>> mapReader(final Resource resource) {
    final MapReaderFactory readerFactory = getMapReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final Reader<Map<String, Object>> reader = readerFactory.createMapReader(resource);
      return reader;
    }
  }

  public static MapReaderFactory getMapReaderFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;
    final MapReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      MapReaderFactory.class, resource);
    return readerFactory;
  }
}
