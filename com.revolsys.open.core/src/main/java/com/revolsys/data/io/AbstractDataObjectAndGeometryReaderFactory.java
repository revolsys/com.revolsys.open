package com.revolsys.data.io;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.ArrayRecordFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.RecordFactory;
import com.revolsys.gis.geometry.io.AbstractGeometryReaderFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.Reader;

public abstract class AbstractDataObjectAndGeometryReaderFactory extends
  AbstractGeometryReaderFactory implements RecordReaderFactory,
  MapReaderFactory {

  public static RecordReader recordReader(
    final FileSystemResource resource, final RecordFactory factory) {
    final RecordReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(
        resource, factory);
      return reader;
    }
  }

  public static RecordReader recordReader(final Resource resource) {
    final RecordReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final RecordReader reader = readerFactory.createRecordReader(resource);
      return reader;
    }
  }

  public static RecordReaderFactory getDataObjectReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final RecordReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      RecordReaderFactory.class, resource);
    return readerFactory;
  }

  private final ArrayRecordFactory dataObjectFactory = new ArrayRecordFactory();

  private boolean singleFile = true;

  private boolean customAttributionSupported = true;

  public AbstractDataObjectAndGeometryReaderFactory(final String name,
    final boolean binary) {
    super(name, binary);
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
    return createRecordReader(resource, dataObjectFactory);

  }

  /**
   * Create a directory reader using the ({@link ArrayRecordFactory}).
   * 
   * @return The reader.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader() {
    final DataObjectDirectoryReader directoryReader = new DataObjectDirectoryReader();
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
    return createDirectoryRecordReader(directory, dataObjectFactory);
  }

  /**
   * Create a reader for the directory using the specified data object
   * dataObjectFactory.
   * 
   * @param directory directory file to read.
   * @param dataObjectFactory The dataObjectFactory used to create data objects.
   * @return The reader for the file.
   */
  @Override
  public Reader<Record> createDirectoryRecordReader(
    final File directory, final RecordFactory dataObjectFactory) {
    final DataObjectDirectoryReader directoryReader = new DataObjectDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  @Override
  public GeometryReader createGeometryReader(final Resource resource) {
    final Reader<Record> dataObjectReader = createRecordReader(resource);
    final Iterator<Record> dataObjectIterator = dataObjectReader.iterator();
    final DataObjectGeometryIterator iterator = new DataObjectGeometryIterator(
      dataObjectIterator);
    final GeometryReader geometryReader = new GeometryReader(iterator);
    return geometryReader;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    final Reader reader = createRecordReader(resource);
    return reader;
  }

  @Override
  public boolean isCustomAttributionSupported() {
    return customAttributionSupported;
  }

  @Override
  public boolean isSingleFile() {
    return singleFile;
  }

  protected void setCustomAttributionSupported(
    final boolean customAttributionSupported) {
    this.customAttributionSupported = customAttributionSupported;
  }

  protected void setSingleFile(final boolean singleFile) {
    this.singleFile = singleFile;
  }
}
