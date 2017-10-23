package com.revolsys.geometry.io;

import java.util.Collections;
import java.util.Map;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface GeometryReaderFactory
  extends FileIoFactory, IoFactoryWithCoordinateSystem, ReadIoFactory {
  default GeometryReader newGeometryReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newGeometryReader(resource);
  }

  default GeometryReader newGeometryReader(final Resource resource) {
    return newGeometryReader(resource, Collections.emptyMap());
  }

  GeometryReader newGeometryReader(final Resource resource,
    Map<String, ? extends Object> properties);
}
