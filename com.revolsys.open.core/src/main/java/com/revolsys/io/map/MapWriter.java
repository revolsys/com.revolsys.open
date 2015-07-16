package com.revolsys.io.map;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.io.Writer;

public interface MapWriter extends Writer<Map<String, ? extends Object>> {
  static MapWriter create(final Path path) {
    final MapWriterFactory factory = IoFactory.factory(MapWriterFactory.class, path);
    if (factory == null) {
      return null;
    } else {
      final MapWriter writer = factory.createMapWriter(path);
      return writer;
    }
  }

  static MapWriter create(final Resource resource) {
    final MapWriterFactory factory = IoFactory.factory(MapWriterFactory.class, resource);
    if (factory == null) {
      return null;
    } else {
      final MapWriter writer = factory.createMapWriter(resource);
      return writer;
    }
  }

  static boolean isWritable(final Path path) {
    for (final String fileNameExtension : Paths.getFileNameExtensions(path)) {
      if (isWritable(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  static boolean isWritable(final String fileNameExtension) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(MapWriterFactory.class, fileNameExtension);
  }
}
