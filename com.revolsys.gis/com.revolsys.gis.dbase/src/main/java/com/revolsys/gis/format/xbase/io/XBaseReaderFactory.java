package com.revolsys.gis.format.xbase.io;

import java.io.File;
import java.io.InputStream;

import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;

public class XBaseReaderFactory extends AbstractDataObjectReaderFactory {

  /** The factory instance. */
  public static final XBaseReaderFactory INSTANCE = new XBaseReaderFactory();

  /**
   * Get the factory instance.
   * 
   * @return The instance.
   */
  public static XBaseReaderFactory get() {
    return INSTANCE;
  }

  public XBaseReaderFactory() {
    super("dBase");
    addMediaTypeAndFileExtension("application/dbase", "dbf");
    addMediaTypeAndFileExtension("application/dbf", "dbf");
  }

  public Reader<DataObject> createDataObjectReader(
    java.io.Reader in,
    DataObjectFactory factory) {
    return null;
  }
  
  @Override
  public Reader<DataObject> createDataObjectReader(
    final File file,
    final DataObjectFactory dataObjectFactory) {
    return new XBaseReader(file, dataObjectFactory);
  }

  /**
   * Create a reader for the file using the specified data object factory.
   * 
   * @param file The file to read.
   * @param factory The factory used to create data objects.
   * @return The reader for the file.
   */
  @Override
  public Reader<DataObject> createDataObjectReader(
    final InputStream in,
    final DataObjectFactory dataObjectFactory) {
    return new XBaseReader(in, dataObjectFactory);
  }

}
