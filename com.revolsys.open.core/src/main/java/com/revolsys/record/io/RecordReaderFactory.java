package com.revolsys.record.io;

import java.io.File;
import java.nio.file.Path;

import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.Reader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.record.ArrayRecord;
import com.revolsys.record.Record;
import com.revolsys.record.RecordFactory;
import com.revolsys.spring.resource.Resource;

public interface RecordReaderFactory
  extends GeometryReaderFactory, MapReaderFactory, IoFactoryWithCoordinateSystem {
  /**
   * Construct a new directory reader using the ({@link ArrayRecordFactory}).
   *
   * @return The reader.
   */
  default Reader<Record> newDirectoryRecordReader() {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    return directoryReader;
  }

  /**
   * Construct a new reader for the directory using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param directory The directory to read.
   * @return The reader for the file.
   */
  default Reader<Record> newDirectoryRecordReader(final File directory) {
    return newDirectoryRecordReader(directory, ArrayRecord.FACTORY);

  }

  /**
   * Construct a new reader for the directory using the specified data object
   * recordFactory.
   *
   * @param directory directory file to read.
   * @param recordFactory The recordFactory used to create data objects.
   * @return The reader for the file.
   */
  default <R extends Record> Reader<Record> newDirectoryRecordReader(final File directory,
    final RecordFactory<R> recordFactory) {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  @Override
  default GeometryReader newGeometryReader(final Resource resource) {
    final Reader<Record> recordReader = newRecordReader(resource);
    final RecordGeometryReader geometryReader = new RecordGeometryReader(recordReader);
    return geometryReader;
  }

  @Override
  default MapReader newMapreader(final Resource resource) {
    final RecordReader reader = newRecordReader(resource);
    return new RecordMapReader(reader);
  }

  /**
   * Construct a new reader for the resource using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  default RecordReader newRecordReader(final Object object) {
    return newRecordReader(object, ArrayRecord.FACTORY);

  }

  /**
   * Construct a new {@link RecordReader} for the given source. The source can be one of the following
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
  default RecordReader newRecordReader(final Object source,
    final RecordFactory<? extends Record> factory) {
    final Resource resource = Resource.getResource(source);
    return newRecordReader(resource, factory);
  }

  RecordReader newRecordReader(Resource resource, RecordFactory<? extends Record> factory);
}
