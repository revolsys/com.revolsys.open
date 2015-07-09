package com.revolsys.io;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

public interface MapReaderFactory extends FileIoFactory {
  static Reader<Map<String, Object>> mapReader(final Resource resource) {
    final MapReaderFactory readerFactory = mapReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final Reader<Map<String, Object>> reader = readerFactory.createMapReader(resource);
      return reader;
    }
  }

  static MapReaderFactory mapReaderFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final MapReaderFactory readerFactory = ioFactoryRegistry.getFactory(MapReaderFactory.class,
      resource);
    return readerFactory;
  }

  default Reader<Map<String, Object>> createMapReader(final Path path) {
    final PathResource resource = new PathResource(path);
    return createMapReader(resource);
  }

  Reader<Map<String, Object>> createMapReader(final Resource resource);
}
