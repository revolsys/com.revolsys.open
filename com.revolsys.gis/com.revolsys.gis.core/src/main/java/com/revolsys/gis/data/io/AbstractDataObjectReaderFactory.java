package com.revolsys.gis.data.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.gis.data.model.ArrayDataObjectFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.io.AbstractIoFactory;

public abstract class AbstractDataObjectReaderFactory extends AbstractIoFactory
  implements DataObjectReaderFactory {
  /** The default data object dataObjectFactory instance. */
  private static final DataObjectFactory DEFAULT_DATA_OBJECT_FACTORY = new ArrayDataObjectFactory();

  public AbstractDataObjectReaderFactory(
    final String name) {
    super(name);
  }

  /**
   * Create a reader for the resource using the ({@link ArrayDataObjectFactory}
   * ).
   * 
   * @param file The file to read.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDataObjectReader(
    final Resource resource) {
    return createDataObjectReader(resource, DEFAULT_DATA_OBJECT_FACTORY);

  }

  /**
   * Create a reader for the directory using the ({@link ArrayDataObjectFactory}
   * ).
   * 
   * @param directory The directory to read.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDirectoryDataObjectReader(
    final File directory) {
    return createDirectoryDataObjectReader(directory,
      DEFAULT_DATA_OBJECT_FACTORY);

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

}
