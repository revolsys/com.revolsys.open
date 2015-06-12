package com.revolsys.io;

import java.util.Map;

import org.springframework.core.io.Resource;

public abstract class AbstractMapReaderFactory extends AbstractIoFactory implements
  MapReaderFactory {
  public static MapReaderFactory getMapReaderFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final MapReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      MapReaderFactory.class, resource);
    return readerFactory;
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

  public AbstractMapReaderFactory(final String name) {
    super(name);
  }
}
