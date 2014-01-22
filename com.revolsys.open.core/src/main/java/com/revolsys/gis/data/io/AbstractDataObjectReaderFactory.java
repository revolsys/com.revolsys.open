package com.revolsys.gis.data.io;

import java.io.File;
import java.util.Map;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.io.AbstractMapReaderFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;

public abstract class AbstractDataObjectReaderFactory extends
  AbstractMapReaderFactory implements DataObjectReaderFactory {
  public static DataObjectReader dataObjectReader(final Resource resource) {
    final DataObjectReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final DataObjectReader reader = readerFactory.createDataObjectReader(resource);
      return reader;
    }
  }

  public static DataObjectReader dataObjectReader(final Resource resource,
    final DataObjectFactory factory) {
    final DataObjectReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final DataObjectReader reader = readerFactory.createDataObjectReader(
        resource, factory);
      return reader;
    }
  }

  public static DataObjectReaderFactory getDataObjectReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final DataObjectReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      DataObjectReaderFactory.class, resource);
    return readerFactory;
  }

  public static DataObjectReaderFactory getDataObjectReaderFactory(
    final String fileName) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();
    final DataObjectReaderFactory readerFactory = ioFactoryRegistry.getFactoryByFileName(
      DataObjectReaderFactory.class, fileName);
    return readerFactory;
  }

  public static boolean hasDataObjectReaderFactory(final Resource resource) {
    return getDataObjectReaderFactory(resource) != null;
  }

  public static boolean hasDataObjectReaderFactory(final String fileName) {
    return getDataObjectReaderFactory(fileName) != null;
  }

  private final DataObjectFactory dataObjectFactory = new ArrayDataObjectFactory();

  private final boolean binary;

  public AbstractDataObjectReaderFactory(final String name, final boolean binary) {
    super(name);
    this.binary = binary;
  }

  /**
   * Create a reader for the resource using the ({@link ArrayDataObjectFactory}
   * ).
   * 
   * @param file The file to read.
   * @return The reader for the file.
   */
  @Override
  public DataObjectReader createDataObjectReader(final Resource resource) {
    return createDataObjectReader(resource, dataObjectFactory);

  }

  /**
   * Create a directory reader using the ({@link ArrayDataObjectFactory}).
   * 
   * @return The reader.
   */
  @Override
  public Reader<DataObject> createDirectoryDataObjectReader() {
    final DataObjectDirectoryReader directoryReader = new DataObjectDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    return directoryReader;
  }

  /**
   * Create a reader for the directory using the ({@link ArrayDataObjectFactory}
   * ).
   * 
   * @param directory The directory to read.
   * @return The reader for the file.
   */
  @Override
  public Reader<DataObject> createDirectoryDataObjectReader(final File directory) {
    return createDirectoryDataObjectReader(directory, dataObjectFactory);

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
  public Reader<DataObject> createDirectoryDataObjectReader(
    final File directory, final DataObjectFactory dataObjectFactory) {
    final DataObjectDirectoryReader directoryReader = new DataObjectDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  @Override
  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    final Reader reader = createDataObjectReader(resource);
    return reader;
  }

  @Override
  public boolean isBinary() {
    return binary;
  }
}
