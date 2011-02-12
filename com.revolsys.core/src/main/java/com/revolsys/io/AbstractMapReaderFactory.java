package com.revolsys.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public abstract class AbstractMapReaderFactory extends AbstractIoFactory
  implements MapReaderFactory {
  public AbstractMapReaderFactory(
    String name) {
    super(name);
  }

  public static MapReader mapReader(
    final Resource resource) {
    final MapReaderFactory readerFactory = getMapReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final MapReader reader = readerFactory.createMapReader(resource);
      return reader;
    }
  }

  public static MapReaderFactory getMapReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;
    final MapReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      MapReaderFactory.class, resource);
    return readerFactory;
  }

  public MapReader createMapReader(
    final Resource resource) {
    try {
      return createMapReader(resource.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException("Unable to get input stream", e);
    }
  }

  public MapReader createMapReader(
    final InputStream in) {
    return createMapReader(new InputStreamReader(in));
  }
}
