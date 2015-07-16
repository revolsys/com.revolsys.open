package com.revolsys.data.record.io;

import java.io.File;
import java.nio.file.Path;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Paths;
import com.revolsys.io.Reader;

public interface RecordReader extends Reader<Record> {
  static RecordReader create(final File file) {
    final Resource resource = new FileSystemResource(file);
    return create(resource);
  }

  static RecordReader create(final FileSystemResource resource, final RecordFactory factory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class,
      resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource, factory);
      return reader;
    }
  }

  static RecordReader create(final Path path) {
    return create(path, ArrayRecordFactory.INSTANCE);
  }

  static RecordReader create(final Path path, final RecordFactory factory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, path);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(path, factory);
      return reader;
    }
  }

  static RecordReader create(final Resource resource) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class,
      resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource);
      return reader;
    }
  }

  static RecordReader create(final Resource resource, final RecordFactory factory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class,
      resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource, factory);
      return reader;
    }
  }

  static RecordReader create(final String fileName) {
    final Resource resource = new FileSystemResource(fileName);
    return create(resource);
  }

  static boolean isReadable(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (isReadable(fileNameExtension)) {
        return true;
      }
    }
    return false;
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
    return ioFactoryRegistry.isFileExtensionSupported(RecordReaderFactory.class, fileNameExtension);
  }

  RecordDefinition getRecordDefinition();
}
