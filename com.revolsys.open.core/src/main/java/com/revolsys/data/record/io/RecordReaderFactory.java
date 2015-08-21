package com.revolsys.data.record.io;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.geometry.io.GeometryReader;
import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.Reader;
import com.revolsys.io.map.MapReader;
import com.revolsys.io.map.MapReaderFactory;
import com.revolsys.spring.resource.Resource;

public interface RecordReaderFactory
  extends GeometryReaderFactory, MapReaderFactory, IoFactoryWithCoordinateSystem {

  /**
   * Create a directory reader using the ({@link ArrayRecordFactory}).
   *
   * @return The reader.
   */
  default Reader<Record> createDirectoryRecordReader() {
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
  default Reader<Record> createDirectoryRecordReader(final File directory) {
    return createDirectoryRecordReader(directory, ArrayRecordFactory.INSTANCE);

  }

  /**
   * Create a reader for the directory using the specified data object
   * recordFactory.
   *
   * @param directory directory file to read.
   * @param recordFactory The recordFactory used to create data objects.
   * @return The reader for the file.
   */
  default Reader<Record> createDirectoryRecordReader(final File directory,
    final RecordFactory recordFactory) {
    final RecordDirectoryReader directoryReader = new RecordDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  @Override
  default GeometryReader createGeometryReader(final Resource resource) {
    final Reader<Record> recordReader = createRecordReader(resource);
    final Iterator<Record> recordIterator = recordReader.iterator();
    final RecordGeometryIterator iterator = new RecordGeometryIterator(recordIterator);
    final GeometryReader geometryReader = new GeometryReader(iterator);
    return geometryReader;
  }

  @Override
  default MapReader createMapReader(final Resource resource) {
    final RecordReader reader = createRecordReader(resource);
    return new RecordMapReader(reader);
  }

  /**
   * Create a reader for the resource using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  default RecordReader createRecordReader(final Object object) {
    return createRecordReader(object, ArrayRecordFactory.INSTANCE);

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
  default RecordReader createRecordReader(final Object source, final RecordFactory factory) {
    final Resource resource = com.revolsys.spring.resource.Resource.getResource(source);
    return createRecordReader(resource, factory);
  }

  RecordReader createRecordReader(Resource resource, RecordFactory factory);
}
