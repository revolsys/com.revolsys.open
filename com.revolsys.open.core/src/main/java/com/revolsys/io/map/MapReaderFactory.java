package com.revolsys.io.map;

import java.nio.file.Path;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.IoFactoryRegistry;

public interface MapReaderFactory extends FileIoFactory {
  static MapReaderFactory mapReaderFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final MapReaderFactory readerFactory = ioFactoryRegistry.getFactory(MapReaderFactory.class,
      resource);
    return readerFactory;
  }

  default MapReader createMapReader(final Path path) {
    final PathResource resource = new PathResource(path);
    return createMapReader(resource);
  }

  MapReader createMapReader(final Resource resource);
}
