package com.revolsys.data.record.io;

import java.io.File;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.Reader;

public abstract class AbstractRecordReaderFactory extends
AbstractMapReaderFactory implements RecordReaderFactory {
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
