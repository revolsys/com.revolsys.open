package com.revolsys.io;

import java.util.Map;

import org.springframework.core.io.Resource;

public abstract class AbstractMapReaderFactory extends AbstractIoFactory
  implements MapReaderFactory {
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

  private boolean singleFile = true;

  protected boolean customAttributionSupported = true;

  public AbstractMapReaderFactory(final String name) {
    super(name);
  }

  @Override
  public boolean isCustomAttributionSupported() {
    return customAttributionSupported;
  }

  @Override
  public boolean isSingleFile() {
    return singleFile;
  }

  protected void setCustomAttributionSupported(
    final boolean customAttributionSupported) {
    this.customAttributionSupported = customAttributionSupported;
  }

  protected void setSingleFile(final boolean singleFile) {
    this.singleFile = singleFile;
  }
}
