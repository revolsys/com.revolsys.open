package com.revolsys.gis.data.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

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
   * Create a reader for the file using the ({@link ArrayDataObjectFactory}).
   * 
   * @param file The file to read.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDataObjectReader(
    final File file) {
    return createDataObjectReader(file, DEFAULT_DATA_OBJECT_FACTORY);

  }

  /**
   * Create a reader for the file using the specified data object
   * dataObjectFactory.
   * 
   * @param file The file to read.
   * @param dataObjectFactory The dataObjectFactory used to create data objects.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDataObjectReader(
    final File file,
    final DataObjectFactory dataObjectFactory) {
    try {
      final FileInputStream in = new FileInputStream(file);
      return createDataObjectReader(in, dataObjectFactory);
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException("File does not exist:" + file, e);
    }
  }

  /**
   * Create a reader for the file using the ({@link ArrayDataObjectFactory}).
   * 
   * @param inputStream The file to read.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDataObjectReader(
    final InputStream inputStream) {
    return createDataObjectReader(inputStream, DEFAULT_DATA_OBJECT_FACTORY);

  }

  public Reader<DataObject> createDataObjectReader(
    final InputStream in,
    final Charset charset) {
    return createDataObjectReader(in, charset, DEFAULT_DATA_OBJECT_FACTORY);
  }

  public Reader<DataObject> createDataObjectReader(
    final InputStream in,
    final Charset charset,
    final DataObjectFactory dataObjectFactory) {
    final InputStreamReader reader = new InputStreamReader(in, charset);
    return createDataObjectReader(reader, dataObjectFactory);
  }

  /**
   * Create a reader for the file using the specified data object
   * dataObjectFactory.
   * 
   * @param inputStream The file to read.
   * @param dataObjectFactory The dataObjectFactory used to create data objects.
   * @return The reader for the file.
   */
  public Reader<DataObject> createDataObjectReader(
    final InputStream inputStream,
    final DataObjectFactory dataObjectFactory) {
    return createDataObjectReader(inputStream, Charset.defaultCharset(),
      dataObjectFactory);
  }

  public Reader<DataObject> createDataObjectReader(
    final java.io.Reader in) {
    return createDataObjectReader(in, DEFAULT_DATA_OBJECT_FACTORY);
  }

  /**
   * Create a reader for the URL using the ({@link ArrayDataObjectFactory}).
   * 
   * @param url The URL to read.
   * @return The reader for the URL.
   */
  public Reader<DataObject> createDataObjectReader(
    final URL url) {
    return createDataObjectReader(url, DEFAULT_DATA_OBJECT_FACTORY);

  }

  /**
   * Create a reader for the URL using the specified data object
   * dataObjectFactory.
   * 
   * @param url The file to read.
   * @param dataObjectFactory The dataObjectFactory used to create data objects.
   * @return The reader for the URL.
   */
  public Reader<DataObject> createDataObjectReader(
    final URL url,
    final DataObjectFactory dataObjectFactory) {
    try {
      final InputStream in = url.openStream();
      return createDataObjectReader(in, dataObjectFactory);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:" + url, e);
    }
  }

}
