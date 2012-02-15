package com.revolsys.gis.data.io;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.gis.geometry.io.AbstractGeometryReaderFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapReaderFactory;
import com.revolsys.io.Reader;

public abstract class AbstractDataObjectAndGeometryReaderFactory extends
  AbstractGeometryReaderFactory implements DataObjectReaderFactory,
  MapReaderFactory {
  /** The default data object dataObjectFactory instance. */
  private static final DataObjectFactory DEFAULT_DATA_OBJECT_FACTORY = new ArrayDataObjectFactory();

  public static DataObjectReader dataObjectReader(
    final FileSystemResource resource,
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

  public static DataObjectReader dataObjectReader(final Resource resource) {
    final DataObjectReaderFactory readerFactory = getDataObjectReaderFactory(resource);
    if (readerFactory == null) {
      return null;
    } else {
      final DataObjectReader reader = readerFactory.createDataObjectReader(resource);
      return reader;
    }
  }

  protected static DataObjectReaderFactory getDataObjectReaderFactory(
    final Resource resource) {
    final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;
    final DataObjectReaderFactory readerFactory = ioFactoryRegistry.getFactoryByResource(
      DataObjectReaderFactory.class, resource);
    return readerFactory;
  }

  private boolean singleFile = true;

  private boolean customAttributionSupported = true;

  public AbstractDataObjectAndGeometryReaderFactory(final String name,
    final boolean binary) {
    super(name, binary);
  }

  /**
   * Create a reader for the resource using the ({@link ArrayDataObjectFactory}
   * ).
   * 
   * @param file The file to read.
   * @return The reader for the file.
   */
  public DataObjectReader createDataObjectReader(final Resource resource) {
    return createDataObjectReader(resource, DEFAULT_DATA_OBJECT_FACTORY);

  }

  /**
   * Create a directory reader using the ({@link ArrayDataObjectFactory}).
   * 
   * @return The reader.
   */
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
  public Reader<DataObject> createDirectoryDataObjectReader(final File directory) {
    return createDirectoryDataObjectReader(directory,
      DEFAULT_DATA_OBJECT_FACTORY);

  }

  /**
   * Create a reader for the directory using the specified data object
   * dataObjectFactory.
   * 
   * @param directory directory file to read.
   * @param dataObjectFactory The dataObjectFactory used to create data objects.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDirectoryDataObjectReader(
    final File directory,
    final DataObjectFactory dataObjectFactory) {
    final DataObjectDirectoryReader directoryReader = new DataObjectDirectoryReader();
    directoryReader.setFileExtensions(getFileExtensions());
    directoryReader.setDirectory(directory);
    return directoryReader;
  }

  public GeometryReader createGeometryReader(final Resource resource) {
    final Reader<DataObject> dataObjectReader = createDataObjectReader(resource);
    final Iterator<DataObject> dataObjectIterator = dataObjectReader.iterator();
    final DataObjectGeometryIterator iterator = new DataObjectGeometryIterator(
      dataObjectIterator);
    final GeometryReader geometryReader = new GeometryReader(iterator);
    return geometryReader;
  }

  public Reader<Map<String, Object>> createMapReader(final Resource resource) {
    final Reader reader = createDataObjectReader(resource);
    return reader;
  }

  public boolean isCustomAttributionSupported() {
    return customAttributionSupported;
  }

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
