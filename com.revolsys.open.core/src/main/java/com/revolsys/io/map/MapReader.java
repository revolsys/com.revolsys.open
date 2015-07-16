package com.revolsys.io.map;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.io.Reader;

public interface MapReader extends Reader<Map<String, Object>> {
  static MapReader create(final Path path) {
    final MapReaderFactory factory = IoFactory.factory(MapReaderFactory.class, path);
    if (factory == null) {
      return null;
    } else {
      final MapReader reader = factory.createMapReader(path);
      return reader;
    }
  }

  static MapReader create(final Resource resource) {
    final MapReaderFactory factory = IoFactory.factory(MapReaderFactory.class, resource);
    if (factory == null) {
      return null;
    } else {
      final MapReader reader = factory.createMapReader(resource);
      return reader;
    }
  }

  static boolean isReadable(final Path path) {
    for (final String fileNameExtension : Paths.getFileNameExtensions(path)) {
      if (isReadable(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  static boolean isReadable(final String fileNameExtension) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(MapReaderFactory.class, fileNameExtension);
  }

}
