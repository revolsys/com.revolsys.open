package com.revolsys.io.map;

import com.revolsys.io.FileIoFactory;
import com.revolsys.spring.resource.Resource;

public interface MapReaderFactory extends FileIoFactory {
  default MapReader newMapreader(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newMapreader(resource);
  }

  MapReader newMapreader(final Resource resource);
}
