package com.revolsys.data.io;

import java.io.File;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;

public abstract class AbstractRecordReaderFactory extends
AbstractMapReaderFactory implements RecordReaderFactory {
  public static RecordReaderFactory getRecordReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final RecordReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      RecordReaderFactory.class, resource);
    return readerFactory;
  }

  public static RecordReaderFactory getRecordReaderFactory(final String fileName) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final RecordReaderFactory readerFactory = ioFactoryRegistry.getFactoryByFileName(
      RecordReaderFactory.class, fileName);
    return readerFactory;
  }

  public static boolean hasRecordReaderFactory(final Resource resource) {
    return getRecordReaderFactory(resource) != null;
  }

  public static boolean hasRecordReaderFactory(final String fileName) {
    return getRecordReaderFactory(fileName) != null;
  }

  public static RecordReader recordReader(final File file) {
    final FileSystemResource resource = new FileSystemResource(file);
    return recordReader(resource);
  }

  public static RecordReader recordReader(final Resource resource) {
    final RecordReaderFactory readerFactory = getRecordReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource);
      return reader;
    }
  }

  public static RecordReader recordReader(final Resource resource,
    final RecordFactory factory) {
    final RecordReaderFactory readerFactory = getRecordReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource,
        factory);
      return reader;
    }
  }

  private final RecordFactory recordFactory = new ArrayRecordFactory();

  private final boolean binary;

  public AbstractRecordReaderFactory(final String name, final boolean binary) {
    super(name);
    this.binary = binary;
  }

  /**
   * Create a directory reader using the ({@link ArrayRecordFactory}).
   *
   * @return The reader.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader() {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    return directoryReader;
  }

  /**
   * Create a reader for the directory using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param directory The directory to read.
   * @return The reader for the file.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader(final File directory) {
    return createDirectoryRecordReader(directory, this.recordFactory);

  }

  /**
   * Create a reader for the directory using the specified data object
   * recordFactory.
   *
   * @param directory directory file to read.
   * @param recordFactory The recordFactory used to create data objects.
   * @return The reader for the file.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader(final File directory,
    final RecordFactory recordFactory) {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    final Reader reader = createRecordReader(resource);
    return reader;
  }

  /**
   * Create a reader for the resource using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  @Override
  public RecordReader createRecordReader(final Resource resource) {
    return createRecordReader(resource, this.recordFactory);

  }

  @Override
  public boolean isBinary() {
    return this.binary;
  }
}
