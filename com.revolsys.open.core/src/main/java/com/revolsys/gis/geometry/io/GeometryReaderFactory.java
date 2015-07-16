package com.revolsys.gis.geometry.io;

import java.nio.file.Path;

import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.io.FileIoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.spring.PathResource;

public interface GeometryReaderFactory extends FileIoFactory, IoFactoryWithCoordinateSystem {
  static GeometryReader geometryReader(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeometryReaderFactory readerFactory = ioFactoryRegistry
      .getFactory(GeometryReaderFactory.class, resource);
    if (readerFactory == null) {
      return null;
    } else {
      final GeometryReader reader = readerFactory.createGeometryReader(resource);
      return reader;
    }
  }

  /**
   * Create a reader for the path using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  default GeometryReader createGeometryReader(final Path path) {
    final PathResource resource = new PathResource(path);
    return createGeometryReader(resource);
  }

  GeometryReader createGeometryReader(final Resource resource);

}
