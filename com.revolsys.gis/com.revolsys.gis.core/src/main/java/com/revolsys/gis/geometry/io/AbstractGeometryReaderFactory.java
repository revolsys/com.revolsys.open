package com.revolsys.gis.geometry.io;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.IoFactoryRegistry;

public abstract class AbstractGeometryReaderFactory extends AbstractIoFactory
  implements GeometryReaderFactory {

  public static GeometryReader createReader(
    Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;
    final GeometryReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      GeometryReaderFactory.class, resource);
    if (readerFactory == null) {
      return null;
    } else {
      final GeometryReader reader = readerFactory.createGeometryReader(resource);
      return reader;
    }
  }

  public AbstractGeometryReaderFactory(
    final String name) {
    super(name);
  }
}
