package com.revolsys.gis.geometry.io;

import java.io.File;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.jts.geom.Geometry;

public class GeometryIo {
  public static boolean canReadGeometry(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (canReadGeometry(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  public static boolean canReadGeometry(final Path path) {
    for (final String fileNameExtension : Paths.getFileNameExtensions(path)) {
      if (canReadGeometry(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  public static boolean canReadGeometry(final String fileNameExtension) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(GeometryReaderFactory.class,
      fileNameExtension);
  }

  public static boolean canWriteGeometry(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (canWriteGeometry(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  public static boolean canWriteGeometry(final String fileNameExtension) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(GeometryWriterFactory.class,
      fileNameExtension);
  }

  public static void copyGeometry(final File sourceFile, final File targetFile) {
    try (
      GeometryReader reader = geometryReader(sourceFile)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + sourceFile);
      } else {
        copyGeometry(reader, targetFile);
      }
    }

  }

  public static void copyGeometry(final File sourceFile, final Writer<Geometry> writer) {
    try (
      GeometryReader reader = geometryReader(sourceFile)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + sourceFile);
      } else {
        copyGeometry(reader, writer);
      }
    }

  }

  public static void copyGeometry(final GeometryReader reader, final File targetFile) {
    if (reader != null) {
      try (
        Writer<Geometry> writer = geometryWriter(targetFile)) {
        if (writer == null) {
          throw new IllegalArgumentException("Unable to create writer " + targetFile);
        } else {
          copyGeometry(reader, writer);
        }
      }
    }
  }

  public static void copyGeometry(final Reader<Geometry> reader, final Writer<Geometry> writer) {
    if (reader != null && writer != null) {
      for (final Geometry geometry : reader) {
        writer.write(geometry);
      }
    }
  }

  public static GeometryReader geometryReader(final File file) {
    final Resource resource = new FileSystemResource(file);
    return geometryReader(resource);
  }

  public static GeometryReader geometryReader(final Path path) {
    final GeometryReaderFactory readerFactory = geometryReaderFactory(path);
    if (readerFactory == null) {
      return null;
    } else {
      final GeometryReader reader = readerFactory.createGeometryReader(path);
      return reader;
    }
  }

  public static GeometryReader geometryReader(final Resource resource) {
    final GeometryReaderFactory readerFactory = geometryReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final GeometryReader reader = readerFactory.createGeometryReader(resource);
      return reader;
    }
  }

  public static GeometryReader geometryReader(final String fileName) {
    final Resource resource = new FileSystemResource(fileName);
    return geometryReader(resource);
  }

  public static GeometryReaderFactory geometryReaderFactory(final Path path) {
    final String fileName = Paths.getFileName(path);
    return geometryReaderFactory(fileName);
  }

  public static GeometryReaderFactory geometryReaderFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeometryReaderFactory readerFactory = ioFactoryRegistry
      .getFactory(GeometryReaderFactory.class, resource);
    return readerFactory;
  }

  public static GeometryReaderFactory geometryReaderFactory(final String fileName) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeometryReaderFactory readerFactory = ioFactoryRegistry
      .getFactoryByFileName(GeometryReaderFactory.class, fileName);
    return readerFactory;
  }

  public static Writer<Geometry> geometryWriter(final File file) {
    return geometryWriter(new FileSystemResource(file));
  }

  public static Writer<Geometry> geometryWriter(final Resource resource) {
    final GeometryWriterFactory writerFactory = geometryWriterFactory(resource);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<Geometry> writer = writerFactory.createGeometryWriter(resource);
      return writer;
    }
  }

  public static GeometryWriterFactory geometryWriterFactory(final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final GeometryWriterFactory writerFactory = ioFactoryRegistry
      .getFactory(GeometryWriterFactory.class, resource);
    return writerFactory;
  }

  public static boolean hasGeometryReaderFactory(final Resource resource) {
    return geometryReaderFactory(resource) != null;
  }

  public static boolean hasGeometryReaderFactory(final String fileName) {
    return geometryReaderFactory(fileName) != null;
  }

}
