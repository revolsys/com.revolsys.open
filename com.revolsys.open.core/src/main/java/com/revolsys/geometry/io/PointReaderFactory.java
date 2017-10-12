package com.revolsys.geometry.io;

import java.util.Collections;
import java.util.Map;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.ReadIoFactory;
import com.revolsys.spring.resource.Resource;

public interface PointReaderFactory
  extends FileIoFactory, IoFactoryWithCoordinateSystem, ReadIoFactory {
  default PointReader newPointReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newPointReader(resource);
  }

  default PointReader newPointReader(final Resource resource) {
    final Map<String, Object> properties = Collections.emptyMap();
    return newPointReader(resource, properties);
  }

  PointReader newPointReader(final Resource resource, Map<String, ? extends Object> properties);
}
