package com.revolsys.data.record.io;

import java.io.File;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.IoFactoryWithCoordinateSystem;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.Reader;

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
  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  default Reader<Map<String, Object>> createMapReader(final Resource resource) {
    final Reader reader = createRecordReader(resource);
    return reader;
  }

  /**
   * Create a reader for the path using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  default RecordReader createRecordReader(final Path path) {
    return createRecordReader(path, ArrayRecordFactory.INSTANCE);
  }

  default RecordReader createRecordReader(final Path path, final RecordFactory factory) {
    final PathResource resource = new PathResource(path);
    return createRecordReader(resource, factory);
  }

  /**
   * Create a reader for the resource using the ({@link ArrayRecordFactory}
   * ).
   *
   * @param file The file to read.
   * @return The reader for the file.
   */
  default RecordReader createRecordReader(final Resource resource) {
    return createRecordReader(resource, ArrayRecordFactory.INSTANCE);

  }

  RecordReader createRecordReader(Resource resource, RecordFactory factory);
}
