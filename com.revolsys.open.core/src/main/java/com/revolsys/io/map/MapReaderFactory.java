package com.revolsys.io.map;

import com.revolsys.io.FileIoFactory;
import com.revolsys.spring.resource.Resource;

public interface MapReaderFactory extends FileIoFactory {
  default MapReader newMapReader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newMapReader(resource);
  }

  MapReader newMapReader(final Resource resource);
}
