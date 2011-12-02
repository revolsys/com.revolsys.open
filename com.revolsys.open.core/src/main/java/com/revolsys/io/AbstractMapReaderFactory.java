package com.revolsys.io;

import java.util.Map;

import org.springframework.core.io.Resource;

public abstract class AbstractMapReaderFactory extends AbstractIoFactory
  implements MapReaderFactory {
  private boolean singleFile = true;

  protected boolean customAttributionSupported = true;

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

  public boolean isCustomAttributionSupported() {
    return customAttributionSupported;
  }

  protected void setCustomAttributionSupported(
    boolean customAttributionSupported) {
    this.customAttributionSupported = customAttributionSupported;
  }

  public boolean isSingleFile() {
    return singleFile;
  }

  protected void setSingleFile(final boolean singleFile) {
    this.singleFile = singleFile;
  }
}
