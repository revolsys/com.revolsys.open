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
  /**
   * Create a {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader create(final Object source) {
    final RecordFactory recordFactory = ArrayRecordFactory.INSTANCE;
    return create(source, recordFactory);
  }

  /**
   * Create a {@link RecordReader} for the given source. The source can be one of the following
   * classes.
   *
   * <ul>
   *   <li>{@link Path}</li>
   *   <li>{@link File}</li>
   *   <li>{@link Resource}</li>
   * </ul>
   * @param source The source to read the records from.
   * @param recordFactory The factory used to create records.
   * @return The reader.
   * @throws IllegalArgumentException If the source is not a supported class.
   */
  static RecordReader create(final Object source, final RecordFactory recordFactory) {
    final RecordReaderFactory readerFactory = IoFactory.factory(RecordReaderFactory.class, source);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(source, recordFactory);
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
