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
import com.revolsys.io.Writer;

public class RecordIo {
  public static boolean canReadRecords(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (canReadRecords(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  public static boolean canReadRecords(final Path path) {
    for (final String fileNameExtension : Paths.getFileNameExtensions(path)) {
      if (canReadRecords(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  public static boolean canReadRecords(final String fileNameExtension) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(RecordReaderFactory.class, fileNameExtension);
  }

  public static boolean canWriteRecords(final File file) {
    for (final String fileNameExtension : FileUtil.getFileNameExtensions(file)) {
      if (canWriteRecords(fileNameExtension)) {
        return true;
      }
    }
    return false;
  }

  public static boolean canWriteRecords(final String fileNameExtension) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    return ioFactoryRegistry.isFileExtensionSupported(RecordWriterFactory.class, fileNameExtension);
  }

  public static void copyRecords(final File sourceFile, final File targetFile) {
    try (
      RecordReader reader = recordReader(sourceFile)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + sourceFile);
      } else {
        copyRecords(reader, targetFile);
      }
    }

  }

  public static void copyRecords(final File sourceFile, final Writer<Record> writer) {
    try (
      RecordReader reader = recordReader(sourceFile)) {
      if (reader == null) {
        throw new IllegalArgumentException("Unable to read " + sourceFile);
      } else {
        copyRecords(reader, writer);
      }
    }

  }

  public static void copyRecords(final Reader<Record> reader, final Writer<Record> writer) {
    if (reader != null && writer != null) {
      for (final Record record : reader) {
        writer.write(record);
      }
    }
  }

  public static void copyRecords(final RecordReader reader, final File targetFile) {
    if (reader != null) {
      final RecordDefinition recordDefinition = reader.getRecordDefinition();
      try (
        Writer<Record> writer = recordWriter(recordDefinition, targetFile)) {
        if (writer == null) {
          throw new IllegalArgumentException("Unable to create writer " + targetFile);
        } else {
          copyRecords(reader, writer);
        }
      }
    }
  }

  public static RecordReader recordReader(final File file) {
    final Resource resource = new FileSystemResource(file);
    return recordReader(resource);
  }

  public static RecordReader recordReader(final FileSystemResource resource,
    final RecordFactory factory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class,
      resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource, factory);
      return reader;
    }
  }

  public static RecordReader recordReader(final Path path) {
    return recordReader(path, ArrayRecordFactory.INSTANCE);
  }

  public static RecordReader recordReader(final Path path, final RecordFactory factory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, path);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(path, factory);
      return reader;
    }
  }

  public static RecordReader recordReader(final Resource resource) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class,
      resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource);
      return reader;
    }
  }

  public static RecordReader recordReader(final Resource resource, final RecordFactory factory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class,
      resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource, factory);
      return reader;
    }
  }

  public static RecordReader recordReader(final String fileName) {
    final Resource resource = new FileSystemResource(fileName);
    return recordReader(resource);
  }

  public static Writer<Record> recordWriter(final RecordDefinition recordDefinition,
    final File file) {
    if (file == null) {
      return null;
    } else {
      final Path path = Paths.get(file);
      return recordWriter(recordDefinition, path);
    }
  }

  public static Writer<Record> recordWriter(final RecordDefinition recordDefinition,
    final Path path) {
    final RecordWriterFactory writerFactory = IoFactory.factory(RecordWriterFactory.class, path);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<Record> writer = writerFactory.createRecordWriter(recordDefinition, path);
      return writer;
    }
  }

  public static Writer<Record> recordWriter(final RecordDefinition recordDefinition,
    final Resource resource) {
    final RecordWriterFactory writerFactory = IoFactory.factory(RecordWriterFactory.class,
      resource);
    if (writerFactory == null) {
      return null;
    } else {
      final Writer<Record> writer = writerFactory.createRecordWriter(recordDefinition, resource);
      return writer;
    }
  }

}
