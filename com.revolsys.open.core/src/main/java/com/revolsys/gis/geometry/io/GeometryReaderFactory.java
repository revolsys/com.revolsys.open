package com.revolsys.gis.geometry.io;

import org.springframework.core.io.Resource;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;

public interface GeometryReaderFactory extends FileIoFactory, IoFactoryWithCoordinateSystem {
  default GeometryReader createGeometryReader(final Object source) {
    final Resource resource = com.revolsys.spring.resource.Resource.getResource(source);
    return createGeometryReader(resource);
  }

  GeometryReader createGeometryReader(final Resource resource);

}
