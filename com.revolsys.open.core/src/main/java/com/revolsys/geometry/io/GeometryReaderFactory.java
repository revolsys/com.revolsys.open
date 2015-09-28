package com.revolsys.geometry.io;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.spring.resource.Resource;

public interface GeometryReaderFactory extends FileIoFactory, IoFactoryWithCoordinateSystem {
  default GeometryReader newGeometryReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newGeometryReader(resource);
  }

  GeometryReader newGeometryReader(final Resource resource);
}
