package com.revolsys.io.map;

import com.revolsys.io.FileIoFactory;
import com.revolsys.spring.resource.Resource;

public interface MapReaderFactory extends FileIoFactory {
  default MapReader createMapReader(final Object source) {
    final Resource resource = com.revolsys.spring.resource.Resource.getResource(source);
    return createMapReader(resource);
  }

  MapReader createMapReader(final Resource resource);
}
